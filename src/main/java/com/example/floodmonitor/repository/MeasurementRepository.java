package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.WarningLevel;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class MeasurementRepository {

    private final Map<String, List<Measurement>> store = new ConcurrentHashMap<>();

    public void save(Measurement measurement) {
        store.computeIfAbsent(measurement.getStationId(),
                        id -> Collections.synchronizedList(new ArrayList<>()))
                .add(measurement);
    }

    public Optional<Measurement> latest(String stationId) {
        List<Measurement> list = store.get(stationId);
        if (list == null) {
            return Optional.empty();
        }
        synchronized (list) {
            return list.isEmpty()
                    ? Optional.empty()
                    : Optional.of(list.get(list.size() - 1));
        }
    }

    public List<Measurement> findAll(String stationId, Instant from, Instant to, WarningLevel warningLevel, Integer limit) {
        List<Measurement> list = store.get(stationId);
        if (list == null) {
            return List.of();
        }
        synchronized (list) {
            return list.stream()
                    .filter(m -> from == null || !m.getTimestamp().isBefore(from))
                    .filter(m -> to == null || !m.getTimestamp().isAfter(to))
                    .filter(m -> warningLevel == null || m.getWarningLevel() == warningLevel)
                    .sorted(Comparator.comparing(Measurement::getTimestamp).reversed()) // Neueste zuerst
                    .limit(limit != null ? limit : Long.MAX_VALUE)
                    .collect(Collectors.toList());
        }
    }
}