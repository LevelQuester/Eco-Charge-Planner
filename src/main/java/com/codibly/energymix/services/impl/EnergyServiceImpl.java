package com.codibly.energymix.services.impl;

import com.codibly.energymix.client.CarbonIntensityClient;
import com.codibly.energymix.domain.dto.DailyEnergyMixDto;
import com.codibly.energymix.domain.dto.EnergyResponseDto;
import com.codibly.energymix.domain.dto.GenerationItemDto;
import com.codibly.energymix.domain.dto.GenerationMixDto;
import com.codibly.energymix.services.EnergyService;
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

    public EnergyServiceImpl(CarbonIntensityClient carbonIntensityClient) {
        this.carbonIntensityClient = carbonIntensityClient;
    }

    private static final Set<String> CLEAN_ENERGY = Set.of("biomass", "nuclear", "hydro", "wind", "solar");

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

        for(int i=0; i<3;i++)
        {
            String date = today.plusDays(i).toString();
            EnergyMixForThreeDays.add(CalculateDailyEnergyMixAverage(date, groupedByDay.get(date)));
        }

        return EnergyMixForThreeDays;
    }

    private DailyEnergyMixDto CalculateDailyEnergyMixAverage(String date, List<GenerationItemDto> intervals) {
        Map<String, Double> sums = new HashMap<>();

        for (GenerationItemDto interval : intervals) {
            for(GenerationMixDto mix : interval.getGenerationMix())
            {
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

            if(CLEAN_ENERGY.contains(fuel)) {
                cleanEnergyPercent += averagePercent;
            }
        }

        cleanEnergyPercent = Math.round(cleanEnergyPercent * 10.0) / 10.0;

        return new DailyEnergyMixDto(date, cleanEnergyPercent, averages);
    }
}
