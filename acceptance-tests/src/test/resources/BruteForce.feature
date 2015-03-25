@working
Feature: Brute force prevention locks a vrm after 3 unsuccessful attempts to lookup a vehicle

  Scenario: After 3 unsuccessful attempts to lookup a vehicle the vrm is locked
    Given the user is on the vehicle lookup page
    When the user enters an incorrect vrm & doc ref number combination three times
    Then on the fourth attempt the vrm is locked

  Scenario: After 2 unsuccessful attempts to lookup a vehicle on the third successful attempt the vehicle is found and the user can progress
    Given the user is on the vehicle lookup page
    When the user enters an incorrect vrm & doc ref number combination two times
    Then on the third attempt the vehicle is found and the user can progress to the next page
