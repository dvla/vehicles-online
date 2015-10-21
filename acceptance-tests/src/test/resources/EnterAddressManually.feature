 @working
  Feature:Manual Adress entry for keeper

    Scenario: Changing the post code
      Given the user is on the manual address page
      When the user changes the postcode to "SA1 1AA"
      And the user has selected the submit control
      Then the user is taken to the vehicle lookup page
      And the page will contain text "SA1 1AA"
