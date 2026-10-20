package com.example.floodmonitor.repository;

import com.example.floodmonitor.model.Location;
import com.example.floodmonitor.model.Station;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class StationRepository {

    private final Map<String, Station> stations = new LinkedHashMap<>();

    public StationRepository() {
        add(new Station("ST-001", "Wien Donau", "Donau",
                new Location(48.2082, 16.3738), 250, 350, 450, true));
        add(new Station("ST-002", "Linz Donau", "Donau",
                new Location(48.3069, 14.2858), 280, 370, 470, true));
        add(new Station("ST-003", "Melk Donau", "Donau",
                new Location(48.2270, 15.3340), 260, 360, 460, true));
    }

    private void add(Station station) {
        stations.put(station.getId(), station);
    }

    public List<Station> findAll() {
        return List.copyOf(stations.values());
    }

    public Optional<Station> findById(String id) {
        return Optional.ofNullable(stations.get(id));
    }
}