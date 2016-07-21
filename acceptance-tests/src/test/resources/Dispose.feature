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

  Scenario: Date of disposal - Over 12 months in the past
    Given the Trader is on the Complete and Confirm page
    When the user enters a date of sale over 12 months in the past and submits the form
    Then the user will remain on the complete and confirm page and a warning will be displayed
    When the user confirms the date
    Then the sell to trade success page is displayed
