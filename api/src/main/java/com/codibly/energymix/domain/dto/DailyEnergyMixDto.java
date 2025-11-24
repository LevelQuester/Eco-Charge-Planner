package com.codibly.energymix.domain.dto;

import java.util.Map;


public class DailyEnergyMixDto {

    private String date;

    private Double cleanEnergyPercent;

    private Map<String, Double> fuelMix;

    public DailyEnergyMixDto(String date, Double cleanEnergyPercent, Map<String, Double> fuelMix) {
        this.date = date;
        this.cleanEnergyPercent = cleanEnergyPercent;
        this.fuelMix = fuelMix;
    }

    public DailyEnergyMixDto() {
    }

    public String getDate() {
        return date;
    }

    public Double getCleanEnergyPercent() {
        return cleanEnergyPercent;
    }

    public Map<String, Double> getFuelMix() {
        return fuelMix;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCleanEnergyPercent(Double cleanEnergyPercent) {
        this.cleanEnergyPercent = cleanEnergyPercent;
    }

    public void setFuelMix(Map<String, Double> fuelMix) {
        this.fuelMix = fuelMix;
    }
}
