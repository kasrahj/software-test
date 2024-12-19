Feature: Add reservation
  Add a reservation for the user
  Scenario: Adding a new reservation
    Given Users new reservation
    When User add the reservation
    Then the reservation should be added with reservation number 0

  Scenario: Adding multiple reservations
    Given Users has 3 reservations
    When User add all the reservations
    Then they should be added with unique reservation numbers starting from 0
