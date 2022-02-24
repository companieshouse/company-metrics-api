package uk.gov.companieshouse.company.metrics.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    private static final String HEALTH_CHECK_RESPONSE = "I am healthy";

    @GetMapping("/healthcheck")
    public ResponseEntity<String> healthcheck() {
        //TODO: Introduce spring actuator
        return ResponseEntity.status(HttpStatus.OK).body(HEALTH_CHECK_RESPONSE);
    }

}
