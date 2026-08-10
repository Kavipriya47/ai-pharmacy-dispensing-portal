package com.pharmacy.dispensing.ai.dto;

import java.time.LocalDate;

public class ExpiryWasteRiskDto {
    private Long batchId;
    private String batchNumber;
    private String medicineName;
    private Integer quantityRemaining;
    private LocalDate expiryDate;
    private Long daysUntilExpiry;
    private Integer predictedConsumptionBeforeExpiry;
    private Integer unitsAtRisk;
    private String riskLevel; // HIGH, MEDIUM, LOW
    private String recommendedAction;

    public ExpiryWasteRiskDto() {}

    public ExpiryWasteRiskDto(Long batchId, String batchNumber, String medicineName,
                              Integer quantityRemaining, LocalDate expiryDate, Long daysUntilExpiry,
                              Integer predictedConsumptionBeforeExpiry, Integer unitsAtRisk,
                              String riskLevel, String recommendedAction) {
        this.batchId = batchId;
        this.batchNumber = batchNumber;
        this.medicineName = medicineName;
        this.quantityRemaining = quantityRemaining;
        this.expiryDate = expiryDate;
        this.daysUntilExpiry = daysUntilExpiry;
        this.predictedConsumptionBeforeExpiry = predictedConsumptionBeforeExpiry;
        this.unitsAtRisk = unitsAtRisk;
        this.riskLevel = riskLevel;
        this.recommendedAction = recommendedAction;
    }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getQuantityRemaining() { return quantityRemaining; }
    public void setQuantityRemaining(Integer quantityRemaining) { this.quantityRemaining = quantityRemaining; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Long getDaysUntilExpiry() { return daysUntilExpiry; }
    public void setDaysUntilExpiry(Long daysUntilExpiry) { this.daysUntilExpiry = daysUntilExpiry; }

    public Integer getPredictedConsumptionBeforeExpiry() { return predictedConsumptionBeforeExpiry; }
    public void setPredictedConsumptionBeforeExpiry(Integer predictedConsumptionBeforeExpiry) { this.predictedConsumptionBeforeExpiry = predictedConsumptionBeforeExpiry; }

    public Integer getUnitsAtRisk() { return unitsAtRisk; }
    public void setUnitsAtRisk(Integer unitsAtRisk) { this.unitsAtRisk = unitsAtRisk; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
}
