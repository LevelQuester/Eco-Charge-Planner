package com.codibly.energymix.domain.dto;

public class OptimalChargingWindowDto {
        private String startTime;
        private String endTime;
        private Double cleanEnergyPercentage;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Double getCleanEnergyPercentage() {
        return cleanEnergyPercentage;
    }

    public void setCleanEnergyPercentage(Double cleanEnergyPercentage) {
        this.cleanEnergyPercentage = cleanEnergyPercentage;
    }

    public OptimalChargingWindowDto() {
    }

    public OptimalChargingWindowDto(String startTime, String endTime, Double cleanEnergyPercentage) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.cleanEnergyPercentage = cleanEnergyPercentage;
    }
}
