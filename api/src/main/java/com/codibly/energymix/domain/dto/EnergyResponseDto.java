package com.codibly.energymix.domain.dto;

import java.util.List;


public class EnergyResponseDto {
    private List<GenerationItemDto> data;

    public void setData(List<GenerationItemDto> data) {
        this.data = data;
    }

    public List<GenerationItemDto> getData() {
        return data;
    }

    public EnergyResponseDto() {
    }

    public EnergyResponseDto(List<GenerationItemDto> data) {
        this.data = data;
    }
}
