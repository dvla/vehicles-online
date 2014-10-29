@US432
Feature: US432: Option to Auto Populate Date of Sale Field with Todays Date

  Scenario: On Dispose page use a manually entered date of disposal with javascript disabled
    Given the Trader is on the Complete & Confirm page and javascript is not enabled for the browser
    When the user manually selects a date using the  Date of Sale date drop downs
    Then the user can select "Confirm sale" without error on the date of sale field

  Scenario: On Dispose page display an error message when no date of disposal is entered
    Given the Trader is on the Complete & Confirm page and javascript is not enabled for the browser
    When the Trader tries to select "Confirm Sale" without setting the Date of Sale
    Then an error will occur stating "Must be between today and two years ago"

  Scenario: Prove the button doesn't work if someone tries to click it using broken CSS
    Given the Trader is on the Complete & Confirm page and javascript is not enabled for the browser
    When the user checks the 'Use today's date' checkbox for Date of Sale
    Then the date dropdown is still unset



