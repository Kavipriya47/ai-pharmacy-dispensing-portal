package com.pharmacy.dispensing.ai.service;

import com.pharmacy.dispensing.ai.dto.DemandForecastDto;
import com.pharmacy.dispensing.ai.dto.ProcurementRecommendationDto;
import com.pharmacy.dispensing.inventory.entity.BatchStatus;
import com.pharmacy.dispensing.inventory.entity.Inventory;
import com.pharmacy.dispensing.inventory.repository.InventoryRepository;
import com.pharmacy.dispensing.medicine.entity.Medicine;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProcurementRecommendationService {

    private final InventoryRepository inventoryRepository;

    public ProcurementRecommendationService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<ProcurementRecommendationDto> generateRecommendations(List<DemandForecastDto> forecasts) {
        List<ProcurementRecommendationDto> recommendations = new ArrayList<>();
        List<Inventory> inventoryList = inventoryRepository.findAll(Pageable.unpaged()).getContent();
        LocalDate today = LocalDate.now();

        for (Inventory inv : inventoryList) {
            Medicine med = inv.getMedicine();
            if (med == null || Boolean.FALSE.equals(med.getActive())) {
                continue;
            }

            // Calculate USABLE stock: ACTIVE status & expiryDate > TODAY
            int usableStock = inv.getBatches().stream()
                    .filter(b -> b.getStatus() == BatchStatus.ACTIVE)
                    .filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isAfter(today))
                    .mapToInt(b -> b.getQuantityRemaining())
                    .sum();

            DemandForecastDto forecast = forecasts.stream()
                    .filter(f -> f.getMedicineId().equals(med.getId()))
                    .findFirst()
                    .orElse(null);

            int projected30Demand = (forecast != null && forecast.getForecasted30DayDemand() != null)
                    ? forecast.getForecasted30DayDemand()
                    : 30;

            int safetyBuffer = (int) Math.ceil(projected30Demand * 0.10); // 10% safety buffer
            int reorderLevel = med.getReorderLevel() != null ? med.getReorderLevel() : 20;

            int recommendedOrder = Math.max(0, (projected30Demand + safetyBuffer) - usableStock);
            if (usableStock <= reorderLevel) {
                recommendedOrder = Math.max(recommendedOrder, reorderLevel - usableStock);
            }

            String urgency;
            if (usableStock <= reorderLevel) {
                urgency = "CRITICAL";
            } else if (usableStock < projected30Demand) {
                urgency = "WARNING";
            } else {
                urgency = "OPTIMAL";
            }

            recommendations.add(new ProcurementRecommendationDto(
                    med.getId(),
                    med.getName(),
                    usableStock,
                    reorderLevel,
                    projected30Demand,
                    safetyBuffer,
                    recommendedOrder,
                    urgency
            ));
        }

        return recommendations;
    }
}
