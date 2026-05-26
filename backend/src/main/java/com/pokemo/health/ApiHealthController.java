package com.pokemo.health;

import java.time.OffsetDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class ApiHealthController {

  @GetMapping
  HealthResponse health() {
    return new HealthResponse("UP", OffsetDateTime.now());
  }

  record HealthResponse(String status, OffsetDateTime timestamp) {
  }
}
