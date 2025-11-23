package com.codibly.energymix.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GenerationMixDto {
    private String fuel;

    @JsonProperty("perc")
    private Double percentage;
}
