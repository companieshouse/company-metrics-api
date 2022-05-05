Feature: Process company metrics recalculate

  Scenario Outline: retrieve company metrics document successfully

    Given Company metrics api service is running
    And the company metrics exists for "<id>"
    When I send GET request with company number "<companyNumber>"
    Then I should receive 200 status code
    And the Get call response body should match "<result>" file

    Examples:
      | id | companyNumber     | result               |
      | 12345678 | 12345678 | 12345678 |


  Scenario Outline: recalculate charges and insert company metrics db successfully

    Given Company metrics api service is running
    And no company metrics exists for "<id>"
    And the company charges entries exists for "<companyNumber>"
    When I send POST request with company number "<companyNumber>"
    Then I should receive 201 status code
    And company metrics exists for "<id>" in company_metrics db with total mortgage count "<number>"

    Examples:
      | id | companyNumber    | number |
      | 123456789 | 123456789 |  1    |


  Scenario Outline: recalculate charges and update company metrics db successfully

    Given Company metrics api service is running
    And the company metrics exists for "<companyNumber>"
    And the company charges entries exists for "<companyNumber>"
    When I send POST request with company number "<companyNumber>"
    Then I should receive 201 status code
    And company metrics exists for "<id>" in company_metrics db with total mortgage count "<number>"

    Examples:
      | companyNumber     | id  | number |
      | 1234567 | 1234567 | 1            |

  Scenario Outline: recalculate charges receive 400 when mortgage flag is null in request

    Given Company metrics api service is running
    And no company metrics exists for "<id>"
    And the company charges entries exists for "<companyNumber>"
    When I send POST request with company number "<companyNumber>" and mortgage flag null in request
    Then I should receive 400 status code

    Examples:
      | id | companyNumber    |
      | 123456789 | 123456789 |
