package com.pharmacy.dispensing.ai.dto;

public class DemandForecastDto {
    private Long medicineId;
    private String medicineName;
    private Double dailyDemandAverage;
    private Integer forecasted30DayDemand;
    private String trend; // INCREASING, DECREASING, STABLE
    private Double trendSlope;
    private Double r2Score;
    private String modelType;
    private Integer dataPointsCount;
    private String forecastStatus; // MODEL_AVAILABLE, INSUFFICIENT_DATA, MODEL_ERROR

    public DemandForecastDto() {}

    public DemandForecastDto(Long medicineId, String medicineName, Double dailyDemandAverage,
                             Integer forecasted30DayDemand, String trend, Double trendSlope,
                             Double r2Score, String modelType, Integer dataPointsCount, String forecastStatus) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dailyDemandAverage = dailyDemandAverage;
        this.forecasted30DayDemand = forecasted30DayDemand;
        this.trend = trend;
        this.trendSlope = trendSlope;
        this.r2Score = r2Score;
        this.modelType = modelType;
        this.dataPointsCount = dataPointsCount;
        this.forecastStatus = forecastStatus;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public Double getDailyDemandAverage() { return dailyDemandAverage; }
    public void setDailyDemandAverage(Double dailyDemandAverage) { this.dailyDemandAverage = dailyDemandAverage; }

    public Integer getForecasted30DayDemand() { return forecasted30DayDemand; }
    public void setForecasted30DayDemand(Integer forecasted30DayDemand) { this.forecasted30DayDemand = forecasted30DayDemand; }

    public String getTrend() { return trend; }
    public void setTrend(String trend) { this.trend = trend; }

    public Double getTrendSlope() { return trendSlope; }
    public void setTrendSlope(Double trendSlope) { this.trendSlope = trendSlope; }

    public Double getR2Score() { return r2Score; }
    public void setR2Score(Double r2Score) { this.r2Score = r2Score; }

    public String getModelType() { return modelType; }
    public void setModelType(String modelType) { this.modelType = modelType; }

    public Integer getDataPointsCount() { return dataPointsCount; }
    public void setDataPointsCount(Integer dataPointsCount) { this.dataPointsCount = dataPointsCount; }

    public String getForecastStatus() { return forecastStatus; }
    public void setForecastStatus(String forecastStatus) { this.forecastStatus = forecastStatus; }
}
