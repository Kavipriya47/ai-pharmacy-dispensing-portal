package com.pharmacy.dispensing.reporting.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Summary statistics for dispensing operations.
 * Note: completed and cancelled counts are derived from {@link com.pharmacy.dispensing.dispensing.entity.DispensationRecord}
 * with status = COMPLETED / CANCELLED. failedCount is derived from audit events of type DISPENSE_FAILED.
 * These categories are NOT mutually exclusive because a failed attempt does not create a DispensationRecord.
 */
public class DispensingStatsDto {
    private long completedCount;
    private long cancelledCount;
    private long failedCount;
    private long totalQuantityDispensed;
    private String semanticsNote = "completed/cancelled counts come from DispensationRecord.status; failedCount reflects DISPENSE_FAILED audit events (no DispensationRecord).";

    // getters & setters
    public long getCompletedCount() { return completedCount; }
    public void setCompletedCount(long completedCount) { this.completedCount = completedCount; }
    public long getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(long cancelledCount) { this.cancelledCount = cancelledCount; }
    public long getFailedCount() { return failedCount; }
    public void setFailedCount(long failedCount) { this.failedCount = failedCount; }
    public long getTotalQuantityDispensed() { return totalQuantityDispensed; }
    public void setTotalQuantityDispensed(long totalQuantityDispensed) { this.totalQuantityDispensed = totalQuantityDispensed; }
    public String getSemanticsNote() { return semanticsNote; }
    public void setSemanticsNote(String semanticsNote) { this.semanticsNote = semanticsNote; }
}
