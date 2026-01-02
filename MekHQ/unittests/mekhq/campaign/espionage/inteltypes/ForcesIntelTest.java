package mekhq.campaign.espionage.inteltypes;

import megamek.Version;
import megamek.common.units.Entity;
import mekhq.campaign.Campaign;
import mekhq.utilities.MHQXMLUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import static mekhq.campaign.espionage.inteltypes.ForcesIntel.DEFAULT_ID;
import static mekhq.campaign.espionage.inteltypes.BasicIntel.HIGHEST_LEVEL;
import static mekhq.campaign.espionage.inteltypes.BasicIntel.LOWEST_LEVEL;
import static org.junit.jupiter.api.Assertions.*;
import static testUtilities.MHQTestUtilities.getEntityForUnitTesting;

class ForcesIntelTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void getKnownEntitiesList() {
        ForcesIntel forcesIntel = new ForcesIntel();
        assertNotNull(forcesIntel.getKnownEntitiesList());

        assertThrows(IllegalArgumentException.class, () -> forcesIntel.setKnownEntitiesList(null));
        assertNotNull(forcesIntel.getKnownEntitiesList());

        assertEquals(0, forcesIntel.getKnownEntitiesList().size());
        forcesIntel.addKnownEntity("Some Mek, I dunno, a BigBoy 2000?", 1);
        assertEquals(1, forcesIntel.getKnownEntitiesList().size());
    }

    @Test
    void setKnownEntitiesList() {
        ForcesIntel forcesIntel = new ForcesIntel();
        forcesIntel.setKnownEntitiesList(
              new ArrayList(
                    List.of(
                          new SimpleEntry<>("Mek A", 1),
                          new SimpleEntry<>("Mek B", 2),
                          new SimpleEntry<>("Mek C", 3),
                          new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID)
                    )
              )
        );

        ArrayList knownEntitiesList = forcesIntel.getKnownEntitiesList();

        assertEquals(4, knownEntitiesList.size());
        assertEquals(new SimpleEntry<>("Mek A", 1), knownEntitiesList.get(0));
        assertEquals(new SimpleEntry<>("Mek B", 2), knownEntitiesList.get(1));
        assertEquals(new SimpleEntry<>("Mek C", 3), knownEntitiesList.get(2));
        assertEquals(new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID), knownEntitiesList.get(3));
    }

    @Test
    void addEntityByName() {
        // Make sure we store both entries but mark them as "default ID", that is, unconfirmed.
        ForcesIntel forcesIntel = new ForcesIntel();
        forcesIntel.addEntityByName("MAD-3R");
        forcesIntel.addEntityByName("CPLT-C1");

        ArrayList knownEntitiesList = forcesIntel.getKnownEntitiesList();
        assertEquals(2, knownEntitiesList.size());
        assertEquals(new SimpleEntry<>("MAD-3R", DEFAULT_ID), knownEntitiesList.get(0));
        assertEquals(new SimpleEntry<>("CPLT-C1", DEFAULT_ID), knownEntitiesList.get(1));
    }

    @Test
    void addKnownEntity() {
        ForcesIntel forcesIntel = new ForcesIntel();
        String fileName = "Rifleman RFL-9T";
        Entity entity = getEntityForUnitTesting(fileName, false);
        assertNotNull(entity);

        forcesIntel.addKnownEntity(entity.getFullChassis(), 1);
        ArrayList<SimpleEntry<String, Integer>> knownEntitiesList = forcesIntel.getKnownEntitiesList();
        SimpleEntry<String, Integer> entry = knownEntitiesList.get(0);
        assertEquals(entry.getKey(), entity.getFullChassis());

        // Test illegal name
        assertThrows(IllegalArgumentException.class, () -> forcesIntel.addKnownEntity(null, 1));
        assertThrows(IllegalArgumentException.class, () -> forcesIntel.addKnownEntity("", 1));
        assertThrows(IllegalArgumentException.class, () -> forcesIntel.addKnownEntity("Some Big Mek", DEFAULT_ID));
    }

    @Test
    void getFullNameFromID() {
        ForcesIntel forcesIntel = new ForcesIntel();
        forcesIntel.setKnownEntitiesList(
              new ArrayList(
                    List.of(
                          new SimpleEntry<>("Mek A", 1),
                          new SimpleEntry<>("Mek B", 2),
                          new SimpleEntry<>("Mek C", 3),
                          new SimpleEntry<>("Suspected Heavy Vehicle", DEFAULT_ID)
                    )
              )
        );

        assertEquals("Mek A", forcesIntel.getFullNameFromID(1));
        assertEquals("Mek B", forcesIntel.getFullNameFromID(2));
        assertEquals("Mek C", forcesIntel.getFullNameFromID(3));
        assertNull(forcesIntel.getFullNameFromID(4));
    }

    @Test
    void getRandomEntityName() {
        ForcesIntel forcesIntel = new ForcesIntel();
        forcesIntel.setKnownEntitiesList(
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
            String randomName = forcesIntel.getRandomEntityName();
            assertTrue(names.contains(randomName));
        }
    }

    @Test
    void setLevel() {
        ForcesIntel forcesIntel = new ForcesIntel();
        assertEquals(0, forcesIntel.getLevel());
        for (int level: List.of(LOWEST_LEVEL, -6, -1, 0, 1, 6, HIGHEST_LEVEL)) {
            forcesIntel.setLevel(level);
            assertEquals(level, forcesIntel.getLevel());
        }

        assertThrows(IllegalArgumentException.class, () -> forcesIntel.setLevel(-1 + LOWEST_LEVEL));
        assertThrows(IllegalArgumentException.class, () -> forcesIntel.setLevel(1 + HIGHEST_LEVEL));
    }

    @Test
    void testSerializeToXML() throws IOException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        ForcesIntel forcesIntel = new ForcesIntel(10);
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            forcesIntel.writeToXML(mockCampaign, pw, 0);

            assertEquals("<intel level=\"10\" type=\"mekhq.campaign.espionage.inteltypes.ForcesIntel\">" +
                               "\t<locked>false</locked>\t<knownEntities>\t</knownEntities></intel>",
                  sw.toString().replaceAll("\\n|\\r\\n", ""));
        }
    }

    @Test
    void testDeserializeFromXML() throws IOException, ParserConfigurationException, SAXException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        int level = 6;
        String entityDesc = "Mek A";
        int entityId = 13;

        ForcesIntel forcesIntel = new ForcesIntel(level);
        forcesIntel.addKnownEntity(entityDesc, entityId);

        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            forcesIntel.writeToXML(mockCampaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        ForcesIntel deserialized = ForcesIntel.generateInstanceFromXML( node, mockCampaign,new Version());

        assertEquals(level, deserialized.getLevel());
        assertEquals(entityDesc, deserialized.getKnownEntitiesList().get(0).getKey());
        assertEquals(entityId, deserialized.getKnownEntitiesList().get(0).getValue());
    }
}
