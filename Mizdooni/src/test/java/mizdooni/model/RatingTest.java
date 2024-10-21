package mizdooni.model;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
public class RatingTest {

    Rating rating;

    @BeforeEach
    void setUp() {
        rating = new Rating();
    }

    @Test
    void testGetStarCount_RoundDown() {
        rating.overall = 4.2;
        assertEquals(4, rating.getStarCount(), "Star count should be rounded down to 4");
    }

    @Test
    void testGetStarCount_RoundUp() {
        rating.overall = 4.7;
        assertEquals(5, rating.getStarCount(), "Star count should be rounded up to 5");
    }

    @Test
    void testGetStarCount_MaxValue() {
        rating.overall = 5.8;
        assertEquals(5, rating.getStarCount(), "Star count should be capped at 5");
    }

    @Test
    void testGetStarCount_MinValue() {
        rating.overall = 2.3;
        assertEquals(2, rating.getStarCount(), "Star count should be rounded to 2");
    }

    @Test
    void testGetStarCount_Zero() {
        rating.overall = 0;
        assertEquals(0, rating.getStarCount(), "Star count should be rounded to 0");
    }
}
