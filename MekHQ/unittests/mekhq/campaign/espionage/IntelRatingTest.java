package mekhq.campaign.espionage;

import megamek.Version;
import mekhq.campaign.Campaign;
import mekhq.campaign.espionage.inteltypes.BasicIntel;
import mekhq.campaign.espionage.inteltypes.CounterIntel;
import mekhq.campaign.espionage.inteltypes.FinancialIntel;
import mekhq.campaign.personnel.Person;
import mekhq.utilities.MHQXMLUtility;
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
import java.util.UUID;

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


    @Test
    void testSerializeToXML() throws IOException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        IntelRating rating = new IntelRating(4);
        Person person = new Person("Bogdan", "Bogdanovich", mockCampaign, "PIR");
        person.setId(UUID.fromString("13bcf124-9468-4c40-9f2a-922b776ba7bb"));
        rating.addPerson(person);
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            rating.writeToXML(mockCampaign, pw, 0);

            assertEquals("<intelRating type=\"mekhq.campaign.espionage.IntelRating\">\t<forcesIntel level=\"4\" " +
                               "type=\"mekhq.campaign.espionage.inteltypes" +
                               ".ForcesIntel\">\t\t<mod>0</mod>\t\t<locked>false</locked>\t\t" +
                               "<knownEntities>\t\t</knownEntities>\t</forcesIntel>\t<positionIntel level=\"4\" type=" +
                               "\"mekhq.campaign.espionage.inteltypes.PositionIntel\">\t\t<mod>0</mod>\t\t<locked>false</locked>\t" +
                               "</positionIntel>\t<logisticsIntel level=\"4\" type=\"mekhq.campaign.espionage.inteltypes" +
                               ".LogisticsIntel\">\t\t<mod>0</mod>\t\t<locked>false</locked>\t</logisticsIntel>\t<personnelIntel " +
                               "level=\"4\" type=\"mekhq.campaign.espionage.inteltypes.PersonnelIntel\">\t\t<mod>0</mod>\t\t<locked>false" +
                               "</locked>\t</personnelIntel>\t<commsIntel level=\"4\" type=\"mekhq.campaign.espionage" +
                               ".inteltypes.CommsIntel\">\t\t<mod>0</mod>\t\t<locked>false</locked>\t</commsIntel>\t<financialIntel level" +
                               "=\"4\" type=\"mekhq.campaign.espionage.inteltypes.FinancialIntel\">\t\t<mod>0</mod>\t\t<locked>false" +
                               "</locked>\t</financialIntel>\t<localIntel level=\"4\" type=\"mekhq.campaign.espionage" +
                               ".inteltypes.LocalIntel\">\t\t<mod>0</mod>\t\t<locked>false</locked>\t</localIntel>\t<counterIntel level" +
                               "=\"4\" type=\"mekhq.campaign.espionage.inteltypes.CounterIntel\">\t\t<mod>0</mod>\t\t<locked>false" +
                               "</locked>\t</counterIntel>\t<assignedPersonIds>13bcf124-9468-4c40-9f2a-922b776ba7bb" +
                               "</assignedPersonIds></intelRating>",
                  sw.toString().replaceAll("\\n|\\r\\n", ""));
        }
    }

    @Test
    void testDeserializeFromXML() throws IOException, ParserConfigurationException, SAXException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        int level = 6;
        String entityDesc = "Mek A";
        int entityId = 13;
        String uuidString = "13bcf124-9468-4c40-9f2a-922b776ba7bb";
        Person person = new Person("Bogdan", "Bogdanovich", mockCampaign, "PIR");
        person.setId(UUID.fromString(uuidString));

        IntelRating rating = new IntelRating(level);
        rating.addPerson(person);
        rating.getForcesIntel().addKnownEntity(entityDesc, entityId);
        rating.getPositionIntel().addKnown(entityId);
        rating.improveAnIntel(IntelRating.FINANCIAL_NAME, 4);

        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            rating.writeToXML(mockCampaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        IntelRating deserialized = IntelRating.generateInstanceFromXML( node, mockCampaign,new Version());

        assertEquals(level, deserialized.getLocalIntel().getLevel());
        assertEquals(level + 4, deserialized.getFinancialIntel().getLevel());
        assertEquals(UUID.fromString(uuidString), deserialized.getAssignedPersonIDs().get(0));
        assertEquals(entityId, deserialized.getForcesIntel().getKnownEntitiesList().get(0).getValue());
    }
}
