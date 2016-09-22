 @working
  Feature:Manual Adress entry for keeper

    Scenario: Manual address entry
      Given the user is on the manual address page
      When the user has selected the submit control with the postcode "SA1 1AA"
      Then the user is taken to the vehicle lookup page
      And the page will contain text "SA1 1AA"
