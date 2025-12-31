package mekhq.campaign.espionage;

import mekhq.campaign.espionage.inteltypes.BasicIntel;
import mekhq.campaign.espionage.inteltypes.CounterIntel;
import mekhq.campaign.espionage.inteltypes.FinancialIntel;
import org.junit.jupiter.api.Test;

import static mekhq.campaign.espionage.IntelRating.FINANCIAL_NAME;
import static mekhq.campaign.espionage.IntelRating.intelAdjacencyMap;
import static org.junit.jupiter.api.Assertions.*;

class IntelRatingTest {

    @Test
    void getForcesIntel() {
        IntelRating rating = new IntelRating();
        assertEquals(0, rating.getForcesIntel().getLevel());
    }

    @Test
    void getPositionIntel() {
        IntelRating rating = new IntelRating(1);
        assertEquals(1, rating.getPositionIntel().getLevel());
    }

    @Test
    void getLogisticsIntel() {
        IntelRating rating = new IntelRating(-2);
        assertEquals(-2, rating.getLogisticsIntel().getLevel());
    }

    @Test
    void getPersonnelIntel() {
        IntelRating rating = new IntelRating(5);
        assertEquals(5, rating.getPersonnelIntel().getLevel());
    }

    @Test
    void getCommsIntel() {
        IntelRating rating = new IntelRating(-6);
        assertEquals(-6, rating.getCommsIntel().getLevel());
    }

    @Test
    void getFinancialIntel() {
        IntelRating rating = new IntelRating(12);
        assertEquals(12, rating.getFinancialIntel().getLevel());
    }

    @Test
    void getLocalIntel() {
        IntelRating rating = new IntelRating(-12);
        assertEquals(-12, rating.getLocalIntel().getLevel());
    }

    @Test
    void getCounterIntel() {
        assertThrows(IllegalArgumentException.class, () -> {IntelRating rating = new IntelRating(15);});
        IntelRating rating2 = new IntelRating(0);
        assertEquals(0, rating2.getCounterIntel().getLevel());

        rating2.getAnIntel(IntelRating.COUNTER_NAME);
        assertEquals(0, rating2.getCounterIntel().getLevel());

        rating2.improveAnIntel(IntelRating.COUNTER_NAME, 6);
        assertEquals(6, rating2.getCounterIntel().getLevel());
    }

    @Test
    void ageAllIntelByOne() {
        IntelRating rating = new IntelRating(5);
        rating.ageAllIntelByOne();
        for (String intelType: intelAdjacencyMap.keySet()) {
            assertEquals(4, rating.getAnIntel(intelType).getLevel());
        }

        rating = new IntelRating(-5);
        rating.ageAllIntelByOne();
        for (String intelType: intelAdjacencyMap.keySet()) {
            assertEquals(-4, rating.getAnIntel(intelType).getLevel());
        }
    }

    @Test
    void ageAllIntel() {
        // Positive levels
        int forcesLevel = 5;
        int positionLevel = 6;
        int logisticsLevel = 1;
        int personnelLevel = 3;

        // Negative levels
        int commsLevel = -1;
        int financialLevel = -2;
        int localLevel = -3;
        int counterLevel = -4;

        // Create a new IntelRating with the specified levels
        IntelRating rating = new IntelRating(forcesLevel, positionLevel, logisticsLevel, personnelLevel,
              commsLevel, financialLevel, localLevel, counterLevel);

        // Age all levels by two, that is, decrease positives towards 0 and increase negatives towards 0
        rating.ageAllIntel(2);

        // Positive levels decreased by 2, or go to 0 if <= 2 but > 0
        assertEquals(3, rating.getForcesIntel().getLevel());
        assertEquals(4, rating.getPositionIntel().getLevel());
        assertEquals(0, rating.getLogisticsIntel().getLevel());
        assertEquals(1, rating.getPersonnelIntel().getLevel());

        // Negative levels increased by 2, or go to 0 if >= -2 but < 0
        assertEquals(0, rating.getCommsIntel().getLevel());
        assertEquals(0, rating.getFinancialIntel().getLevel());
        assertEquals(-1, rating.getLocalIntel().getLevel());
        assertEquals(-2, rating.getCounterIntel().getLevel());
    }

    @Test
    void getAnIntel() {
        IntelRating rating = new IntelRating();
        // Confirm getForcesIntel() and getAnIntel("ForcesIntel") do the same thing.
        assertEquals(0, rating.getForcesIntel().getLevel());
        assertEquals(0, rating.getAnIntel(IntelRating.FORCES_NAME).getLevel());

        // Return null for wrong names
        assertNull(rating.getAnIntel("PokemonIntel"));
    }

    @Test
    void ageAnIntel() {
        IntelRating rating = new IntelRating();

        // Confirm correct name allows an Intel type to be aged
        CounterIntel counterIntel = rating.getCounterIntel();
        counterIntel.setLevel(12);
        rating.ageAnIntel(IntelRating.COUNTER_NAME, 5);
        assertEquals(7, rating.getCounterIntel().getLevel());

        // Assert no change for wrong names
        rating.ageAnIntel("CounterBlintel", 1);
        assertEquals(7, rating.getCounterIntel().getLevel());

        // Lock intel and try to age again.
        counterIntel.setLocked(true);
        rating.ageAnIntel(IntelRating.COUNTER_NAME, 3);
        assertEquals(7, rating.getCounterIntel().getLevel());

        // Unlock and age again.
        counterIntel.setLocked(false);
        rating.ageAnIntel(IntelRating.COUNTER_NAME, 3);
        assertEquals(4, rating.getCounterIntel().getLevel());
    }

    @Test
    void improveAnIntel() {
        IntelRating rating = new IntelRating();
        FinancialIntel financialIntel = rating.getFinancialIntel();

        // Start at 1
        financialIntel.setLevel(1);
        rating.improveAnIntel(IntelRating.FINANCIAL_NAME, 4);
        assertEquals(5, rating.getFinancialIntel().getLevel());
        // Confirm that the adjacent level improvement worked
        for(String adjName: intelAdjacencyMap.get(FINANCIAL_NAME)) {
            BasicIntel adjacent = rating.getAnIntel(adjName);
            assertEquals((int) Math.floor(4/2.0), adjacent.getLevel());
        }

        // Continue by improving the score 5 levels from
        rating.improveAnIntel(IntelRating.FINANCIAL_NAME, 7);
        assertEquals(12, rating.getFinancialIntel().getLevel());
        for(String adjName: intelAdjacencyMap.get(FINANCIAL_NAME)) {
            BasicIntel adjacent = rating.getAnIntel(adjName);
            assertEquals((int) Math.floor(4/2.0) + (int) Math.floor(7/2.0), adjacent.getLevel());
        }
    }
}
