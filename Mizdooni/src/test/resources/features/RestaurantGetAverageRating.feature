Feature: Get average rating
  Gets average rating of the restaurant

  Scenario: Restaurant doesn't have a review
    Given Restaurant doesn't have any reviews
    When Get the average rating of the restaurant
    Then I should see zero as the average rating

  Scenario: Restaurant has reviews
    Given Restaurant has reviews with ratings "3, 4, 5"
    When Get the average rating of the restaurant
    Then I should see 4 as the average rating
