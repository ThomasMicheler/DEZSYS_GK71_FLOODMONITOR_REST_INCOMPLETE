package com.example.floodmonitor.model;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Station {
    private final String id;
    private final String name;
    private final String river;
    private final Location location;
    private final double normalWaterLevel;   // cm
    private final double warningWaterLevel;  // cm
    private final double criticalWaterLevel; // cm
    private final boolean isActive;

    public Station(String id, String name, String river, Location location,
                   double normalWaterLevel, double warningWaterLevel,
                   double criticalWaterLevel, boolean isActive) {
        this.id = id;
        this.name = name;
        this.river = river;
        this.location = location;
        this.normalWaterLevel = normalWaterLevel;
        this.warningWaterLevel = warningWaterLevel;
        this.criticalWaterLevel = criticalWaterLevel;
        this.isActive = isActive;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRiver() { return river; }
    public Location getLocation() { return location; }
    public double getNormalWaterLevel() { return normalWaterLevel; }
    public double getWarningWaterLevel() { return warningWaterLevel; }
    public double getCriticalWaterLevel() { return criticalWaterLevel; }

    // Ohne @JsonProperty würde Jackson daraus "active" machen
    @JsonProperty("isActive")
    public boolean isActive() { return isActive; }
}