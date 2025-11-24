package com.codibly.energymix.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GenerationItemDto {
    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<GenerationMixDto> getGenerationMix() {
        return generationMix;
    }

    public void setGenerationMix(List<GenerationMixDto> generationMix) {
        this.generationMix = generationMix;
    }

    public GenerationItemDto() {
    }

    public GenerationItemDto(String from, String to, List<GenerationMixDto> generationMix) {
        this.from = from;
        this.to = to;
        this.generationMix = generationMix;
    }

    @JsonProperty("generationmix")
    private List<GenerationMixDto> generationMix;
}
