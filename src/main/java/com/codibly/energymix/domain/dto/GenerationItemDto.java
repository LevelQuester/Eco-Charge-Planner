package com.codibly.energymix.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class GenerationItemDto {
    private String from;
    private String to;

    @JsonProperty("generationmix")
    private List<GenerationMixDto> generationMix;
}
