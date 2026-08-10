package com.pharmacy.dispensing.ai.service;

import com.pharmacy.dispensing.ai.dto.DemandForecastDto;
import com.pharmacy.dispensing.ai.dto.ExpiryWasteRiskDto;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.MedicineBatch;
import com.pharmacy.dispensing.inventory.repository.MedicineBatchRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpiryRiskService {

    private final MedicineBatchRepository batchRepository;

    public ExpiryRiskService(MedicineBatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public List<ExpiryWasteRiskDto> evaluateWasteRisk(List<DemandForecastDto> forecasts) {
        List<ExpiryWasteRiskDto> risks = new ArrayList<>();
        LocalDate today = LocalDate.now();

        List<MedicineBatch> activeBatches = batchRepository.findByStatus(BatchStatus.ACTIVE, Pageable.unpaged()).getContent();

        for (MedicineBatch batch : activeBatches) {
            if (batch.getExpiryDate() == null || batch.getExpiryDate().isBefore(today)) {
                continue;
            }

            Long medicineId = batch.getInventory().getMedicine().getId();
            DemandForecastDto forecast = forecasts.stream()
                    .filter(f -> f.getMedicineId().equals(medicineId))
                    .findFirst()
                    .orElse(null);

            double dailyDemand;
            if (forecast != null && "MODEL_AVAILABLE".equals(forecast.getForecastStatus()) && forecast.getForecasted30DayDemand() != null) {
                dailyDemand = Math.max(0.1, forecast.getForecasted30DayDemand() / 30.0);
            } else {
                dailyDemand = (forecast != null && forecast.getDailyDemandAverage() != null)
                        ? Math.max(0.1, forecast.getDailyDemandAverage())
                        : 1.0;
            }

            long daysUntilExpiry = ChronoUnit.DAYS.between(today, batch.getExpiryDate());
            int predictedConsumption = (int) Math.round(dailyDemand * daysUntilExpiry);
            int remaining = batch.getQuantityRemaining();
            int unitsAtRisk = Math.max(0, remaining - predictedConsumption);

            String riskLevel;
            String action;
            if (unitsAtRisk > 30 || (daysUntilExpiry <= 60 && unitsAtRisk > 0)) {
                riskLevel = "HIGH";
                action = "CRITICAL: Prioritize FEFO dispensing immediately or arrange supplier return/discount.";
            } else if (unitsAtRisk > 0 || daysUntilExpiry <= 90) {
                riskLevel = "MEDIUM";
                action = "WARNING: Monitor dispensing velocity; ensure FEFO enforcement.";
            } else {
                riskLevel = "LOW";
                action = "OPTIMAL: Expected to be fully consumed prior to expiration.";
            }

            risks.add(new ExpiryWasteRiskDto(
                    batch.getId(),
                    batch.getBatchNumber(),
                    batch.getInventory().getMedicine().getName(),
                    remaining,
                    batch.getExpiryDate(),
                    daysUntilExpiry,
                    predictedConsumption,
                    unitsAtRisk,
                    riskLevel,
                    action
            ));
        }

        return risks;
    }
}
