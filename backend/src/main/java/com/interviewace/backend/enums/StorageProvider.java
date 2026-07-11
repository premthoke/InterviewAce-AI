package com.interviewace.backend.enums;

/**
 * Enumeration representing the storage backend used for resume file persistence.
 *
 * <p>Defines where the physical resume file is stored. This allows the system
 * to support multiple storage strategies and migrate between them transparently.</p>
 *
 * <ul>
 *     <li>{@link #LOCAL} — file stored on the local filesystem</li>
 *     <li>{@link #CLOUDINARY} — file stored in Cloudinary cloud storage</li>
 * </ul>
 */
public enum StorageProvider {

    /**
     * Resume file is stored on the local server filesystem.
     */
    LOCAL,

    /**
     * Resume file is stored in Cloudinary cloud storage.
     */
    CLOUDINARY
}
