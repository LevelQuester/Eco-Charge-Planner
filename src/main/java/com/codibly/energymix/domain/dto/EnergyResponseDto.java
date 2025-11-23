package com.codibly.energymix.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class EnergyResponseDto {
    private List<GenerationItemDto> data;
}
