@working
Feature: Validate vehicle disposal date

#  Scenario:
#    Given the Trader is on the Complete and Confirm page
#    And the motor trader has confirmed the consent of the current keeper
#    And the motor trader has not confirmed the consent of the current keeper
#    Then one error will occur consent
#
#  Scenario:
#    Given the motor trader has confirmed the consent acknowledgement of the current keeper
#    Then one error will occur keeper aware
#
#  Scenario:
#    Given that entered details correspond to a valid clean record that has no markers or error codes
#    When they attempt to dispose of the vehicle
#    Then dispose success

  Scenario:
#    Given that entered details correspond to a valid record which has markers or error codes
    Given the Trader is on the Complete and Confirm page
    When they attempt to dispose of the vehicle
    Then three errors will occur
    When they attempt to dispose of the vehicle and consent is incomplete
    Then two errors will occur
    When they give full consent and attempt to dispose of vehicle without disposal date
    Then a single error message "Must be a valid date" is displayed
#    Then one error will occur
    When the user enters an invalid disposal date that is too old
    Then a single error message "Must be a valid date" is displayed
    When the user enters an invalid disposal date bad date format
    Then a single error message "Must be a valid date" is displayed
    When the user enters an invalid disposal date bad month format
    Then a single error message "Must be a valid date" is displayed
    When the user enters a valid disposal date
    Then the sell to trade success page is displayed
