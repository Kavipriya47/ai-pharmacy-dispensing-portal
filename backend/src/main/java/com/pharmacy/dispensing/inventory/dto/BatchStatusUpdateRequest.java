package com.pharmacy.dispensing.inventory.dto;

import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for updating a batch's status (QUARANTINE, RECALL, etc.).
 * Only ADMIN-authorized users may call this endpoint.
 */
public class BatchStatusUpdateRequest {

    @NotNull(message = "status is required")
    private BatchStatus status;

    /** Mandatory reason for status changes that remove stock availability. */
    private String reason;

    // ---- Getters & Setters ----

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
