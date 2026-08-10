from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.metrics import r2_score

app = FastAPI(
    title="PharmaCare AI ML Microservice",
    description="Scikit-Learn Linear Regression Demand Forecasting Engine",
    version="1.0.0"
)

class DailyRecord(BaseModel):
    date: str
    quantity: int

class DemandPredictRequest(BaseModel):
    medicine_id: int
    medicine_name: str
    daily_series: List[DailyRecord]

class DemandPredictResponse(BaseModel):
    medicine_id: int
    medicine_name: str
    daily_demand_average: float
    forecasted_30_day_demand: int
    trend: str
    trend_slope: float
    r2_score: float
    model_type: str
    data_points_count: int
    forecast_status: str

@app.get("/health")
def health_check():
    return {"status": "UP", "service": "PharmaCare Scikit-Learn ML Engine"}

@app.post("/predict/demand", response_model=DemandPredictResponse)
def predict_demand(req: DemandPredictRequest):
    series = req.daily_series
    count = len(series)
    
    if count < 3:
        return DemandPredictResponse(
            medicine_id=req.medicine_id,
            medicine_name=req.medicine_name,
            daily_demand_average=round(float(np.mean([item.quantity for item in series])) if count > 0 else 0.0, 2),
            forecasted_30_day_demand=sum(item.quantity for item in series) * 3 if count > 0 else 0,
            trend="STABLE",
            trend_slope=0.0,
            r2_score=0.0,
            model_type="Scikit-Learn Linear Regression",
            data_points_count=count,
            forecast_status="INSUFFICIENT_DATA"
        )
    
    # Prepare DataFrame
    df = pd.DataFrame([item.dict() for item in series])
    df['day_index'] = np.arange(len(df))
    
    X = df[['day_index']].values
    y = df['quantity'].values
    
    # Fit Linear Regression Model
    model = LinearRegression()
    model.fit(X, y)
    
    # Predict next 30 days
    last_day = len(df) - 1
    future_X = np.arange(last_day + 1, last_day + 31).reshape(-1, 1)
    future_pred = model.predict(future_X)
    
    # Clip negative predictions to zero and bound maximum predictions
    current_max_daily = float(np.max(y))
    future_pred_clipped = np.clip(future_pred, a_min=0, a_max=max(daily_avg * 3, current_max_daily))
    forecast_30_days = int(round(np.sum(future_pred_clipped)))
    
    daily_avg = float(np.mean(y))
    slope = float(model.coef_[0])
    
    if slope > 0.05:
        trend = "INCREASING"
    elif slope < -0.05:
        trend = "DECREASING"
    else:
        trend = "STABLE"
        
    y_pred_hist = model.predict(X)
    score = float(r2_score(y, y_pred_hist))
    if np.isnan(score):
        score = 0.0
        
    return DemandPredictResponse(
        medicine_id=req.medicine_id,
        medicine_name=req.medicine_name,
        daily_demand_average=round(daily_avg, 2),
        forecasted_30_day_demand=forecast_30_days,
        trend=trend,
        trend_slope=round(slope, 4),
        r2_score=round(score, 4),
        model_type="Scikit-Learn Linear Regression with bounded extrapolation",
        data_points_count=count,
        forecast_status="MODEL_AVAILABLE"
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
