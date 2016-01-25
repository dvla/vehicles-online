@working
Feature: Validate vehicle disposal date

  Scenario:
    Given the Trader is on the Complete and Confirm page
    When they attempt to dispose of the vehicle
    Then three errors will occur
    When they attempt to dispose of the vehicle and consent is incomplete
    Then two errors will occur
    When they give full consent and attempt to dispose of vehicle without disposal date
    Then a single error message "Must be a valid date" is displayed
    When the user enters an invalid disposal date that is too old
    Then a single error message "Must be a valid date" is displayed
    When the user enters an invalid disposal date bad date format
    Then a single error message "Must be a valid date" is displayed
    When the user enters an invalid disposal date bad month format
    Then a single error message "Must be a valid date" is displayed
    When the user enters a valid disposal date
    Then the sell to trade success page is displayed
