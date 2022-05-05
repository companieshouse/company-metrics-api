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


  Scenario Outline: Recalculate charges and update company metrics successfully

    Given Company metrics api service is running
    And no company metrics exists for "<companyNumber>"
    And the company charges entries exists for "<companyNumber>"
    When I send POST request with company number "<companyNumber>"
    Then I should receive 201 status code
    And the Get call response body should match "<result>" file

    Examples:
      | companyNumber     | result               |
      | 00000587 | 00000587 |


