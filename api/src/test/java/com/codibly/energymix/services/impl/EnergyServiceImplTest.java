package com.codibly.energymix.services.impl;

import com.codibly.energymix.client.CarbonIntensityClient;
import com.codibly.energymix.domain.dto.DailyEnergyMixDto;
import com.codibly.energymix.domain.dto.EnergyResponseDto;
import com.codibly.energymix.domain.dto.GenerationItemDto;
import com.codibly.energymix.domain.dto.OptimalChargingWindowDto;
import com.codibly.energymix.exception.NotEnoughDataException;
import com.codibly.energymix.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EnergyServiceImplTest {

        @Mock
        private CarbonIntensityClient carbonIntensityClient;

        private EnergyServiceImpl energyService;

        private final Set<String> cleanSources = Set.of("solar", "wind", "nuclear");

        @BeforeEach
        void setUp() {
                energyService = new EnergyServiceImpl(carbonIntensityClient, cleanSources);
        }

        @Test
        void getEnergyMixForThreeDays_ShouldReturnCorrectAverages() {

                LocalDate today = LocalDate.now(ZoneOffset.UTC);
                String todayStr = today.toString();
                String tomorrowStr = today.plusDays(1).toString();
                String dayAfterTomorrowStr = today.plusDays(2).toString();

                GenerationItemDto item1 = TestDataBuilder.generationItem()
                                .from(todayStr + "T10:00:00Z")
                                .withMix("solar", 50.0)
                                .withMix("gas", 20.0)
                                .withMix("coal", 30.0)
                                .build();

                GenerationItemDto item2 = TestDataBuilder.generationItem()
                                .from(todayStr + "T10:30:00Z")
                                .withMix("solar", 60.0)
                                .withMix("gas", 10.0)
                                .withMix("coal", 30.0)
                                .build();

                GenerationItemDto item3 = TestDataBuilder.generationItem()
                                .from(tomorrowStr + "T10:00:00Z")
                                .withMix("solar", 10.0)
                                .withMix("gas", 80.0)
                                .withMix("coal", 10.0)
                                .build();

                GenerationItemDto item4 = TestDataBuilder.generationItem()
                                .from(dayAfterTomorrowStr + "T10:00:00Z")
                                .withMix("solar", 0.0)
                                .withMix("gas", 100.0)
                                .withMix("coal", 0.0)
                                .build();

                List<GenerationItemDto> data = List.of(item1, item2, item3, item4);
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                List<DailyEnergyMixDto> result = energyService.getEnergyMixForThreeDays();

                assertEquals(3, result.size());

                DailyEnergyMixDto day1 = result.stream().filter(d -> d.getDate().equals(todayStr)).findFirst()
                                .orElseThrow();
                assertEquals(55.0, day1.getCleanEnergyPercent(), 0.001);
                assertEquals(55.0, day1.getFuelMix().get("solar"), 0.001);
                assertEquals(15.0, day1.getFuelMix().get("gas"), 0.001);
                assertEquals(30.0, day1.getFuelMix().get("coal"), 0.001);

                DailyEnergyMixDto day2 = result.stream().filter(d -> d.getDate().equals(tomorrowStr)).findFirst()
                                .orElseThrow();
                assertEquals(10.0, day2.getCleanEnergyPercent(), 0.001);

                DailyEnergyMixDto day3 = result.stream().filter(d -> d.getDate().equals(dayAfterTomorrowStr))
                                .findFirst()
                                .orElseThrow();
                assertEquals(0.0, day3.getCleanEnergyPercent(), 0.001);
        }

        @Test
        void getOptimalChargingWindow_ShouldReturnBestWindow() {

                int hours = 1;

                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0);
                String t0 = now.format(DateTimeFormatter.ISO_INSTANT);
                String t1 = now.plusMinutes(30).format(DateTimeFormatter.ISO_INSTANT);
                String t2 = now.plusMinutes(60).format(DateTimeFormatter.ISO_INSTANT);
                String t3 = now.plusMinutes(90).format(DateTimeFormatter.ISO_INSTANT);
                String t4 = now.plusMinutes(120).format(DateTimeFormatter.ISO_INSTANT);

                GenerationItemDto i0 = TestDataBuilder.generationItem().from(t0).to(t1).withMix("solar", 50.0)
                                .withMix("gas", 50.0).build();
                GenerationItemDto i1 = TestDataBuilder.generationItem().from(t1).to(t2).withMix("solar", 60.0)
                                .withMix("gas", 40.0).build();
                GenerationItemDto i2 = TestDataBuilder.generationItem().from(t2).to(t3).withMix("solar", 80.0)
                                .withMix("gas", 20.0).build();
                GenerationItemDto i3 = TestDataBuilder.generationItem().from(t3).to(t4).withMix("solar", 90.0)
                                .withMix("gas", 10.0).build();

                List<GenerationItemDto> data = List.of(i0, i1, i2, i3);
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                OptimalChargingWindowDto result = energyService.getOptimalChargingWindow(hours);

                assertEquals(t2, result.getStartTime());
                assertEquals(t4, result.getEndTime());
                assertEquals(85.0, result.getCleanEnergyPercentage(), 0.001);
        }

        @Test
        void getOptimalChargingWindow_ShouldThrowException_WhenNotEnoughData() {

                int hours = 2;
                List<GenerationItemDto> data = List
                                .of(TestDataBuilder.generationItem().from("2023-01-01T00:00:00Z").withMix("solar", 50.0)
                                                .build());
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                assertThrows(NotEnoughDataException.class, () -> energyService.getOptimalChargingWindow(hours));
        }

        @Test
        void getOptimalChargingWindow_ShouldThrowException_WhenResponseIsNull() {

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(null);

                assertThrows(NotEnoughDataException.class, () -> energyService.getOptimalChargingWindow(1));
        }

        @Test
        void getEnergyMixForThreeDays_ShouldHandleMissingDays() {
                LocalDate today = LocalDate.now(ZoneOffset.UTC);
                String todayStr = today.toString();

                GenerationItemDto item1 = TestDataBuilder.generationItem()
                                .from(todayStr + "T10:00:00Z")
                                .withMix("solar", 50.0)
                                .build();

                List<GenerationItemDto> data = List.of(item1);
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                List<DailyEnergyMixDto> result = energyService.getEnergyMixForThreeDays();

                assertEquals(3, result.size());

                DailyEnergyMixDto day1 = result.stream().filter(d -> d.getDate().equals(todayStr)).findFirst()
                                .orElseThrow();
                assertEquals(50.0, day1.getCleanEnergyPercent(), 0.001);

                String tomorrowStr = today.plusDays(1).toString();
                DailyEnergyMixDto day2 = result.stream().filter(d -> d.getDate().equals(tomorrowStr)).findFirst()
                                .orElseThrow();
                assertEquals(0.0, day2.getCleanEnergyPercent(), 0.001);
                assertTrue(day2.getFuelMix().isEmpty());
        }

        @Test
        void getOptimalChargingWindow_ShouldHandleExactWindowSize() {
                int hours = 1;
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0);
                String t0 = now.format(DateTimeFormatter.ISO_INSTANT);
                String t1 = now.plusMinutes(30).format(DateTimeFormatter.ISO_INSTANT);
                String t2 = now.plusMinutes(60).format(DateTimeFormatter.ISO_INSTANT);

                GenerationItemDto i0 = TestDataBuilder.generationItem().from(t0).to(t1).withMix("solar", 50.0).build();
                GenerationItemDto i1 = TestDataBuilder.generationItem().from(t1).to(t2).withMix("solar", 60.0).build();

                List<GenerationItemDto> data = List.of(i0, i1);
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                OptimalChargingWindowDto result = energyService.getOptimalChargingWindow(hours);

                assertEquals(t0, result.getStartTime());
                assertEquals(t2, result.getEndTime());
                assertEquals(55.0, result.getCleanEnergyPercentage(), 0.001);
        }

        @Test
        void getOptimalChargingWindow_ShouldHandleNullGenerationMix() {
                int hours = 1;
                ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC).withMinute(0).withSecond(0).withNano(0);
                String t0 = now.format(DateTimeFormatter.ISO_INSTANT);
                String t1 = now.plusMinutes(30).format(DateTimeFormatter.ISO_INSTANT);
                String t2 = now.plusMinutes(60).format(DateTimeFormatter.ISO_INSTANT);

                GenerationItemDto i0 = new GenerationItemDto();
                i0.setFrom(t0);
                i0.setTo(t1);
                i0.setGenerationMix(null);

                GenerationItemDto i1 = TestDataBuilder.generationItem().from(t1).to(t2).withMix("solar", 100.0).build();

                List<GenerationItemDto> data = List.of(i0, i1);
                EnergyResponseDto response = new EnergyResponseDto();
                response.setData(data);

                given(carbonIntensityClient.GetGenerationData(anyString(), anyString())).willReturn(response);

                OptimalChargingWindowDto result = energyService.getOptimalChargingWindow(hours);

                assertEquals(t0, result.getStartTime());
                assertEquals(t2, result.getEndTime());
                assertEquals(50.0, result.getCleanEnergyPercentage(), 0.001);
        }
}
