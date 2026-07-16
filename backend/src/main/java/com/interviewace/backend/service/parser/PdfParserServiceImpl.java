package com.interviewace.backend.service.parser;

import com.interviewace.backend.config.ParserProperties;
import com.interviewace.backend.entity.resume.ResumeVersion;
import com.interviewace.backend.enums.ParseFailureReason;
import com.interviewace.backend.enums.ParseStatus;
import com.interviewace.backend.exception.PdfParseException;
import com.interviewace.backend.repository.resume.ResumeVersionRepository;
import com.interviewace.backend.service.storage.CloudinaryService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Implementation of {@link PdfParserService} using Apache PDFBox 3.x.
 *
 * <p>Orchestrates the complete PDF parsing lifecycle:</p>
 * <ol>
 *     <li>Load the {@link ResumeVersion} entity from the database</li>
 *     <li>Apply idempotency guards (skip if COMPLETED or PENDING)</li>
 *     <li>Mark as {@code PENDING} and persist immediately</li>
 *     <li>Download PDF bytes via {@link CloudinaryService}</li>
 *     <li>Extract text using PDFBox {@link PDFTextStripper}</li>
 *     <li>Compute word count</li>
 *     <li>Mark as {@code COMPLETED} with parsed text and word count</li>
 * </ol>
 *
 * <p><b>Transaction design:</b> Follows the same pattern as
 * {@link com.interviewace.backend.service.resume.ResumeWorkflowServiceImpl} —
 * uses {@link TransactionTemplate} for programmatic transaction control.
 * The PDF download (external HTTP) and text extraction (CPU-bound) happen
 * <em>outside</em> any database transaction. Only status updates use
 * short-lived transactions.</p>
 *
 * <p><b>Error handling:</b> All exceptions are caught, classified into a
 * {@link ParseFailureReason}, and persisted to the database. Errors are
 * <em>never</em> propagated to the caller — this is a fire-and-forget service.</p>
 *
 * @see PdfParserService
 * @see CloudinaryService
 * @see ParseFailureReason
 */
@Service
public class PdfParserServiceImpl implements PdfParserService {

    private static final Logger log = LoggerFactory.getLogger(PdfParserServiceImpl.class);

    private final ResumeVersionRepository resumeVersionRepository;
    private final CloudinaryService cloudinaryService;
    private final TransactionTemplate transactionTemplate;
    private final ParserProperties parserProperties;

    /**
     * Constructor injection for all dependencies.
     *
     * @param resumeVersionRepository repository for loading and saving ResumeVersion entities
     * @param cloudinaryService       storage service for downloading PDF bytes
     * @param transactionTemplate     programmatic transaction manager
     * @param parserProperties        configurable parser settings (timeout, max size)
     */
    public PdfParserServiceImpl(ResumeVersionRepository resumeVersionRepository,
                                CloudinaryService cloudinaryService,
                                TransactionTemplate transactionTemplate,
                                ParserProperties parserProperties) {
        this.resumeVersionRepository = resumeVersionRepository;
        this.cloudinaryService = cloudinaryService;
        this.transactionTemplate = transactionTemplate;
        this.parserProperties = parserProperties;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This method is the main entry point for the parsing pipeline.
     * It handles the full lifecycle including idempotency checks,
     * status transitions, text extraction, and error classification.</p>
     *
     * <p>Errors are caught and persisted — never propagated. The caller
     * can safely invoke this method without try-catch.</p>
     */
    @Override
    public void parseResume(Long resumeVersionId) {
        log.info("Starting parse: versionId={}", resumeVersionId);

        // --- Step 1: Load ResumeVersion ---
        ResumeVersion version = loadResumeVersion(resumeVersionId);
        if (version == null) {
            return; // Not found — logged inside helper
        }

        // --- Step 2: Idempotency guard ---
        if (!isEligibleForParsing(version)) {
            return; // Already parsed or in progress — logged inside helper
        }

        // --- Step 3: Mark as PENDING ---
        markAsPending(version);

        try {
            // --- Step 4: Download PDF bytes (OUTSIDE transaction) ---
            byte[] pdfBytes = downloadPdf(version);

            // --- Step 5: Extract text (OUTSIDE transaction) ---
            String extractedText = extractText(pdfBytes);

            // --- Step 6: Check for empty text ---
            if (extractedText == null || extractedText.isBlank()) {
                log.warn("Empty text extracted: versionId={}", resumeVersionId);
                markAsFailed(version,
                        ParseFailureReason.EMPTY_TEXT,
                        "PDF contains no extractable text. The document may be scanned or image-only.");
                return;
            }

            // --- Step 7: Compute word count ---
            int wordCount = computeWordCount(extractedText);

            // --- Step 8: Mark as COMPLETED ---
            markAsCompleted(version, extractedText, wordCount);

            log.info("Parse completed: versionId={}, wordCount={}, textLength={}",
                    resumeVersionId, wordCount, extractedText.length());

        } catch (PdfParseException e) {
            // Classified parsing exception — persist the structured failure
            log.error("Parse failed: versionId={}, reason={}, error={}",
                    resumeVersionId, e.getFailureReason(), e.getMessage(), e);
            markAsFailed(version, e.getFailureReason(), e.getMessage());

        } catch (Exception e) {
            // Unclassified exception — catch-all safety net
            log.error("Parse failed (unknown): versionId={}, error={}",
                    resumeVersionId, e.getMessage(), e);
            markAsFailed(version, ParseFailureReason.UNKNOWN, e.getMessage());
        }
    }

    /* ------------------------------------------------------------------ */
    /*  Private Helpers                                                     */
    /* ------------------------------------------------------------------ */

    /**
     * Loads a {@link ResumeVersion} entity by ID within a read transaction.
     *
     * @param resumeVersionId the ID to look up
     * @return the entity, or {@code null} if not found
     */
    private ResumeVersion loadResumeVersion(Long resumeVersionId) {
        return transactionTemplate.execute(status -> {
            var optionalVersion = resumeVersionRepository.findById(resumeVersionId);
            if (optionalVersion.isEmpty()) {
                log.error("ResumeVersion not found: id={}", resumeVersionId);
                return null;
            }
            return optionalVersion.get();
        });
    }

    /**
     * Checks if the resume version is eligible for parsing based on its current status.
     *
     * <p>Eligible: {@code NOT_STARTED}, {@code FAILED} (retry).
     * Not eligible: {@code COMPLETED}, {@code PENDING}.</p>
     *
     * @param version the resume version to check
     * @return {@code true} if parsing should proceed
     */
    private boolean isEligibleForParsing(ResumeVersion version) {
        ParseStatus currentStatus = version.getParseStatus();

        switch (currentStatus) {
            case COMPLETED:
                log.info("Skipping parse (already completed): versionId={}", version.getId());
                return false;

            case PENDING:
                log.warn("Skipping parse (already in progress): versionId={}", version.getId());
                return false;

            case NOT_STARTED:
            case FAILED:
                log.info("Parse eligible: versionId={}, currentStatus={}",
                        version.getId(), currentStatus);
                return true;

            default:
                log.warn("Unknown parse status: versionId={}, status={}",
                        version.getId(), currentStatus);
                return false;
        }
    }

    /**
     * Transitions the version to {@code PENDING} status and persists immediately.
     *
     * <p>Resets all parse-related fields to ensure a clean slate for
     * retries. The status is saved in its own short-lived transaction
     * so that API queries can see the {@code PENDING} state.</p>
     *
     * @param version the resume version to update
     */
    private void markAsPending(ResumeVersion version) {
        transactionTemplate.executeWithoutResult(status -> {
            version.setParseStatus(ParseStatus.PENDING);
            version.setParseStartedAt(LocalDateTime.now());
            version.setParseCompletedAt(null);
            version.setParsedText(null);
            version.setWordCount(null);
            version.setParseFailureReason(null);
            version.setParseErrorMessage(null);
            resumeVersionRepository.save(version);
        });

        log.info("Marked as PENDING: versionId={}", version.getId());
    }

    /**
     * Downloads the PDF bytes from cloud storage.
     *
     * <p>Validates the file size against the configured maximum before returning.
     * Wraps download failures in a {@link PdfParseException} with
     * {@link ParseFailureReason#NETWORK}.</p>
     *
     * @param version the resume version containing the storage URL
     * @return the PDF file bytes
     * @throws PdfParseException with {@code NETWORK} reason on download failure
     */
    private byte[] downloadPdf(ResumeVersion version) {
        try {
            byte[] pdfBytes = cloudinaryService.downloadResume(version.getStorageUrl());

            // Safety check: ensure file size is within parser limits
            if (pdfBytes.length > parserProperties.getMaxParseSizeBytes()) {
                throw new PdfParseException(
                        ParseFailureReason.CORRUPTED_FILE,
                        String.format("PDF size (%d bytes) exceeds parser limit (%d bytes)",
                                pdfBytes.length, parserProperties.getMaxParseSizeBytes()));
            }

            return pdfBytes;

        } catch (PdfParseException e) {
            throw e; // Re-throw classified exceptions
        } catch (Exception e) {
            throw new PdfParseException(
                    ParseFailureReason.NETWORK,
                    "Failed to download PDF from storage: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts text from PDF bytes using Apache PDFBox 3.x.
     *
     * <p>Uses {@link Loader#loadPDF(byte[])} (PDFBox 3.x API) and
     * {@link PDFTextStripper} to extract all text from all pages.</p>
     *
     * <p>Classifies PDFBox exceptions into structured failure reasons:</p>
     * <ul>
     *     <li>{@link InvalidPasswordException} → {@code ENCRYPTED}</li>
     *     <li>{@link IOException} → {@code CORRUPTED_FILE}</li>
     * </ul>
     *
     * @param pdfBytes the raw PDF file content
     * @return the extracted text (may be empty for image-only PDFs)
     * @throws PdfParseException with a classified failure reason
     */
    private String extractText(byte[] pdfBytes) {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            log.debug("Text extracted: pages={}, length={}",
                    document.getNumberOfPages(),
                    text != null ? text.length() : 0);

            return text;

        } catch (InvalidPasswordException e) {
            throw new PdfParseException(
                    ParseFailureReason.ENCRYPTED,
                    "PDF is password-protected or encrypted: " + e.getMessage(), e);

        } catch (IOException e) {
            throw new PdfParseException(
                    ParseFailureReason.CORRUPTED_FILE,
                    "Failed to parse PDF — file may be corrupted: " + e.getMessage(), e);
        }
    }

    /**
     * Computes the word count of the extracted text.
     *
     * <p>Splits on whitespace boundaries and counts non-empty tokens.
     * Returns 0 for null or blank input.</p>
     *
     * @param text the extracted text
     * @return the number of words
     */
    private int computeWordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.split("\\s+").length;
    }

    /**
     * Transitions the version to {@code COMPLETED} status with extracted content.
     *
     * <p>Persists the parsed text, word count, and completion timestamp
     * in a single short-lived transaction.</p>
     *
     * @param version   the resume version to update
     * @param text      the extracted text content
     * @param wordCount the computed word count
     */
    private void markAsCompleted(ResumeVersion version, String text, int wordCount) {
        transactionTemplate.executeWithoutResult(status -> {
            version.setParseStatus(ParseStatus.COMPLETED);
            version.setParsedText(text);
            version.setWordCount(wordCount);
            version.setParseCompletedAt(LocalDateTime.now());
            version.setParseFailureReason(null);
            version.setParseErrorMessage(null);
            resumeVersionRepository.save(version);
        });
    }

    /**
     * Transitions the version to {@code FAILED} status with failure details.
     *
     * <p>Persists the failure reason (enum), error message (detail),
     * and completion timestamp in a single short-lived transaction.</p>
     *
     * @param version       the resume version to update
     * @param failureReason the structured failure category
     * @param errorMessage  the detailed error description
     */
    private void markAsFailed(ResumeVersion version,
                              ParseFailureReason failureReason,
                              String errorMessage) {
        try {
            // Truncate error message to fit the database column (1000 chars)
            String truncatedMessage = errorMessage;
            if (truncatedMessage != null && truncatedMessage.length() > 1000) {
                truncatedMessage = truncatedMessage.substring(0, 997) + "...";
            }

            String finalMessage = truncatedMessage;
            transactionTemplate.executeWithoutResult(status -> {
                version.setParseStatus(ParseStatus.FAILED);
                version.setParseFailureReason(failureReason);
                version.setParseErrorMessage(finalMessage);
                version.setParseCompletedAt(LocalDateTime.now());
                version.setParsedText(null);
                version.setWordCount(null);
                resumeVersionRepository.save(version);
            });

            log.info("Marked as FAILED: versionId={}, reason={}", version.getId(), failureReason);

        } catch (Exception e) {
            // Critical: if we can't even save the FAILED status, log it
            log.error("CRITICAL — Failed to persist FAILED status: versionId={}, error={}",
                    version.getId(), e.getMessage(), e);
        }
    }
}
