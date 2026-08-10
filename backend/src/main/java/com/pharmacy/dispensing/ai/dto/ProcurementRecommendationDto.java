package com.pharmacy.dispensing.ai.dto;

public class ProcurementRecommendationDto {
    private Long medicineId;
    private String medicineName;
    private Integer currentUsableStock;
    private Integer reorderLevel;
    private Integer projected30DayDemand;
    private Integer safetyBuffer;
    private Integer recommendedOrderQuantity;
    private String urgency; // CRITICAL, WARNING, OPTIMAL

    public ProcurementRecommendationDto() {}

    public ProcurementRecommendationDto(Long medicineId, String medicineName, Integer currentUsableStock,
                                         Integer reorderLevel, Integer projected30DayDemand,
                                         Integer safetyBuffer, Integer recommendedOrderQuantity, String urgency) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.currentUsableStock = currentUsableStock;
        this.reorderLevel = reorderLevel;
        this.projected30DayDemand = projected30DayDemand;
        this.safetyBuffer = safetyBuffer;
        this.recommendedOrderQuantity = recommendedOrderQuantity;
        this.urgency = urgency;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Integer getCurrentUsableStock() { return currentUsableStock; }
    public void setCurrentUsableStock(Integer currentUsableStock) { this.currentUsableStock = currentUsableStock; }

    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }

    public Integer getProjected30DayDemand() { return projected30DayDemand; }
    public void setProjected30DayDemand(Integer projected30DayDemand) { this.projected30DayDemand = projected30DayDemand; }

    public Integer getSafetyBuffer() { return safetyBuffer; }
    public void setSafetyBuffer(Integer safetyBuffer) { this.safetyBuffer = safetyBuffer; }

    public Integer getRecommendedOrderQuantity() { return recommendedOrderQuantity; }
    public void setRecommendedOrderQuantity(Integer recommendedOrderQuantity) { this.recommendedOrderQuantity = recommendedOrderQuantity; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }
}
