package com.pharmacy.dispensing.ai.client;

import com.pharmacy.dispensing.ai.dto.DemandForecastDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MlForecastClient {

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public DemandForecastDto getDemandForecast(Long medicineId, String medicineName, List<Map<String, Object>> dailySeries) {
        try {
            String endpoint = aiServiceUrl + "/predict/demand";
            Map<String, Object> request = new HashMap<>();
            request.put("medicine_id", medicineId);
            request.put("medicine_name", medicineName);
            request.put("daily_series", dailySeries);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);
            if (response == null) {
                return fallbackForecast(medicineId, medicineName, dailySeries, "MODEL_ERROR");
            }

            double dailyDemandAvg = response.get("daily_demand_average") != null ? ((Number) response.get("daily_demand_average")).doubleValue() : 0.0;
            int forecasted30 = response.get("forecasted_30_day_demand") != null ? ((Number) response.get("forecasted_30_day_demand")).intValue() : 0;
            String trend = response.get("trend") != null ? response.get("trend").toString() : "STABLE";
            double slope = response.get("trend_slope") != null ? ((Number) response.get("trend_slope")).doubleValue() : 0.0;
            double r2 = response.get("r2_score") != null ? ((Number) response.get("r2_score")).doubleValue() : 0.0;
            String modelType = response.get("model_type") != null ? response.get("model_type").toString() : "Scikit-Learn Linear Regression";
            int count = response.get("data_points_count") != null ? ((Number) response.get("data_points_count")).intValue() : 0;
            String status = response.get("forecast_status") != null ? response.get("forecast_status").toString() : "MODEL_AVAILABLE";

            return new DemandForecastDto(
                    medicineId,
                    medicineName,
                    dailyDemandAvg,
                    forecasted30,
                    trend,
                    slope,
                    r2,
                    modelType,
                    count,
                    status
            );
        } catch (Exception e) {
            return fallbackForecast(medicineId, medicineName, dailySeries, "MODEL_ERROR");
        }
    }

    private DemandForecastDto fallbackForecast(Long medicineId, String medicineName, List<Map<String, Object>> dailySeries, String status) {
        double avg = dailySeries.stream()
                .mapToDouble(m -> ((Number) m.getOrDefault("quantity", 0)).doubleValue())
                .average()
                .orElse(0.0);
        int projected30 = (int) Math.round(avg * 30);
        return new DemandForecastDto(
                medicineId,
                medicineName,
                Math.round(avg * 100.0) / 100.0,
                projected30,
                "STABLE",
                0.0,
                0.0,
                "Historical Moving Average (Fallback)",
                dailySeries.size(),
                status
        );
    }
}
