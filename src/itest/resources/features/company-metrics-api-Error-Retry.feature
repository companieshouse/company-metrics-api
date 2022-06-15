Feature: Process company metrics error and retry

  Scenario Outline: Retrieve company metrics document successfully
    Given Company metrics api rest service is running
    And the company charges database is down
    When I send POST request with company number "<companyNumber>"
    Then Rest endpoint returns http response code 503 'Service Unavailable' to the client

    Examples:
      | companyNumber |
      | 12345678      |

  Scenario: Invalid payload that fails to de-serialise
    Given Company metrics api rest service is running
    When I send POST request with an invalid payload that fails to de-serialised into Request object
    Then Rest endpoint returns http response code 400 'Bad Request' to the client

  Scenario: Request without authentication headers fails to process
    Given Company metrics api rest service is running
    When I send POST request with an no auth header
    Then Rest endpoint returns http response code 403 'Forbidden' to the client

  Scenario Outline: Non recoverable NPE returns 500 response
    Given Company metrics api rest service is running
    And A metrics document exists for "<companyNumber>" without data
    When I send POST a metrics recalculate request "<companyNumber>"
    Then Rest endpoint returns http response code 500 'Internal Error' to the client

    Examples:
      | companyNumber |
      | 12345678      |
