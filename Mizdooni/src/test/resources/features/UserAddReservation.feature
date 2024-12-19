Feature: Add reservation
  Add a reservation for the user
  Scenario: Add reservation
    Given User is logged in
    When User add a reservation
    Then User should see the reservation in the list
