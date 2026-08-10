package com.pharmacy.dispensing.reporting.dto;

import java.time.LocalDateTime;

public class RecallHistoryDto {
    private String batchNumber;
    private String medicineName;
    private LocalDateTime recallDate;
    private String recalledBy;
    private String reason;
    private long affectedDispensationCount;

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public LocalDateTime getRecallDate() {
        return recallDate;
    }

    public void setRecallDate(LocalDateTime recallDate) {
        this.recallDate = recallDate;
    }

    public String getRecalledBy() {
        return recalledBy;
    }

    public void setRecalledBy(String recalledBy) {
        this.recalledBy = recalledBy;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public long getAffectedDispensationCount() {
        return affectedDispensationCount;
    }

    public void setAffectedDispensationCount(long affectedDispensationCount) {
        this.affectedDispensationCount = affectedDispensationCount;
    }
}
