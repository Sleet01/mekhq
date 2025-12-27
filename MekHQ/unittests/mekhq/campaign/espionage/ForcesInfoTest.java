package mekhq.campaign.espionage;

import megamek.common.units.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import static mekhq.campaign.espionage.ForcesInfo.DEFAULT_ID;
import static mekhq.campaign.espionage.RatingInfo.HIGHEST_LEVEL;
import static mekhq.campaign.espionage.RatingInfo.LOWEST_LEVEL;
import static org.junit.jupiter.api.Assertions.*;
import static testUtilities.MHQTestUtilities.getEntityForUnitTesting;

class ForcesInfoTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getKnownEntitiesList() {
        ForcesInfo forcesInfo = new ForcesInfo();
        assertNotNull(forcesInfo.getKnownEntitiesList());

        assertThrows(IllegalArgumentException.class, () -> forcesInfo.setKnownEntitiesList(null));
        assertNotNull(forcesInfo.getKnownEntitiesList());

        assertEquals(0, forcesInfo.getKnownEntitiesList().size());
        forcesInfo.addKnownEntity("Some Mek, I dunno, a BigBoy 2000?", 1);
        assertEquals(1, forcesInfo.getKnownEntitiesList().size());
    }

    @Test
    void setKnownEntitiesList() {
        ForcesInfo forcesInfo = new ForcesInfo();
        forcesInfo.setKnownEntitiesList(
              new ArrayList(
                    List.of(
                          new SimpleEntry<>("Mek A", 1),
                          new SimpleEntry<>("Mek B", 2),
                          new SimpleEntry<>("Mek C", 3),
                          new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID)
                    )
              )
        );

        ArrayList knownEntitiesList = forcesInfo.getKnownEntitiesList();

        assertEquals(4, knownEntitiesList.size());
        assertEquals(new SimpleEntry<>("Mek A", 1), knownEntitiesList.get(0));
        assertEquals(new SimpleEntry<>("Mek B", 2), knownEntitiesList.get(1));
        assertEquals(new SimpleEntry<>("Mek C", 3), knownEntitiesList.get(2));
        assertEquals(new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID), knownEntitiesList.get(3));
    }

    @Test
    void addEntityByName() {
        // Make sure we store both entries but mark them as "default ID", that is, unconfirmed.
        ForcesInfo forcesInfo = new ForcesInfo();
        forcesInfo.addEntityByName("MAD-3R");
        forcesInfo.addEntityByName("CPLT-C1");

        ArrayList knownEntitiesList = forcesInfo.getKnownEntitiesList();
        assertEquals(2, knownEntitiesList.size());
        assertEquals(new SimpleEntry<>("MAD-3R", DEFAULT_ID), knownEntitiesList.get(0));
        assertEquals(new SimpleEntry<>("CPLT-C1", DEFAULT_ID), knownEntitiesList.get(1));
    }

    @Test
    void addKnownEntity() {
        ForcesInfo forcesInfo = new ForcesInfo();
        String fileName = "Rifleman RFL-9T";
        Entity entity = getEntityForUnitTesting(fileName, false);
        assertNotNull(entity);

        forcesInfo.addKnownEntity(entity.getFullChassis(), 1);
        ArrayList<SimpleEntry<String, Integer>> knownEntitiesList = forcesInfo.getKnownEntitiesList();
        SimpleEntry<String, Integer> entry = knownEntitiesList.get(0);
        assertEquals(entry.getKey(), entity.getFullChassis());

        // Test illegal name
        assertThrows(IllegalArgumentException.class, () -> forcesInfo.addKnownEntity(null, 1));
        assertThrows(IllegalArgumentException.class, () -> forcesInfo.addKnownEntity("", 1));
        assertThrows(IllegalArgumentException.class, () -> forcesInfo.addKnownEntity("Some Big Mek", DEFAULT_ID));
    }

    @Test
    void getFullNameFromID() {
        ForcesInfo forcesInfo = new ForcesInfo();
        forcesInfo.setKnownEntitiesList(
              new ArrayList(
                    List.of(
                          new SimpleEntry<>("Mek A", 1),
                          new SimpleEntry<>("Mek B", 2),
                          new SimpleEntry<>("Mek C", 3),
                          new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID)
                    )
              )
        );

        assertEquals("Mek A", forcesInfo.getFullNameFromID(1));
        assertEquals("Mek B", forcesInfo.getFullNameFromID(2));
        assertEquals("Mek C", forcesInfo.getFullNameFromID(3));
        assertNull(forcesInfo.getFullNameFromID(4));
    }

    @Test
    void getRandomEntityName() {
        ForcesInfo forcesInfo = new ForcesInfo();
        forcesInfo.setKnownEntitiesList(
              new ArrayList(
                    List.of(
                          new SimpleEntry<>("Mek A", 1),
                          new SimpleEntry<>("Mek B", 2),
                          new SimpleEntry<>("Mek C", 3),
                          new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID)
                    )
              )
        );
        ArrayList<String> names = new ArrayList<String>(List.of("Mek A", "Mek B", "Mek C", "Suspected Heavy Vehicle"));
        for (int i = 0; i < 4; i++) {
            String randomName = forcesInfo.getRandomEntityName();
            assertTrue(names.contains(randomName));
        }
    }

    @Test
    void setLevel() {
        ForcesInfo forcesInfo = new ForcesInfo();
        assertEquals(0, forcesInfo.getLevel());
        for (int level: List.of(LOWEST_LEVEL, -6, -1, 0, 1, 6, HIGHEST_LEVEL)) {
            forcesInfo.setLevel(level);
            assertEquals(level, forcesInfo.getLevel());
        }

        assertThrows(IllegalArgumentException.class, () -> forcesInfo.setLevel(-1 + LOWEST_LEVEL));
        assertThrows(IllegalArgumentException.class, () -> forcesInfo.setLevel(1 + HIGHEST_LEVEL));
    }
}
