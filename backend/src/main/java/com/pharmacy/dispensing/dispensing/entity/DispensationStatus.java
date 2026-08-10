package com.pharmacy.dispensing.dispensing.entity;

/**
 * Lifecycle status of a {@link DispensationRecord}.
 * <ul>
 *   <li>{@code COMPLETED} – medication was successfully dispensed</li>
 *   <li>{@code CANCELLED} – dispensation was cancelled (e.g., patient left)</li>
 * </ul>
 */
public enum DispensationStatus {
    COMPLETED,
    CANCELLED
}
