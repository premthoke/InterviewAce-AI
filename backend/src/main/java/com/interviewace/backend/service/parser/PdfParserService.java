package com.interviewace.backend.service.parser;

/**
 * Defines the PDF text extraction pipeline for resume versions.
 *
 * <p>This service orchestrates the complete parsing lifecycle:</p>
 * <ol>
 *     <li>Load the {@code ResumeVersion} entity by ID</li>
 *     <li>Download the PDF bytes from cloud storage</li>
 *     <li>Extract text using Apache PDFBox</li>
 *     <li>Compute word count</li>
 *     <li>Persist the extracted text, word count, and parse status</li>
 * </ol>
 *
 * <p>This is an <b>infrastructure service</b> — it knows how to parse PDFs
 * but does not know about upload workflows, controllers, or AI analysis.
 * It is invoked by the workflow layer after a resume upload succeeds.</p>
 *
 * <p>The service accepts a {@code Long} version ID (not an entity) to
 * maintain a clean boundary that supports future async processing.
 * Internally, it loads a fresh entity within its own transaction.</p>
 *
 * <p>Error handling follows a fire-and-forget pattern: parsing failures
 * are caught internally and persisted as {@code FAILED} status with a
 * structured {@link com.interviewace.backend.enums.ParseFailureReason}.
 * The calling code is never forced to handle parsing exceptions.</p>
 *
 * @see PdfParserServiceImpl
 * @see com.interviewace.backend.enums.ParseStatus
 * @see com.interviewace.backend.enums.ParseFailureReason
 */
public interface PdfParserService {

    /**
     * Parses a resume version's PDF and stores the extracted text.
     *
     * <p>Idempotency guards:</p>
     * <ul>
     *     <li>{@code COMPLETED} — returns immediately (already parsed)</li>
     *     <li>{@code PENDING} — returns immediately (parse in progress)</li>
     *     <li>{@code NOT_STARTED} — proceeds with parsing</li>
     *     <li>{@code FAILED} — proceeds with retry (resets all parse fields)</li>
     * </ul>
     *
     * <p>On success: sets {@code parseStatus = COMPLETED}, stores
     * {@code parsedText} and {@code wordCount}.</p>
     *
     * <p>On failure: sets {@code parseStatus = FAILED}, stores
     * {@code parseFailureReason} and {@code parseErrorMessage}.
     * Does <b>not</b> throw — errors are persisted, not propagated.</p>
     *
     * @param resumeVersionId the ID of the ResumeVersion to parse
     */
    void parseResume(Long resumeVersionId);
}
