package com.pharmacy.dispensing.inventory.entity;

/**
 * Lifecycle states of a {@link MedicineBatch}.
 * <ul>
 *   <li>{@code ACTIVE}      – available for dispensing</li>
 *   <li>{@code EXPIRED}     – past expiry date; auto-set by {@link com.pharmacy.dispensing.inventory.service.ExpiryCheckScheduler}</li>
 *   <li>{@code QUARANTINED} – held pending quality investigation</li>
 *   <li>{@code DEPLETED}    – quantity_remaining == 0</li>
 *   <li>{@code RECALLED}    – manufacturer / regulatory recall</li>
 * </ul>
 */
public enum BatchStatus {
    ACTIVE,
    EXPIRED,
    QUARANTINED,
    DEPLETED,
    RECALLED
}
