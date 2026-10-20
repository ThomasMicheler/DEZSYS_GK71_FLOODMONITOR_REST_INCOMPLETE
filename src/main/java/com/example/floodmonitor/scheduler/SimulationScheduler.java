package com.example.floodmonitor.scheduler;

import com.example.floodmonitor.service.SimulationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true", matchIfMissing = true)
public class SimulationScheduler {

    private final SimulationService simulationService;

    public SimulationScheduler(SimulationService simulationService) {
        this.simulationService = simulationService;
    }

    @Scheduled(fixedRateString = "${simulation.interval:10000}")
    public void run() {
        simulationService.tick();
    }
}