@working
  Feature:
    Scenario:
      Given I am on the vehicles online prototype site url
      And   I click the Start now button to begin the transaction
      And   I enter trader name and postcode then click on next button
      And   Select the address form address choose page then click on next button
      When  I enter vehicle look up details and click on submit button
      Then  I should be taken to complete and confirm page and fill the required details and click on confirm sale button
      And   I am on the summary page
