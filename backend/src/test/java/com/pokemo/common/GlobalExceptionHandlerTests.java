package com.pokemo.common;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTests {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .standaloneSetup(new ApiExceptionTestController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void returnsErrorResponseForApiException() throws Exception {
    mockMvc.perform(get("/api/test/api-exception"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Test authentication required"))
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.timestamp", not(blankOrNullString())));
  }

  @RestController
  static class ApiExceptionTestController {

    @GetMapping("/api/test/api-exception")
    void throwApiException() {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Test authentication required");
    }
  }
}
