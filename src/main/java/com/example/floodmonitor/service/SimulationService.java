package com.example.floodmonitor.service;

import com.example.floodmonitor.model.Measurement;
import com.example.floodmonitor.model.Station;
import com.example.floodmonitor.model.StationStatus;
import com.example.floodmonitor.repository.MeasurementRepository;
import com.example.floodmonitor.repository.StationRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class SimulationService {

    // Simulationskonstanten – in der Dokumentation begründen
    private static final double RAIN_DECAY = 0.7;        // Regen klingt pro Messung ab
    private static final double RAIN_PROBABILITY = 0.03; // Chance auf einen Schauer
    private static final double RAIN_MAX = 80.0;         // mm/h
    private static final double LEVEL_PER_MM = 0.5;      // cm Anstieg pro mm/h Regen
    private static final double RETURN_FACTOR = 0.05;    // Rückkehr zum Normalwert
    private static final double LEVEL_NOISE = 0.8;       // cm Standardabweichung
    private static final double FLOW_FACTOR = 0.13;      // Q = 0.13 * h^1.5
    private static final int BATTERY_LOW = 15;           // ab hier MAINTENANCE
    private static final int BATTERY_DRAIN_CHANCE = 360; // ca. 1 % pro Stunde bei 10 s Intervall

    private final StationRepository stations;
    private final MeasurementRepository measurements;
    private final WarningService warningService;
    private final Random random = new Random();

    // Hinzugefügt/Korrigiert: Fehlende Map für Ausfall-Ticks
    private final Map<String, Integer> ausfallTicks = new HashMap<>();

    public SimulationService(StationRepository stations, MeasurementRepository measurements, WarningService warningService) {
        this.stations = stations;
        this.measurements = measurements;
        this.warningService = warningService;
    }

    @PostConstruct
    void initialize() {
        for (Station station : stations.findAll()) {
            measurements.save(initialMeasurement(station));
        }
    }

    // Wird vom Scheduler aufgerufen
    public void tick() {
        for (Station station : stations.findAll()) {
            if (!station.isActive()) {
                continue;
            }
            Optional<Measurement> previous = measurements.latest(station.getId());
            if (previous.isEmpty() || previous.get().getStatus() == StationStatus.OFFLINE) {
                continue;
            }
            measurements.save(next(previous.get(), station, random));
        }
    }

    // Öffentlich, damit man die Regeln ohne Scheduler testen kann
    public Measurement next(Measurement prev, Station station, Random rnd) {
        // Niederschlag: klingt ab, gelegentlich kommt ein Schauer
        double rain = prev.getRainfall() * RAIN_DECAY
                + (rnd.nextDouble() < RAIN_PROBABILITY ? rnd.nextDouble() * 40 : 0);
        rain = clamp(rain, 0, RAIN_MAX);

        // Wasserstand: Regen hebt, Rückkehr zum Normalwert senkt, kleine Schwankung
        double level = prev.getWaterLevel()
                + LEVEL_PER_MM * rain
                + RETURN_FACTOR * (station.getNormalWaterLevel() - prev.getWaterLevel())
                + rnd.nextGaussian() * LEVEL_NOISE;
        level = round(Math.max(0, level), 2);

        // Durchfluss steigt mit dem Wasserstand
        double flow = round(FLOW_FACTOR * Math.pow(level, 1.5), 1);

        // Temperatur ändert sich nur langsam
        double temperature = round(clamp(prev.getTemperature() + rnd.nextGaussian() * 0.2, -5, 35), 1);

        // Akku sinkt sehr langsam
        int battery = prev.getBatteryLevel();
        if (rnd.nextInt(BATTERY_DRAIN_CHANCE) == 0) {
            battery--;
        }
        battery = (int) clamp(battery, 0, 100);

        StationStatus status = battery < BATTERY_LOW
                ? StationStatus.MAINTENANCE
                : StationStatus.ONLINE;

        return new Measurement(
                station.getId(),
                Instant.now(),
                level,
                flow,
                round(rain, 1),
                temperature,
                battery,
                status,
                warningService.calculate(station, level));
    }

    private Measurement initialMeasurement(Station station) {
        double level = station.getNormalWaterLevel();
        return new Measurement(
                station.getId(),
                Instant.now(),
                level,
                round(FLOW_FACTOR * Math.pow(level, 1.5), 1),
                0.0,
                round(8 + random.nextDouble() * 8, 1),
                60 + random.nextInt(41),
                StationStatus.ONLINE,
                warningService.calculate(station, level)
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}