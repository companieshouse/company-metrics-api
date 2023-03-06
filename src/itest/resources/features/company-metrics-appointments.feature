Feature: Appointments acceptance criteria

  Scenario Outline: Create
    Given A company metrics resource does not exist for a company number of "0123456"
    And The company has ACTIVE appointments for <activeDirectors> directors, <activeSecretaries> secretaries, <activeLLPMembers> LLP members, <activeCorporateLLPMembers> Corp. LLP members
    And The company has RESIGNED appointments for <resignedDirectors> directors, <resignedSecretaries> secretaries, <resignedLLPMembers> LLP members, <resignedCorporateLLPMembers> Corp. LLP members
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.appointments object
    And The appointments should have <activeDirectors> active directors, <activeSecretaries> active secretaries, <activeLLPMembers> active LLP members, <activeCorporateLLPMembers> active Corp. LLP members, <totalCount> total appointments, <activeCount> active appointments, <resignedCount> resigned appointments
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"
    Examples:
      | activeDirectors | activeSecretaries | activeLLPMembers | activeCorporateLLPMembers | resignedDirectors | resignedSecretaries | resignedLLPMembers | resignedCorporateLLPMembers | activeCount | totalCount | resignedCount |
      | 2               | 0                 | 0                | 0                         | 1                 | 0                   | 0                  | 0                           | 2           | 3          | 1             |

  Scenario Outline: Update
    Given A company metrics resource exists for a company number of "0123456"
    And The company has ACTIVE appointments for <activeDirectors> directors, <activeSecretaries> secretaries, <activeLLPMembers> LLP members, <activeCorporateLLPMembers> Corp. LLP members
    And The company has RESIGNED appointments for <resignedDirectors> directors, <resignedSecretaries> secretaries, <resignedLLPMembers> LLP members, <resignedCorporateLLPMembers> Corp. LLP members
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.appointments object
    And The appointments should have <activeDirectors> active directors, <activeSecretaries> active secretaries, <activeLLPMembers> active LLP members, <activeCorporateLLPMembers> active Corp. LLP members, <totalCount> total appointments, <activeCount> active appointments, <resignedCount> resigned appointments
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"
    Examples:
      | activeDirectors | activeSecretaries | activeLLPMembers | activeCorporateLLPMembers | resignedDirectors | resignedSecretaries | resignedLLPMembers | resignedCorporateLLPMembers | activeCount | totalCount | resignedCount |
      | 1               | 1                 | 0                | 0                         | 1                 | 0                   | 0                  | 0                           | 2           | 3          | 1             |

  Scenario Outline: Update without an updatedBy value
    Given A company metrics resource exists for a company number of "0123456"
    And The company has ACTIVE appointments for <activeDirectors> directors, <activeSecretaries> secretaries, <activeLLPMembers> LLP members, <activeCorporateLLPMembers> Corp. LLP members
    And The company has RESIGNED appointments for <resignedDirectors> directors, <resignedSecretaries> secretaries, <resignedLLPMembers> LLP members, <resignedCorporateLLPMembers> Corp. LLP members
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with a context ID of "987654321" but without an updatedBy value
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource has a count.appointments object
    And The appointments should have <activeDirectors> active directors, <activeSecretaries> active secretaries, <activeLLPMembers> active LLP members, <activeCorporateLLPMembers> active Corp. LLP members, <totalCount> total appointments, <activeCount> active appointments, <resignedCount> resigned appointments
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "contextId:987654321"
    Examples:
      | activeDirectors | activeSecretaries | activeLLPMembers | activeCorporateLLPMembers | resignedDirectors | resignedSecretaries | resignedLLPMembers | resignedCorporateLLPMembers | activeCount | totalCount | resignedCount |
      | 1               | 1                 | 0                | 0                         | 1                 | 0                   | 0                  | 0                           | 2           | 3          | 1             |


  Scenario: Document Cleanup No appointments
    Given A company metrics resource exists for a company number of "0123456"
    And The company has ZERO appointments registered
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource does not have a count.appointments object
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"

  Scenario: Document Cleanup No counts
    Given A company metrics resource exists for a company number of "0123456" but with no counts metrics
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The location response header should be "/company/0123456/metrics"
    And The response body should be empty
    And The metrics resource does not have a counts object
    And The etag should be updated
    And The updated object will have valid timestamp (UTC), type: "company_metrics" and by: "stream-partition-offset"

  Scenario: Document Cleanup No metrics
    Given A company metrics resource exists for a company number of "0123456" but no metrics
    And The user is authenticated and authorised with internal app privileges
    When The recalculate endpoint is called with updatedBy as "stream-partition-offset"
    Then The response code should be HTTP OK
    And The response body should be empty
    And The response should not include a location header
    And The company metrics document has been deleted
