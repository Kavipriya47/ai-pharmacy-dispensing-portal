package com.pharmacy.dispensing.reporting.dto;

import java.time.LocalDateTime;

public class AuditReportRow {
    private Long id;
    private String eventType;
    private String performedBy;
    private String description;
    private String metadata;
    private String ipAddress;
    private LocalDateTime createdAt;

    public AuditReportRow() {}

    public AuditReportRow(Long id, String eventType, String performedBy, String description, String metadata, String ipAddress, LocalDateTime createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.performedBy = performedBy;
        this.description = description;
        this.metadata = metadata;
        this.ipAddress = ipAddress;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
