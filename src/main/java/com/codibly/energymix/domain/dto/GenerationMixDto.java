package com.codibly.energymix.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerationMixDto {
    private String fuel;

    public String getFuel() {
        return fuel;
    }

    public void setFuel(String fuel) {
        this.fuel = fuel;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public GenerationMixDto() {
    }

    public GenerationMixDto(String fuel, Double percentage) {
        this.fuel = fuel;
        this.percentage = percentage;
    }

    @JsonProperty("perc")
    private Double percentage;
}
