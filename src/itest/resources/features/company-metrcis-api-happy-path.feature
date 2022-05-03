Feature: Process company metrics recalculate

  Scenario Outline: Retrieve company links successfully

    Given Company metrics api service is running
    And the company metrics exists for "<companyNumber>"
    When I send GET request with company number "<companyNumber>"
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file

    Examples:
      | companyNumber     | result               |
      | 00000587 | 00000587 |
