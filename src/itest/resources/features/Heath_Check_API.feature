Feature: Health check API endpoint


  @Ignored
  Scenario: Client invokes GET /healthcheck endpoint
    Given the application running
    When the client invokes '/company-metrics-api/healthcheck' endpoint
    Then the client receives status code of 200
    And the client receives response body as '{"status":"UP"}'
