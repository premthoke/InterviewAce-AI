package com.interviewace.backend.service.resume;

import com.interviewace.backend.dto.resume.ResumeVersionResponse;
import com.interviewace.backend.entity.resume.Resume;
import com.interviewace.backend.entity.resume.ResumeVersion;
import com.interviewace.backend.entity.user.User;
import com.interviewace.backend.enums.ParseStatus;
import com.interviewace.backend.enums.StorageProvider;
import com.interviewace.backend.exception.InvalidResumeFileException;
import com.interviewace.backend.exception.ResumeUploadException;
import com.interviewace.backend.mapper.ResumeMapper;
import com.interviewace.backend.repository.resume.ResumeRepository;
import com.interviewace.backend.repository.resume.ResumeVersionRepository;
import com.interviewace.backend.service.parser.PdfParserService;
import com.interviewace.backend.service.storage.CloudinaryService;
import com.interviewace.backend.service.storage.CloudinaryUploadResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Implementation of {@link ResumeWorkflowService}.
 *
 * <p>Orchestrates the complete resume upload workflow:</p>
 * <ol>
 *     <li>Validate the uploaded file</li>
 *     <li>Extract raw data from {@code MultipartFile}</li>
 *     <li>Compute SHA-256 checksum</li>
 *     <li>Upload to Cloudinary (external HTTP call — outside transaction)</li>
 *     <li>Persist Resume + ResumeVersion in a single DB transaction</li>
 *     <li>Return the response DTO</li>
 * </ol>
 *
 * <p><b>Transaction design:</b> The Cloudinary upload (step 4) happens
 * <em>outside</em> the database transaction. The DB persistence (step 5)
 * uses {@link TransactionTemplate} to keep the transaction scope minimal —
 * we never hold a DB connection open during an external HTTP request.
 * If DB persistence fails, a compensating delete removes the orphaned
 * Cloudinary file.</p>
 *
 * <p><b>Why {@code TransactionTemplate} instead of {@code @Transactional}?</b>
 * Spring's {@code @Transactional} annotation uses proxy-based AOP and does
 * not work on private methods or self-invocations within the same class.
 * {@code TransactionTemplate} provides programmatic transaction control
 * that works correctly regardless of method visibility, giving us precise
 * control over the transaction boundary.</p>
 *
 * <p><b>Phase 5.4B — PDF parsing integration:</b> After the upload persists
 * successfully, the service invokes {@link PdfParserService#parseResume(Long)}
 * synchronously. Parsing is fire-and-forget — errors are caught and persisted
 * by {@code PdfParserServiceImpl}, never propagated to the caller. The response
 * DTO is built after parsing completes, so it includes the post-parse state
 * ({@code parseStatus}, {@code hasText}, {@code wordCount}).</p>
 *
 * @see CloudinaryService
 * @see PdfParserService
 * @see ResumeService
 */
@Service
public class ResumeWorkflowServiceImpl implements ResumeWorkflowService {

    private static final Logger log = LoggerFactory.getLogger(ResumeWorkflowServiceImpl.class);

    /** Maximum file size allowed: 5MB. */
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    /** The only allowed MIME type for resume uploads. */
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    private final CloudinaryService cloudinaryService;
    private final PdfParserService pdfParserService;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository resumeVersionRepository;
    private final ResumeMapper resumeMapper;
    private final TransactionTemplate transactionTemplate;

    /**
     * Constructor injection for all dependencies.
     *
     * @param cloudinaryService        Cloudinary storage service
     * @param pdfParserService         PDF text extraction service
     * @param resumeRepository         repository for Resume aggregate root
     * @param resumeVersionRepository  repository for ResumeVersion entities
     * @param resumeMapper             mapper for entity-to-DTO conversions
     * @param transactionTemplate      programmatic transaction manager
     */
    public ResumeWorkflowServiceImpl(CloudinaryService cloudinaryService,
                                     PdfParserService pdfParserService,
                                     ResumeRepository resumeRepository,
                                     ResumeVersionRepository resumeVersionRepository,
                                     ResumeMapper resumeMapper,
                                     TransactionTemplate transactionTemplate) {
        this.cloudinaryService = cloudinaryService;
        this.pdfParserService = pdfParserService;
        this.resumeRepository = resumeRepository;
        this.resumeVersionRepository = resumeVersionRepository;
        this.resumeMapper = resumeMapper;
        this.transactionTemplate = transactionTemplate;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This method is intentionally <b>not</b> annotated with
     * {@code @Transactional}. The Cloudinary upload is an external HTTP
     * call that must not hold a database connection open. The DB
     * persistence is wrapped in a {@link TransactionTemplate} block
     * with minimal scope.</p>
     */
    @Override
    public ResumeVersionResponse uploadResume(User user, MultipartFile file) {
        log.info("Starting upload: user={}", user.getEmail());

        // --- Step 1: Validate file ---
        validateFile(file);

        // --- Step 2: Extract raw data from MultipartFile ---
        byte[] fileBytes = extractBytes(file);
        String originalFilename = file.getOriginalFilename();
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // --- Step 3: Compute SHA-256 checksum ---
        String checksum = computeChecksum(fileBytes);

        // --- Step 4: Upload to Cloudinary (OUTSIDE transaction) ---
        CloudinaryUploadResult uploadResult = cloudinaryService.uploadResume(
                fileBytes, originalFilename, contentType
        );
        log.info("Cloudinary upload completed: publicId={}", uploadResult.publicId());

        // --- Steps 5–11: Persist in DB (INSIDE transaction) ---
        ResumeVersion savedVersion;
        try {
            savedVersion = persistResumeVersion(
                    user, originalFilename, contentType, fileSize,
                    checksum, uploadResult
            );

            log.info("Resume version {} created: user={}, publicId={}",
                    savedVersion.getVersionNumber(), user.getEmail(), uploadResult.publicId());

        } catch (Exception e) {
            // --- Compensating delete: remove orphaned Cloudinary file ---
            log.error("Database persistence failed: user={}, error={}",
                    user.getEmail(), e.getMessage(), e);

            try {
                cloudinaryService.deleteResume(uploadResult.publicId());
                log.info("Compensating delete successful: publicId={}", uploadResult.publicId());
            } catch (Exception deleteEx) {
                log.error("Compensating delete failed: publicId={}, error={}",
                        uploadResult.publicId(), deleteEx.getMessage(), deleteEx);
            }

            throw new ResumeUploadException(
                    "Resume upload failed. The file has been cleaned up. Please try again.", e);
        }

        // --- Step 12: Invoke PDF parser (fire-and-forget, errors persisted internally) ---
        pdfParserService.parseResume(savedVersion.getId());

        // --- Step 13: Re-read entity to capture post-parse state and map to DTO ---
        ResumeVersion freshVersion = transactionTemplate.execute(status ->
                resumeVersionRepository.findById(savedVersion.getId()).orElse(savedVersion)
        );
        return resumeMapper.toVersionResponse(freshVersion);
    }

    /* ------------------------------------------------------------------ */
    /*  Private Helpers                                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Persists the Resume aggregate and ResumeVersion entity within a
     * single database transaction.
     *
     * <p>Uses {@link TransactionTemplate} for programmatic transaction
     * control, keeping the transaction scope strictly around DB operations
     * only — no external HTTP calls.</p>
     *
     * @param user              the authenticated user
     * @param originalFilename  the original filename of the uploaded file
     * @param contentType       the MIME type of the uploaded file
     * @param fileSize          the file size in bytes
     * @param checksum          the SHA-256 checksum of the file
     * @param uploadResult      the Cloudinary upload result (url + publicId)
     * @return the saved ResumeVersion entity
     */
    private ResumeVersion persistResumeVersion(User user,
                                               String originalFilename,
                                               String contentType,
                                               long fileSize,
                                               String checksum,
                                               CloudinaryUploadResult uploadResult) {

        return transactionTemplate.execute(status -> {
            // Step 5–6: Find or create Resume
            Resume resume = findOrCreateResume(user);

            // Step 7: Determine next version number
            int nextVersionNumber = resumeVersionRepository
                    .findTopByResumeOrderByVersionNumberDesc(resume)
                    .map(latest -> latest.getVersionNumber() + 1)
                    .orElse(1);

            // Step 8: Build ResumeVersion entity
            ResumeVersion version = ResumeVersion.builder()
                    .resume(resume)
                    .versionNumber(nextVersionNumber)
                    .originalFilename(originalFilename)
                    .storageProvider(StorageProvider.CLOUDINARY)
                    .fileSize(fileSize)
                    .mimeType(contentType)
                    .uploadedAt(LocalDateTime.now())
                    .parseStatus(ParseStatus.NOT_STARTED)
                    .storageUrl(uploadResult.url())
                    .cloudinaryPublicId(uploadResult.publicId())
                    .checksum(checksum)
                    .build();

            // Step 9: Save ResumeVersion
            ResumeVersion savedVersion = resumeVersionRepository.save(version);

            // Step 10: Update Resume.currentVersion
            resume.setCurrentVersion(savedVersion);

            // Step 11: Save Resume
            resumeRepository.save(resume);

            return savedVersion;
        });
    }

    /**
     * Finds the existing Resume for the user, or creates a new one if
     * this is the user's first upload.
     *
     * <p>The Resume aggregate is created lazily — only when the user
     * uploads their first resume. This avoids empty Resume rows in
     * the database.</p>
     *
     * @param user the authenticated user
     * @return the existing or newly created Resume entity
     */
    private Resume findOrCreateResume(User user) {
        return resumeRepository.findByUser(user)
                .orElseGet(() -> {
                    log.info("First upload for user: email={} — creating Resume aggregate",
                            user.getEmail());
                    Resume newResume = Resume.builder()
                            .user(user)
                            .build();
                    return resumeRepository.save(newResume);
                });
    }

    /**
     * Validates the uploaded resume file.
     *
     * <p>Validation checks:</p>
     * <ol>
     *     <li>File must not be null or empty</li>
     *     <li>MIME type must be {@code application/pdf}</li>
     *     <li>File size must not exceed 5MB</li>
     * </ol>
     *
     * @param file the uploaded file to validate
     * @throws InvalidResumeFileException if any validation check fails
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Invalid upload: file is empty or missing");
            throw new InvalidResumeFileException(
                    "Resume file is required and must not be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase(ALLOWED_CONTENT_TYPE)) {
            log.warn("Invalid upload: received contentType={}, expected={}",
                    contentType, ALLOWED_CONTENT_TYPE);
            throw new InvalidResumeFileException(
                    "Only PDF files are accepted. Received: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Invalid upload: size={} bytes exceeds maximum={} bytes",
                    file.getSize(), MAX_FILE_SIZE);
            throw new InvalidResumeFileException(
                    "File size exceeds the maximum allowed size of 5MB.");
        }
    }

    /**
     * Extracts the raw byte array from the {@link MultipartFile}.
     *
     * @param file the uploaded file
     * @return the file content as a byte array
     * @throws InvalidResumeFileException if the file cannot be read
     */
    private byte[] extractBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded file bytes: {}", e.getMessage(), e);
            throw new InvalidResumeFileException(
                    "Failed to read uploaded file. Please try again.");
        }
    }

    /**
     * Computes the SHA-256 hash of the given byte array.
     *
     * <p>Returns a 64-character lowercase hex string. Used for:</p>
     * <ul>
     *     <li>Duplicate detection (user feedback)</li>
     *     <li>Integrity verification on download</li>
     *     <li>Future deduplication optimization</li>
     * </ul>
     *
     * @param fileBytes the raw file content
     * @return the SHA-256 checksum as a hex string
     */
    private String computeChecksum(byte[] fileBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fileBytes);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available in every JVM
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

}
