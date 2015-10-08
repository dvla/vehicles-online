@working
Feature: Disposal to Trade: Change Trader Details
  As a Motor Trader Administrator
  I want to be easily able to change the Trader details
  So that I do not enter the wrong Trade outlet for the transaction

  Scenario:
    Given I am on the Enter vehicle details page
    When I select the 'Change these trader details?' function
    Then I will be directed to the Provide Trader details page with the entry fields empty
