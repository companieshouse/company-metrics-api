Feature: Pscs acceptance criteria

  Scenario Outline: Create
    Given A company metrics resource does not exist for a company number of "0123456"
    And The company has <activeStatements> active Psc Statements and <withdrawnStatements> withdrawn Psc Statements
    And The company has <activePscs> active Pscs and <ceasedPscs> ceased Pscs
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.persons-with-significant-control object
    And The PscCounts should have <activeStatements> active statements, <withdrawnStatements> withdrawn statements, <activePscs> active Pscs, <ceasedPscs> ceased Pscs and the correct counts for total Pscs and total statements
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"
    Examples:
      | activeStatements | withdrawnStatements | activePscs | ceasedPscs |
      | 2                | 3                   | 1          | 5          |

  Scenario Outline: Update
    Given A company metrics resource exists for a company number of "0123456"
    And The company has <activeStatements> active Psc Statements and <withdrawnStatements> withdrawn Psc Statements
    And The company has <activePscs> active Pscs and <ceasedPscs> ceased Pscs
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.persons-with-significant-control object
    And The PscCounts should have <activeStatements> active statements, <withdrawnStatements> withdrawn statements, <activePscs> active Pscs, <ceasedPscs> ceased Pscs and the correct counts for total Pscs and total statements
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"
    Examples:
      | activeStatements | withdrawnStatements | activePscs | ceasedPscs |
      | 7                | 2                   | 3          | 5          |

  Scenario Outline: Update without an updatedBy value
    Given A company metrics resource exists for a company number of "0123456"
    And The company has <activeStatements> active Psc Statements and <withdrawnStatements> withdrawn Psc Statements
    And The company has <activePscs> active Pscs and <ceasedPscs> ceased Pscs
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with a context ID of "987654321" but without an updatedBy value
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.persons-with-significant-control object
    And The PscCounts should have <activeStatements> active statements, <withdrawnStatements> withdrawn statements, <activePscs> active Pscs, <ceasedPscs> ceased Pscs and the correct counts for total Pscs and total statements
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "contextId:987654321"
    Examples:
      | activeStatements | withdrawnStatements | activePscs | ceasedPscs |
      | 7                | 2                   | 3          | 5          |

  Scenario: Document Cleanup No Pscs
    Given A company metrics resource exists for a company number of "0123456"
    And The company has ZERO Pscs or Psc Statements registered
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource does not have a count.persons-with-significant-control object
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"

  Scenario: Document Cleanup No counts
    Given A company metrics resource exists for a company number of "0123456" but with no counts metrics
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource does not have a counts object
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"

  Scenario: Document Cleanup No metrics
    Given A company metrics resource exists for a company number of "0123456" but no metrics
    And The user is fully authenticated and authorised with internal app privileges
    When The recalculate Pscs endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The response body should be empty
    And The response should not include a location header
    And The company metrics document has been deleted
