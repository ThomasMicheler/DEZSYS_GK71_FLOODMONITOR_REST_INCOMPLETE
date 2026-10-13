package com.example.floodmonitor.model;

import java.time.Instant;

public class Measurement {
    private final String stationId;
    private final Instant timestamp;
    private final double waterLevel;   // cm
    private final double flowRate;     // m³/s
    private final double rainfall;     // mm/h
    private final double temperature;  // °C
    private final int batteryLevel;    // %
    private final StationStatus status;
    private final WarningLevel warningLevel;

    public Measurement(String stationId, Instant timestamp, double waterLevel, double flowRate, double rainfall, double temperature, int batteryLevel, StationStatus status, WarningLevel warningLevel) {
        this.stationId = stationId;
        this.timestamp = timestamp;
        this.waterLevel = waterLevel;
        this.flowRate = flowRate;
        this.rainfall = rainfall;
        this.temperature = temperature;
        this.batteryLevel = batteryLevel;
        this.status = status;
        this.warningLevel = warningLevel;
    }

    public String getStationId() { return stationId; }
    public Instant getTimestamp() { return timestamp; }
    public double getWaterLevel() { return waterLevel; }
    public double getFlowRate() { return flowRate; }
    public double getRainfall() { return rainfall; }
    public double getTemperature() { return temperature; }
    public int getBatteryLevel() { return batteryLevel; }
    public StationStatus getStatus() { return status; }
    public WarningLevel getWarningLevel() { return warningLevel; }
}