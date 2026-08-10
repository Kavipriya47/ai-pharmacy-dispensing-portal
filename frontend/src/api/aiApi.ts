import apiClient from './axiosClient';

export interface DemandForecastDto {
  medicineId: number;
  medicineName: string;
  dailyDemandAverage: number;
  forecasted30DayDemand: number;
  trend: 'INCREASING' | 'DECREASING' | 'STABLE';
  trendSlope: number;
  r2Score: number;
  modelType: string;
  dataPointsCount: number;
  forecastStatus: 'MODEL_AVAILABLE' | 'INSUFFICIENT_DATA' | 'MODEL_ERROR';
}

export interface ExpiryWasteRiskDto {
  batchId: number;
  batchNumber: string;
  medicineName: string;
  quantityRemaining: number;
  expiryDate: string;
  daysUntilExpiry: number;
  predictedConsumptionBeforeExpiry: number;
  unitsAtRisk: number;
  riskLevel: 'HIGH' | 'MEDIUM' | 'LOW';
  recommendedAction: string;
}

export interface ProcurementRecommendationDto {
  medicineId: number;
  medicineName: string;
  currentUsableStock: number;
  reorderLevel: number;
  projected30DayDemand: number;
  safetyBuffer: number;
  recommendedOrderQuantity: number;
  urgency: 'CRITICAL' | 'WARNING' | 'OPTIMAL';
}

export interface AiInsightsSummaryDto {
  demandForecasts: DemandForecastDto[];
  expiryWasteRisks: ExpiryWasteRiskDto[];
  procurementRecommendations: ProcurementRecommendationDto[];
  totalUnitsAtRisk: number;
  totalRecommendedOrders: number;
  aiEngineStatus: 'UP' | 'DEGRADED' | 'DOWN';
}

export async function getAiInsightsSummary(): Promise<AiInsightsSummaryDto> {
  const response = await apiClient.get<AiInsightsSummaryDto>('/api/v1/ai/insights/summary');
  return response.data;
}
