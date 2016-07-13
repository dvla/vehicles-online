@working
Feature: Disposal to Trade: Validate VRM format entry
  As a Motor Trader
  I want to enter a properly formatted VRM
  so that I can progress through the transaction

  Scenario Outline:
    Given a correctly formatted vehicle reference mark "<vrm>" has been entered
    When this is submitted along with any other mandatory information
    Then the vehicle reference mark "<vrm>" is retained
    And the next step in the dispose transaction "Complete and confirm" is shown

  Examples:
    | vrm     |
    | A9      |
    | A99     |
    | A999    |

  Scenario Outline:
    Given an incorrectly formatted vehicle reference mark "<vrm>" has been entered
    When this is submitted along with any other mandatory information
    Then a single error message "Vehicle registration number must be valid format" is displayed
    And the dispose transaction does not proceed past the "Enter vehicle details" step

  Examples:
    | vrm     |
    |         |
    | 9A99A99 |
    | 9A9A99  |
    | 9AAAA9  |
