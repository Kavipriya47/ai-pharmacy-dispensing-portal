package com.pharmacy.dispensing.ai.service;

import com.pharmacy.dispensing.ai.client.MlForecastClient;
import com.pharmacy.dispensing.ai.dto.*;
import com.pharmacy.dispensing.dispensing.entity.DispensationRecord;
import com.pharmacy.dispensing.dispensing.entity.DispensationStatus;
import com.pharmacy.dispensing.dispensing.repository.DispensationRepository;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import com.pharmacy.dispensing.medicine.repository.MedicineRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiAssistanceService {

    private final MedicineRepository medicineRepository;
    private final DispensationRepository dispensationRepository;
    private final MlForecastClient mlForecastClient;
    private final ExpiryRiskService expiryRiskService;
    private final ProcurementRecommendationService procurementRecommendationService;

    public AiAssistanceService(MedicineRepository medicineRepository,
                               DispensationRepository dispensationRepository,
                               MlForecastClient mlForecastClient,
                               ExpiryRiskService expiryRiskService,
                               ProcurementRecommendationService procurementRecommendationService) {
        this.medicineRepository = medicineRepository;
        this.dispensationRepository = dispensationRepository;
        this.mlForecastClient = mlForecastClient;
        this.expiryRiskService = expiryRiskService;
        this.procurementRecommendationService = procurementRecommendationService;
    }

    public AiInsightsSummaryDto getInsightsSummary() {
        List<Medicine> activeMedicines = medicineRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActive()))
                .collect(Collectors.toList());

        List<DemandForecastDto> demandForecasts = new ArrayList<>();
        List<DispensationRecord> allRecords = dispensationRepository.findAll();

        for (Medicine med : activeMedicines) {
            // Group completed dispensations by LocalDate
            Map<LocalDate, Integer> dailyMap = allRecords.stream()
                    .filter(r -> r.getMedicine() != null && r.getMedicine().getId().equals(med.getId()))
                    .filter(r -> r.getStatus() == DispensationStatus.COMPLETED)
                    .filter(r -> r.getDispensedAt() != null)
                    .collect(Collectors.groupingBy(
                            r -> r.getDispensedAt().toLocalDate(),
                            Collectors.summingInt(DispensationRecord::getQuantityDispensed)
                    ));

            // Create continuous daily series for past 14 days (or dates present)
            LocalDate end = LocalDate.now();
            LocalDate start = dailyMap.keySet().stream().min(LocalDate::compareTo).orElse(end.minusDays(13));
            if (ChronoUnitDays(start, end) < 5) {
                start = end.minusDays(13);
            }

            List<Map<String, Object>> dailySeries = new ArrayList<>();
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                Map<String, Object> point = new HashMap<>();
                point.put("date", date.toString());
                point.put("quantity", dailyMap.getOrDefault(date, 0));
                dailySeries.add(point);
            }

            DemandForecastDto forecast = mlForecastClient.getDemandForecast(med.getId(), med.getName(), dailySeries);
            demandForecasts.add(forecast);
        }

        List<ExpiryWasteRiskDto> wasteRisks = expiryRiskService.evaluateWasteRisk(demandForecasts);
        List<ProcurementRecommendationDto> procurementRecs = procurementRecommendationService.generateRecommendations(demandForecasts);

        int totalUnitsAtRisk = wasteRisks.stream().mapToInt(r -> r.getUnitsAtRisk() != null ? r.getUnitsAtRisk() : 0).sum();
        int totalRecommendedOrders = procurementRecs.stream().mapToInt(p -> p.getRecommendedOrderQuantity() != null ? p.getRecommendedOrderQuantity() : 0).sum();

        boolean anyError = demandForecasts.stream().anyMatch(f -> "MODEL_ERROR".equals(f.getForecastStatus()));
        String status = anyError ? "DEGRADED" : "UP";

        return new AiInsightsSummaryDto(
                demandForecasts,
                wasteRisks,
                procurementRecs,
                totalUnitsAtRisk,
                totalRecommendedOrders,
                status
        );
    }

    private long ChronoUnitDays(LocalDate start, LocalDate end) {
        return java.time.temporal.ChronoUnit.DAYS.between(start, end);
    }
}
