@Sample
Feature: Sample Feature

  @SampleXmlFeature
  Scenario Outline: Feature One
    Given the template "SampleRestPayload" is prepared
    And set the "Tag1" for "Parent" is "<Tag1Value>"
    Then system runs "Rest" request for "Service" rule validation
    Then validate the "Expected" for "EligibilityMessage-Message" is "<ExpectedValue>"

    Examples: 
      | Tag1Value | ExpectedValue |
      | Test1     | Pass          |
      | Test2     | Fail          |

  @SampleJsonFeature
  Scenario Outline: Send Email using JSON Payload
    Given the base template "SampleRestJsonMulti" is prepared
    And set the node "model" for "cars-Nissan" is "<model>"
    When system runs "Rest" request for "CarValidation" validation
    Then validate the node "statusCode" for "root" is "<statusCode>"

    Examples: 
      | model  | statusCode |
      | Audi   |        200 |
      | Maruti |        400 |

  @SampleGet
  Scenario Outline: Send Email using JSON Payload
    When system runs "Get" request for "dummy" validation

    Examples: 
      | model  | statusCode |
      | Audi   |        200 |
      | Maruti |        400 |
