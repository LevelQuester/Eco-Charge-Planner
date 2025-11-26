package com.codibly.energymix.client;

import com.codibly.energymix.domain.dto.EnergyResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(value = CarbonIntensityClientImpl.class, properties = "carbon.api.url=http://localhost:8080")
@Import(com.codibly.energymix.config.RestTemplateConfig.class)
class CarbonIntensityClientTest {

  @Autowired
  private CarbonIntensityClientImpl client;

  @Autowired
  private MockRestServiceServer server;

  @Test
  void GetGenerationData_ShouldReturnData_WhenApiReturnsSuccess() {

    String from = "2023-01-01T00:00:00Z";
    String to = "2023-01-01T01:00:00Z";
    String responseJson = """
        {
          "data": [
            {
              "from": "2023-01-01T00:00:00Z",
              "to": "2023-01-01T00:30:00Z",
              "generationmix": []
            }
          ]
        }
        """;

    server.expect(requestTo(org.hamcrest.Matchers.containsString("/2023-01-01T00:30:00Z")))
        .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

    EnergyResponseDto response = client.GetGenerationData(from, to);

    assertNotNull(response);
    assertEquals(1, response.getData().size());
  }

  @Test
  void GetGenerationData_ShouldThrowException_WhenApiReturns404() {

    String from = "2023-01-01T00:00:00Z";
    String to = "2023-01-01T01:00:00Z";

    server.expect(requestTo(org.hamcrest.Matchers.containsString("/2023-01-01T00:30:00Z")))
        .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators
            .withStatus(org.springframework.http.HttpStatus.NOT_FOUND));

   assertThrows(org.springframework.web.client.HttpClientErrorException.class, () -> {
      client.GetGenerationData(from, to);
    });
  }

  @Test
  void GetGenerationData_ShouldThrowException_WhenApiReturns500() {

    String from = "2023-01-01T00:00:00Z";
    String to = "2023-01-01T01:00:00Z";

    server.expect(requestTo(org.hamcrest.Matchers.containsString("/2023-01-01T00:30:00Z")))
        .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withServerError());

    assertThrows(org.springframework.web.client.HttpServerErrorException.class, () -> {
      client.GetGenerationData(from, to);
    });
  }
}
