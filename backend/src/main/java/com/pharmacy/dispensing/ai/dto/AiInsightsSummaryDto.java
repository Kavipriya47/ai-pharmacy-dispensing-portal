package com.pharmacy.dispensing.ai.dto;

import java.util.List;

public class AiInsightsSummaryDto {
    private List<DemandForecastDto> demandForecasts;
    private List<ExpiryWasteRiskDto> expiryWasteRisks;
    private List<ProcurementRecommendationDto> procurementRecommendations;
    private Integer totalUnitsAtRisk;
    private Integer totalRecommendedOrders;
    private String aiEngineStatus; // UP, DEGRADED, DOWN

    public AiInsightsSummaryDto() {}

    public AiInsightsSummaryDto(List<DemandForecastDto> demandForecasts,
                                List<ExpiryWasteRiskDto> expiryWasteRisks,
                                List<ProcurementRecommendationDto> procurementRecommendations,
                                Integer totalUnitsAtRisk, Integer totalRecommendedOrders,
                                String aiEngineStatus) {
        this.demandForecasts = demandForecasts;
        this.expiryWasteRisks = expiryWasteRisks;
        this.procurementRecommendations = procurementRecommendations;
        this.totalUnitsAtRisk = totalUnitsAtRisk;
        this.totalRecommendedOrders = totalRecommendedOrders;
        this.aiEngineStatus = aiEngineStatus;
    }

    public List<DemandForecastDto> getDemandForecasts() { return demandForecasts; }
    public void setDemandForecasts(List<DemandForecastDto> demandForecasts) { this.demandForecasts = demandForecasts; }

    public List<ExpiryWasteRiskDto> getExpiryWasteRisks() { return expiryWasteRisks; }
    public void setExpiryWasteRisks(List<ExpiryWasteRiskDto> expiryWasteRisks) { this.expiryWasteRisks = expiryWasteRisks; }

    public List<ProcurementRecommendationDto> getProcurementRecommendations() { return procurementRecommendations; }
    public void setProcurementRecommendations(List<ProcurementRecommendationDto> procurementRecommendations) { this.procurementRecommendations = procurementRecommendations; }

    public Integer getTotalUnitsAtRisk() { return totalUnitsAtRisk; }
    public void setTotalUnitsAtRisk(Integer totalUnitsAtRisk) { this.totalUnitsAtRisk = totalUnitsAtRisk; }

    public Integer getTotalRecommendedOrders() { return totalRecommendedOrders; }
    public void setTotalRecommendedOrders(Integer totalRecommendedOrders) { this.totalRecommendedOrders = totalRecommendedOrders; }

    public String getAiEngineStatus() { return aiEngineStatus; }
    public void setAiEngineStatus(String aiEngineStatus) { this.aiEngineStatus = aiEngineStatus; }
}
