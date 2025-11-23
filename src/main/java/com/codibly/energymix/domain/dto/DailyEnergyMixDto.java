package com.codibly.energymix.domain.dto;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyEnergyMixDto {

    private String date;

    private Double cleanEnergyPercent;

    private Map<String, Double> fuelMix;
}
