package com.codibly.energymix.client;

import com.codibly.energymix.domain.dto.EnergyResponseDto;

public interface CarbonIntensityClient {
    EnergyResponseDto GetGenerationData(String from, String to);
}
