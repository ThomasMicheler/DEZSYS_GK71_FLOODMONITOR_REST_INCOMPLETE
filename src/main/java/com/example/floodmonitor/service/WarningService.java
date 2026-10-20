package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Service;

@Service
public class WarningService {

    /**
     * Berechnet die Warnstufe serverseitig basierend auf Station und Messung.
     */
    public WarningLevel calculate(Station station, Measurement measurement) {
        if (station == null || measurement == null || !station.isActive() || measurement.getStatus() == StationStatus.OFFLINE) {
            return WarningLevel.UNKNOWN;
        }

        return calculate(station, measurement.getWaterLevel());
    }

    /**
     * Berechnet die Warnstufe direkt anhand des Wasserstands.
     */
    public WarningLevel calculate(Station station, double waterLevel) {
        if (station == null || !station.isActive()) {
            return WarningLevel.UNKNOWN;
        }

        if (waterLevel >= station.getCriticalWaterLevel()) {
            return WarningLevel.CRITICAL;
        }
        if (waterLevel >= station.getWarningWaterLevel()) {
            return WarningLevel.WARNING;
        }
        return WarningLevel.NORMAL;
    }
}