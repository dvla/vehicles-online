@tag
Feature: Validate document reference entry format - Error Messages
  As a Motor Trader
  I want to receive an appropriate error message when I enter an invalid document reference number
  so that I can correct it and move to the next step in the transaction

  Scenario:
    Given a correctly formatted document reference number "11111111111" has been entered
    When this is submitted along with any other mandatory information
    Then the document reference number "11111111111" is retained
    And the next step in the dispose transaction "Complete & confirm" is shown

  Scenario Outline:
    Given an incorrectly formatted document reference number "<incorrect>" has been entered
    When this is submitted along with any other mandatory information
    Then a single error message "Document reference number - Document reference number must be an 11-digit number" is displayed
    And the dispose transaction does not proceed past the "Enter vehicle details" step

  Examples:
    | incorrect    | comment                            |
    |              | no document reference number       |
    | 1234567890   | 10 digit document reference number |
    | 123456789012 | 12 digit document reference number |
    | abcdefghijk  | all alphabetic characters          |
    | abc123abc12  | alphanumeric characters            |
