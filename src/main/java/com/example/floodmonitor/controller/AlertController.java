package com.example.floodmonitor.controller;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final StationRepository stationRepository;
    private final MeasurementRepository measurementRepository;

    public AlertController(StationRepository stationRepository, MeasurementRepository measurementRepository) {
        this.stationRepository = stationRepository;
        this.measurementRepository = measurementRepository;
    }

    @GetMapping
    public List<Measurement> getAlerts(
            @RequestParam(required = false) WarningLevel warningLevel,
            @RequestParam(required = false) String river) {

        List<Measurement> alerts = new ArrayList<>();

        stationRepository.findAll().stream()
                .filter(station -> river == null || station.getRiver().equalsIgnoreCase(river))
                .forEach(station -> {
                    Optional<Measurement> latest = measurementRepository.latest(station.getId());
                    if (latest.isPresent()) {
                        Measurement m = latest.get();
                        // Standardmäßig nur Warnungen und kritische Zustände anzeigen, es sei denn, warningLevel ist explizit gefiltert
                        boolean isAlert = m.getWarningLevel() == WarningLevel.WARNING || m.getWarningLevel() == WarningLevel.CRITICAL;

                        if (warningLevel != null) {
                            if (m.getWarningLevel() == warningLevel) {
                                alerts.add(m);
                            }
                        } else if (isAlert) {
                            alerts.add(m);
                        }
                    }
                });

        return alerts;
    }
}