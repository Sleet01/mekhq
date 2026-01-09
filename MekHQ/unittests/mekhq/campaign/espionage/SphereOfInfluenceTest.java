package mekhq.campaign.espionage;

import megamek.Version;
import mekhq.campaign.Campaign;
import mekhq.utilities.MHQXMLUtility;
import org.junit.jupiter.api.BeforeAll;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SphereOfInfluenceTest {

    static Campaign campaign;

    @BeforeAll
    static void setup() {
        campaign = Mockito.mock(Campaign.class);
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSphereOfInfluenceXMLSerDes() throws IOException, ParserConfigurationException, SAXException {
        int soiId = 1;
        int missionId = 2;
        String title = "Edwards System Sphere of Influence";
        String description = "This is a test sphere of Influence";

        // SOI setup
        SphereOfInfluence soi = new SphereOfInfluence();
        soi.setSoiId(soiId);
        soi.setMissionId(missionId);
        soi.setTitle(title);
        soi.setDescription(description);

        // Intel ratings: 1 per actor per foe; typically 1:1 to begin with.
        int playerId = 1;
        int playerRatingScore = 12;
        int botId = 2;
        int botRatingScore = 3;
        IntelRating playerRating = new IntelRating(playerRatingScore);
        StaticIntelRating botRating = new StaticIntelRating(botRatingScore);
        soi.setActorRatingForFoe(playerId, botId, playerRating);
        soi.setActorRatingForFoe(botId, playerId, botRating);

        // IntelEvent for SOI
        int eventId = 3;
        IntelEvent testEvent = new IntelEvent();
        testEvent.setEventId(eventId);
        testEvent.setTitle("The Happening");
        testEvent.setDescription("It's gonna Happen.");
        testEvent.setStartDate(LocalDate.of(3025, 1, 1));
        testEvent.setEndDate(LocalDate.of(3025, 2, 1));
        soi.addEventForActor(playerId, testEvent);

        // IntelItem for SOI
        int itemId = 4;
        IntelItem testItem = new IntelItem();
        testItem.setItemId(itemId);
        testItem.setItemName("MacGuffin");
        testItem.setItemName("It's got what the Inner Sphere craves.");
        testItem.setDiscovered(true);
        testItem.setStartDate(LocalDate.of(3025, 1, 15));
        soi.addIntelItem(testItem);


        // start region SerDes
        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            soi.writeToXML(campaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        SphereOfInfluence deserialized = SphereOfInfluence.generateInstanceFromXML( node, campaign, new Version());
        // end region SerDes

        // Test values
        assertEquals(soiId, deserialized.getSoiId());
        assertEquals(missionId, deserialized.getMissionId());
        assertEquals(title, deserialized.getTitle());
        assertEquals(description, deserialized.getDescription());

        // Test IntelRatings
        Set<Integer> actorIds = soi.getActors();
        assertEquals(2, actorIds.size());
        assertTrue(actorIds.contains(playerId));
        assertTrue(actorIds.contains(botId));

        IntelRating playerBotDeserialized = deserialized.getActorRatingForFoe(playerId, botId);
        assertEquals(playerRatingScore, playerBotDeserialized.getForcesIntel().getLevel());

        IntelRating botPlayerDeserialized = deserialized.getActorRatingForFoe(botId, playerId);
        assertEquals(botRatingScore, botPlayerDeserialized.getForcesIntel().getLevel());

        // Test IntelEvents
        ArrayList<IntelEvent> playerEvents = soi.getEventsListForActor(playerId);
        assertFalse(playerEvents.isEmpty());
        assertEquals(eventId, playerEvents.get(0).getEventId());
        assertEquals(testEvent.getTitle(), playerEvents.get(0).getTitle());
        assertEquals(testEvent.getDescription(), playerEvents.get(0).getDescription());
        assertEquals(testEvent.getStartDate(), playerEvents.get(0).getStartDate());
        assertEquals(testEvent.getEndDate(), playerEvents.get(0).getEndDate());

        // Test IntelItems
        ArrayList<IntelItem> deserializedItems = deserialized.getItems();
        assertEquals(itemId, deserializedItems.get(0).getItemId());
        assertEquals(testItem.getItemName(), deserializedItems.get(0).getItemName());
        assertEquals(testItem.getItemDescription(), deserializedItems.get(0).getItemDescription());
        assertEquals(testItem.isDiscovered(), deserializedItems.get(0).isDiscovered());
        assertEquals(testItem.getStartDate(), deserializedItems.get(0).getStartDate());
    }
}
