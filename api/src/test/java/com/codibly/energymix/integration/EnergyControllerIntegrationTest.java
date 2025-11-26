package com.codibly.energymix.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EnergyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());

    @AfterAll
    static void stopWireMockServer() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        registry.add("carbon.api.url", wireMockServer::baseUrl);
    }

    @Test
    void energyMix_ShouldReturn200AndCorrectData() throws Exception {
        String today = java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString();
        String responseBody = String.format("""
                {
                  "data": [
                    {
                      "from": "%sT10:00:00Z",
                      "to": "%sT10:30:00Z",
                      "generationmix": [
                        { "fuel": "solar", "perc": 50.0 },
                        { "fuel": "wind", "perc": 30.0 },
                        { "fuel": "gas", "perc": 20.0 }
                      ]
                    }
                  ]
                }
                """, today, today);

        wireMockServer.stubFor(WireMock.get(urlPathMatching("/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        mockMvc.perform(get("/api/energyMixAverage")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cleanEnergyPercent").value(80.0))
                .andExpect(jsonPath("$[0].fuelMix.solar").value(50.0));
    }

    @Test
    void optimalChargingWindow_ShouldReturn200AndBestWindow() throws Exception {

        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
                .withMinute(0).withSecond(0).withNano(0);

        String t0 = now.format(java.time.format.DateTimeFormatter.ISO_INSTANT);
        String t1 = now.plusMinutes(30).format(java.time.format.DateTimeFormatter.ISO_INSTANT);
        String t2 = now.plusMinutes(60).format(java.time.format.DateTimeFormatter.ISO_INSTANT);
        String t3 = now.plusMinutes(90).format(java.time.format.DateTimeFormatter.ISO_INSTANT);
        String t4 = now.plusMinutes(120).format(java.time.format.DateTimeFormatter.ISO_INSTANT);

        String responseBody = String.format("""
                {
                  "data": [
                    {
                      "from": "%s",
                      "to": "%s",
                      "generationmix": [{ "fuel": "gas", "perc": 100.0 }]
                    },
                    {
                      "from": "%s",
                      "to": "%s",
                      "generationmix": [{ "fuel": "gas", "perc": 100.0 }]
                    },
                    {
                      "from": "%s",
                      "to": "%s",
                      "generationmix": [{ "fuel": "solar", "perc": 100.0 }]
                    },
                    {
                      "from": "%s",
                      "to": "%s",
                      "generationmix": [{ "fuel": "solar", "perc": 100.0 }]
                    }
                  ]
                }
                """, t0, t1, t1, t2, t2, t3, t3, t4);

        wireMockServer.stubFor(WireMock.get(urlPathMatching("/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        mockMvc.perform(get("/api/optimalChargingWindow")
                .param("hours", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startTime").value(t2))
                .andExpect(jsonPath("$.endTime").value(t4))
                .andExpect(jsonPath("$.cleanEnergyPercentage").value(100.0));
    }

    @Test
    void shouldHandleExternalApiError500() throws Exception {
        wireMockServer.stubFor(WireMock.get(urlPathMatching("/.*"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(get("/api/energyMixAverage"))
                .andExpect(status().isServiceUnavailable());
    }
}
