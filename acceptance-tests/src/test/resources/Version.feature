@working
Feature: to view version information of micro services and webapp

  Background:
    Given the user is on the version page

  Scenario: Version and runtime information should be shown for the webapp and microservices
    Then The user should be able to see version and runtime information for the webapp
    Then The user should be able to see version and runtime information for the microservices
