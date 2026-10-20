package com.example.floodmonitor.controller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "simulation.enabled=false") // Schaltet den Scheduler für Tests
public class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllStationsJson() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void testGetAllStationsXml() throws Exception {
        mockMvc.perform(get("/api/v1/stations")
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML));
    }

    @Test
    void testGetExistingStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ST-001"))
                .andExpect(jsonPath("$.name").value("Wien Donau"));
    }

    @Test
    void testGetUnknownStation() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testGetLatestMeasurement() throws Exception {
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements/latest")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stationId").value("ST-001"))
                .andExpect(jsonPath("$.waterLevel").exists());
    }

    @Test
    void testGetMeasurementsInvalidDateRange() throws Exception {
        // 'from' liegt nach 'to' -> Erwartet 400 Bad Request
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("from", "2026-05-01T12:00:00Z")
                        .param("to", "2026-05-01T08:00:00Z")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testGetMeasurementsInvalidLimit() throws Exception {
        // Ungültiges Limit (z. B. zu hoch) -> Erwartet 400 Bad Request
        mockMvc.perform(get("/api/v1/stations/ST-001/measurements")
                        .param("limit", "5000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}