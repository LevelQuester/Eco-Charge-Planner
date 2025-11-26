package com.codibly.energymix.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handleValidationError_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Validation error"))
                .andExpect(jsonPath("$.message").value("Invalid argument"));
    }

    @Test
    void handleExternalApiError_ShouldReturnServiceUnavailable() throws Exception {
        mockMvc.perform(get("/test/rest-client"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(HttpStatus.SERVICE_UNAVAILABLE.value()))
                .andExpect(jsonPath("$.error").value("External API error"))
                .andExpect(jsonPath("$.message").value("Couldnt load the data from Carbon Intensity API"));
    }

    @Test
    void handleTypeMismatch_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/type-mismatch").param("id", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Type Mismatch"));
    }

    @Test
    void handleNotEnoughData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/test/not-enough-data"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(jsonPath("$.error").value("Invalid data from External API"))
                .andExpect(jsonPath("$.message").value("Not enough data"));
    }

    @Test
    void handleException_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(get("/test/generic-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Something went wrong"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/illegal-argument")
        void throwIllegalArgument() {
            throw new IllegalArgumentException("Invalid argument");
        }

        @GetMapping("/test/rest-client")
        void throwRestClient() {
            throw new RestClientException("External error");
        }

        @GetMapping("/test/type-mismatch")
        void throwTypeMismatch(@RequestParam int id) {
        }

        @GetMapping("/test/not-enough-data")
        void throwNotEnoughData() {
            throw new NotEnoughDataException("Not enough data");
        }

        @GetMapping("/test/generic-exception")
        void throwGeneric() throws Exception {
            throw new Exception("Something went wrong");
        }
    }
}
