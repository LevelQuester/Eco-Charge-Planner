package com.codibly.energymix.services.impl;

import com.codibly.energymix.client.CarbonIntensityClient;
import com.codibly.energymix.domain.dto.*;
import com.codibly.energymix.exception.NotEnoughDataException;
import com.codibly.energymix.services.EnergyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnergyServiceImpl implements EnergyService {

    private final CarbonIntensityClient carbonIntensityClient;
    private final Set<String> cleanSources;

    public EnergyServiceImpl(CarbonIntensityClient carbonIntensityClient,
            @Value("${energy.clean-sources:biomass,nuclear,hydro,wind,solar}") Set<String> cleanSources) {
        this.carbonIntensityClient = carbonIntensityClient;
        this.cleanSources = cleanSources;
    }

    @Override
    public List<DailyEnergyMixDto> getEnergyMixForThreeDays() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        String fromDate = today.atStartOfDay(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_INSTANT);

        String toDate = today.atStartOfDay(ZoneOffset.UTC)
                .plusDays(3).format(DateTimeFormatter.ISO_INSTANT);

        EnergyResponseDto response = carbonIntensityClient.GetGenerationData(fromDate, toDate);

        Map<String, List<GenerationItemDto>> groupedByDay = response.getData().stream()
                .collect(Collectors.groupingBy(item -> {
                    return ZonedDateTime.parse(item.getFrom())
                            .toLocalDate()
                            .toString();
                }));

        ArrayList<DailyEnergyMixDto> EnergyMixForThreeDays = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String date = today.plusDays(i).toString();
            EnergyMixForThreeDays.add(CalculateDailyEnergyMixAverage(date, groupedByDay.get(date)));
        }

        return EnergyMixForThreeDays;
    }

    private DailyEnergyMixDto CalculateDailyEnergyMixAverage(String date, List<GenerationItemDto> intervals) {
        Map<String, Double> sums = new HashMap<>();

        for (GenerationItemDto interval : intervals) {
            for (GenerationMixDto mix : interval.getGenerationMix()) {
                if (!sums.containsKey(mix.getFuel())) {
                    sums.put(mix.getFuel(), mix.getPercentage());
                } else {
                    double oldVal = sums.get(mix.getFuel());
                    sums.put(mix.getFuel(), oldVal + mix.getPercentage());
                }
            }
        }

        double count = intervals.size();
        Map<String, Double> averages = new HashMap<>();
        double cleanEnergyPercent = 0;

        for (Map.Entry<String, Double> entry : sums.entrySet()) {
            String fuel = entry.getKey();
            double averagePercent = entry.getValue() / count;

            averagePercent = Math.round(averagePercent * 10.0) / 10.0;
            averages.put(fuel, averagePercent);

            if (cleanSources.contains(fuel)) {
                cleanEnergyPercent += averagePercent;
            }
        }

        cleanEnergyPercent = Math.round(cleanEnergyPercent * 10.0) / 10.0;

        return new DailyEnergyMixDto(date, cleanEnergyPercent, averages);
    }

    private static final int INTERVAL_MINUTES = 30;
    private static final int DAYS_TO_FETCH = 2;

    private double calculateCleanEnergyPercentPerInterval(GenerationItemDto interval) {
        double cleanEnergyPercent = 0;
        if (interval.getGenerationMix() != null) {
            for (GenerationMixDto mix : interval.getGenerationMix()) {
                if (cleanSources.contains(mix.getFuel())) {
                    cleanEnergyPercent += mix.getPercentage();
                }
            }
        }
        return cleanEnergyPercent;
    }

    @Override
    public OptimalChargingWindowDto getOptimalChargingWindow(int hours) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);

        String from = now.withMinute((now.getMinute() / INTERVAL_MINUTES) * INTERVAL_MINUTES)
                .withSecond(0).withNano(0)
                .format(DateTimeFormatter.ISO_INSTANT);

        String to = ZonedDateTime.parse(from).plusDays(DAYS_TO_FETCH).format(DateTimeFormatter.ISO_INSTANT);

        EnergyResponseDto response = carbonIntensityClient.GetGenerationData(from, to);

        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new NotEnoughDataException("Couldnt get generation data");
        }

        List<GenerationItemDto> data = response.getData();
        int windowSize = (hours * 60) / INTERVAL_MINUTES;

        if (data.size() < windowSize) {
            throw new NotEnoughDataException("Not enough generation data to calculate charging window");
        }

        double[] prefixSumsCleanEnergyPercent = new double[data.size() + 1];
        prefixSumsCleanEnergyPercent[0] = 0;

        for (int i = 0; i < data.size(); i++) {
            double currentCleanEnergyPercent = calculateCleanEnergyPercentPerInterval(data.get(i));
            prefixSumsCleanEnergyPercent[i + 1] = prefixSumsCleanEnergyPercent[i] + currentCleanEnergyPercent;
        }

        double maxCleanEnergyPercentSum = -1;
        int bestStartIndex = -1;

        for (int i = windowSize; i < prefixSumsCleanEnergyPercent.length; i++) {
            double currentWindowSum = prefixSumsCleanEnergyPercent[i] - prefixSumsCleanEnergyPercent[i - windowSize];
            if (currentWindowSum > maxCleanEnergyPercentSum) {
                maxCleanEnergyPercentSum = currentWindowSum;
                bestStartIndex = i - windowSize;
            }
        }

        if (bestStartIndex == -1) {
            throw new NotEnoughDataException("Coulndt find valid charging window");
        }

        double averageCleanEnergyPercent = maxCleanEnergyPercentSum / windowSize;
        averageCleanEnergyPercent = Math.round(averageCleanEnergyPercent * 10.0) / 10.0;

        String startTime = data.get(bestStartIndex).getFrom();
        String endTime = data.get(bestStartIndex + windowSize - 1).getTo();

        return new OptimalChargingWindowDto(startTime, endTime, averageCleanEnergyPercent);
    }
}
