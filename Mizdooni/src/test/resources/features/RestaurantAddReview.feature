Feature: Add review
  Add a review for a restaurant

  Scenario: User doesn't have a review
    Given User doesn't have a review
    When I add a review
    Then the review should be added

  Scenario: User already has a review
    Given User already has a review
    When I add a review
    Then the review should be updated