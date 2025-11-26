package com.codibly.energymix.controllers;

import com.codibly.energymix.domain.dto.DailyEnergyMixDto;
import com.codibly.energymix.domain.dto.OptimalChargingWindowDto;
import com.codibly.energymix.services.EnergyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(EnergyController.class)
class EnergyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnergyService energyService;

    @Test
    void energyMix_ShouldReturn200AndList_WhenServiceReturnsData() throws Exception {

        DailyEnergyMixDto dto = new DailyEnergyMixDto("2023-10-27", 50.5, Map.of("solar", 20.0, "wind", 30.5));
        List<DailyEnergyMixDto> mockResponse = List.of(dto);
        given(energyService.getEnergyMixForThreeDays()).willReturn(mockResponse);

        mockMvc.perform(get("/api/energyMixAverage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].date").value("2023-10-27"))
                .andExpect(jsonPath("$[0].cleanEnergyPercent").value(50.5))
                .andExpect(jsonPath("$[0].fuelMix.solar").value(20.0));
    }

    @Test
    void energyMix_ShouldReturn200AndEmptyList_WhenServiceReturnsEmpty() throws Exception {

        given(energyService.getEnergyMixForThreeDays()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/energyMixAverage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void optimalChargingWindow_ShouldReturn200_WhenHoursIsValid() throws Exception {

        int hours = 3;
        OptimalChargingWindowDto dto = new OptimalChargingWindowDto("2023-10-27T10:00:00Z", "2023-10-27T13:00:00Z",
                80.0);
        given(energyService.getOptimalChargingWindow(hours)).willReturn(dto);

        mockMvc.perform(get("/api/optimalChargingWindow")
                .param("hours", String.valueOf(hours))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value("2023-10-27T10:00:00Z"))
                .andExpect(jsonPath("$.endTime").value("2023-10-27T13:00:00Z"))
                .andExpect(jsonPath("$.cleanEnergyPercentage").value(80.0));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 7, -1, 100 })
    void optimalChargingWindow_ShouldThrowException_WhenHoursIsInvalid(int hours) throws Exception {
        mockMvc.perform(get("/api/optimalChargingWindow")
                .param("hours", String.valueOf(hours))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof IllegalArgumentException)) {
                        throw new AssertionError("Expected IllegalArgumentException");
                    }
                });
    }
}
