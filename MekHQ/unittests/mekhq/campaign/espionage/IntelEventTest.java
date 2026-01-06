package mekhq.campaign.espionage;

import megamek.Version;
import megamek.common.Player;
import mekhq.campaign.Campaign;
import mekhq.campaign.espionage.IntelEvent.EventState;
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
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class IntelEventTest {

    static Campaign campaign;

    @BeforeAll
    static void setup() {
        campaign = Mockito.mock(Campaign.class);
    }

    @BeforeEach
    void setUp() {
    }

    @Test
    void getLocationParts() {
    }

    @Test
    void getPrerequisites() {
    }

    @Test
    void addPrerequisite() {
    }

    @Test
    void addPrerequisites() {
    }

    @Test
    void removePrerequisites() {
    }

    @Test
    void getState() {
    }

    @Test
    void update() {
    }

    @Test
    void testFullXMLSerDes() throws IOException, ParserConfigurationException, SAXException {
        // Start region configuration
        LocalDate mockCurrentDate = LocalDate.of(2035, 7, 10);
        LocalDate mockStart = LocalDate.of(2035, 7, 14);
        LocalDate mockEnd = LocalDate.of(2035, 7, 21);
        Player player = new Player(1, "Test Player");
        when(campaign.getLocalDate()).thenReturn(mockCurrentDate);
        when(campaign.getPlayer()).thenReturn(player);

        // Configure EspionageManager,
        int soiId = 1;
        EspionageManager manager = EspionageManager.getInstance();
        manager.setCampaign(campaign);
        SphereOfInfluence sphereOfInfluence = new SphereOfInfluence(soiId, 1, "Test SOI", "Test Sphere Of Influence",
              new HashMap<>(), new ArrayList<>(), new HashMap<>());

        IntelRating intelRating = new IntelRating(4);
        StaticIntelRating staticIntelRating = new StaticIntelRating(2);

        // Set intel rating and static intel rating for player (1) and fictional bot force, (2)
        sphereOfInfluence.setActorRatingForFoe(1, 2, intelRating);
        sphereOfInfluence.setActorRatingForFoe(2, 1, staticIntelRating);

        // Add an item to the SOI - some macguffin of interest.
        // The item has an ID but its owner and possessor are unset as yet.
        int itemId = 5;
        IntelItem intelItem = new IntelItem(mockCurrentDate, itemId, IntelItem.UNSET_ID, IntelItem.UNSET_ID,
              "The Malted Falchion", "A celebrated ceremonial sabre encrusted with small gems.", new ArrayList<>());
        intelItem.setDiscovered(false);

        // Create the outcome with its test.
        IntelOutcome outcome = new IntelOutcome();
        outcome.setTitle("You found the sword!");
        outcome.setBeneficiaryId(player.getId());
        outcome.setDescription("You have successfully uncovered and taken possession of this legendary artifact!");
        int playerId = player.getId();
        // Create the test function that determines if this outcome happens
        outcome.setTestFunction((ISerializableSupplier<Boolean>) () -> {
            // This should be replaced with a simple int, int or string lookup at some point.
            EspionageManager manager1 = EspionageManager.getInstance();
            SphereOfInfluence soi = (manager1 != null) ? manager1.getSphereOfInfluence(soiId) : null;
            IntelItem item = (soi != null) ? soi.getIntelItem(itemId) : null;
            return (item != null && (item.isDiscovered() && item.isCaptured() && item.getPossessorId() == playerId));
        });

        // Create the apply function that is run when the outcome should happen
        // Note: it is entirely possible for the bot force to get this outcome instead of the player, depending
        // on events that set the current owner
        outcome.setApplyFunction((ISerializableRunnable) () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            SphereOfInfluence soi = (manager1 != null) ? manager1.getSphereOfInfluence(soiId) : null;
            IntelItem item = (soi != null) ? soi.getIntelItem(itemId) : null;
            if (item != null) {
                item.setOwnerId(playerId);
            }
        });

        // Make sure things get added as they should
        intelItem.addOutcome(outcome);
        sphereOfInfluence.addIntelItem(intelItem);
        manager.addSphereOfInfluence(sphereOfInfluence);

        // Create an IntelEvent that will modify the campaign or SOI state based on some criteria.
        int eventId = 3;
        IntelEvent testEvent = new IntelEvent();
        testEvent.setTitle("Test Event - Dig Through The Archives");
        testEvent.setDescription("An event that contains simple prerequisite, test, and outcome functions.\nShould " +
                                       "exercise all XML write steps.");
        testEvent.setEventId(eventId);
        testEvent.setStartDate(mockStart);
        testEvent.setEndDate(mockEnd);
        testEvent.setLocationParts(new ArrayList<>(List.of("McKenna (Planet)", "James City (City)", "AO Hex B2 (AO)",
              "Planetary Institute")));

        // This event will modify the referenced intelItem
        testEvent.addItemId(itemId);

        // Prereq - determines if the event can be run or not.
        // In this case the event must simply have been started, which is usually handled by update().
        ISerializableSupplier<Boolean> prereqCheck = () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            IntelEvent event = (manager1 != null) ? manager1.getSphereOfInfluence(soiId).findIntelEvent(playerId, eventId) : null;
            return ((event != null) && event.getState() != IntelEvent.EventState.NOT_STARTED);
        };
        testEvent.addPrerequisite(new IntelEventPrerequisite(
              "Spend a week in the archives digging through boxes",
              prereqCheck)
        );

        // Test function - determines if the event is complete.
        // This has to be checked before the expiration check happens!
        ISerializableSupplier<Boolean> testFunction = () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            Campaign campaign = manager1.getCampaign(); // Should never be null...
            LocalDate finalDay = LocalDate.parse(mockEnd.toString());
            LocalDate currentDay = (campaign != null) ? campaign.getLocalDate() : null;
            IntelEvent event = (manager1 != null) ? manager1.getSphereOfInfluence(soiId).findIntelEvent(playerId, eventId) : null;
            return ((event != null) && (campaign != null) && (currentDay != null) && (
                  event.getState() != IntelEvent.EventState.NOT_STARTED && (
                        currentDay.equals(finalDay)))
            );
        };
        testEvent.addTestFunction(testFunction);

        // Outcome: will be achieved when the event is completed, and change the IntelItem ownership to player
        IntelOutcome eventOutcome = new IntelOutcome();
        eventOutcome.setTitle("A Curious Relic Is Discovered!");
        eventOutcome.setDescription("Operatives searching through the abandoned archives recovered a heavy case!");

        // Inline test function definition: returns true if the event has completed (see above)
        eventOutcome.setTestFunction((ISerializableSupplier<Boolean>) () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            IntelEvent event = (manager1 != null) ? manager1.getSphereOfInfluence(soiId).findIntelEvent(playerId, eventId) : null;
            return (event != null && event.getState() == IntelEvent.EventState.COMPLETED);
        });

        // Apply function: sets the previously-defined IntelItem to Discovered and Captured, and the Possessor ID to
        // player's ID.
        eventOutcome.setApplyFunction((ISerializableRunnable) () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            IntelEvent event = (manager1 != null) ? manager1.getSphereOfInfluence(soiId).findIntelEvent(playerId, eventId) : null;
            if (event != null) {
                event.setState(EventState.SUCCEEDED);
            }
            IntelItem item = (manager1 != null) ? manager1.getSphereOfInfluence(soiId).getIntelItem(itemId) : null;
            if (item != null) {
                item.setDiscovered(true);
                item.setCaptured(true);
                item.setPossessorId(playerId);
            };
        });
        testEvent.addOutcome(eventOutcome);
        // end region configuration

        // start region SerDes
        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            testEvent.writeToXML(campaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        IntelEvent deserialized = IntelEvent.generateInstanceFromXML( node, campaign, new Version());
        // end region SerDes

        // start region tests
        // Event should still be un-started as we have not advanced the day
        assertEquals(EventState.NOT_STARTED, deserialized.getState());
        assertEquals(1, deserialized.getTestFunctions().size());

        // Add the event to the SOI, as would happen when loading a campaign save
        sphereOfInfluence.getEventsMap().put(playerId, new ArrayList<>(List.of(deserialized)));

        // Advance the date and run an update
        when(campaign.getLocalDate()).thenReturn(mockStart);
        String initialReport = sphereOfInfluence.update(mockStart);
        assertEquals(EventState.STARTED, deserialized.getState());

        // Advance the date again and update
        when(campaign.getLocalDate()).thenReturn(mockEnd);
        String completionReport = sphereOfInfluence.update(mockEnd);
        assertEquals(EventState.SUCCEEDED, deserialized.getState());

        // Advance the date past the end of the event; event should be cleaned up
        when(campaign.getLocalDate()).thenReturn(mockEnd.plusDays(1));
        String finalReport = sphereOfInfluence.update(mockEnd.plusDays(1));
        assertNull(sphereOfInfluence.findIntelEvent(playerId, eventId));
        assertEquals(intelItem.getPossessorId(), playerId);
    }
}
