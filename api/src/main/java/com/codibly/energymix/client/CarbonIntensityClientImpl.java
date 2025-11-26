package com.codibly.energymix.client;

import com.codibly.energymix.domain.dto.EnergyResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CarbonIntensityClientImpl implements CarbonIntensityClient {
    private final RestTemplate restTemplate;

    @Value("${carbon.api.url}")
    private String api_url;

    public CarbonIntensityClientImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public EnergyResponseDto GetGenerationData(String from, String to) {
        from = ZonedDateTime.parse(from).plusMinutes(30).format(DateTimeFormatter.ISO_INSTANT);
        String url = String.format("%s/%s/%s", api_url, from, to);
        return restTemplate.getForObject(url, EnergyResponseDto.class);
    }
}
