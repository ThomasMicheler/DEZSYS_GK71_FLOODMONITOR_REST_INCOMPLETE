package com.example.floodmonitor.controller;

import com.example.floodmonitor.exception.BadRequestException;
import com.example.floodmonitor.exception.ResourceNotFoundException;
import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.WarningLevel;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {

    private final StationRepository stations;
    private final MeasurementRepository measurements;

    public StationController(StationRepository stations, MeasurementRepository measurements) {
        this.stations = stations;
        this.measurements = measurements;
    }

    @GetMapping
    public List<Station> getAll(
            @RequestParam(required = false) String river,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) WarningLevel warningLevel) {

        return stations.findAll().stream()
                .filter(s -> river == null || s.getRiver().equalsIgnoreCase(river))
                .filter(s -> isActive == null || s.isActive() == isActive)
                .filter(s -> {
                    if (warningLevel == null) return true;
                    var latest = measurements.latest(s.getId());
                    return latest.isPresent() && latest.get().getWarningLevel() == warningLevel;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Station getOne(@PathVariable String id) {
        return stations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Station " + id + " wurde nicht gefunden"));
    }

    @GetMapping("/{id}/measurements/latest")
    public Measurement getLatest(@PathVariable String id) {
        if (stations.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Station " + id + " wurde nicht gefunden");
        }
        return measurements.latest(id)
                .orElseThrow(() -> new ResourceNotFoundException("Keine Messwerte für Station " + id + " vorhanden"));
    }

    @GetMapping("/{id}/measurements")
    public List<Measurement> getHistory(
            @PathVariable String id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) WarningLevel warningLevel,
            @RequestParam(required = false) Integer limit) {

        if (stations.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Station " + id + " wurde nicht gefunden");
        }

        // Validierung für from nach to
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Der Parameter 'from' darf nicht nach 'to' liegen.");
        }

        // Validierung für Limit
        if (limit != null && (limit <= 0 || limit > 1000)) {
            throw new BadRequestException("Der Parameter 'limit' muss zwischen 1 und 1000 liegen.");
        }

        return measurements.findAll(id, from, to, warningLevel, limit);
    }
}