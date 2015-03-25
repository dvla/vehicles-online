@working
Feature: Back Navigation From Complete and Confirm page

  Scenario: Back Navigation from  Complete and Confirm page to trader details page
    Given the user on complete and confirm page without any validation errors
    When  the user clicks on back button on complete and confirm page
    Then  the user should taken to vehicle lookUp Page
    When  the user click on the back button in vehicle look up page
    Then  navigate to business choose address page
    When  the user click on the back button on business choose address page
    Then  the user will navigate to traderDetailsPage
