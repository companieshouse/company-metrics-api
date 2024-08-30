Feature: Process company metrics error and retry

  Scenario Outline: Retrieve company metrics document successfully
    And the company charges database is down
    When I send POST request with company number "<companyNumber>" to update charges metrics
    Then Rest endpoint returns http response code 503 'Service Unavailable' to the client

    Examples:
      | companyNumber |
      | 12345678      |

  Scenario: Invalid payload that fails to de-serialise
    When I send POST request with an invalid payload that fails to de-serialised into Request object
    Then Rest endpoint returns http response code 400 'Bad Request' to the client

  Scenario: Request without authentication headers fails to process
    When I send POST request with an no auth header
    Then Rest endpoint returns http response code 401 'Unauthorised' to the client
