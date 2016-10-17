@working
Feature:
  As a Private Keeper selling to the trade
  I want to see successful transaction and failure transaction from start to end

  Scenario:
    Given I am on the private complete and confirm page
    When  I click on confirm sale button without any validation errors
    Then  I should be taken to the private successful summary page
    And   I can see the details about the vehicle registration number doc ref no date of sale and transaction id

  Scenario:
    Given I am on the private successful summary page
    And   I can see the notify another sale and finish button
    When  I click on notify another sale button
    Then  I should be taken to notify another sale page

  Scenario:
    Given I am on the private notify another sale page
    When  I select "Yes" and click next
    Then  I should be taken to vehicle look up page
    And   the trader details should be played back

  Scenario:
    Given I am on the private notify another sale page
    When  I select "No" and click next
    Then  I should be taken to setup trade details page
    And   trader input fields should be blank

  Scenario:
    Given I am on the private complete and confirm page with failure data
    When  I click on confirm sale button without any validation errors
    Then  I should be taken to failure  page
    And   I can see the details of transaction id with failure screen
