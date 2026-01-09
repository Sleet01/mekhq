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

import static org.junit.jupiter.api.Assertions.*;

class IntelItemTest {

    static Campaign campaign;

    @BeforeAll
    static void setup() {
        campaign = Mockito.mock(Campaign.class);
    }

    @Test
    void testIntelItemXMLSerDes() throws IOException, ParserConfigurationException, SAXException {
        EspionageManager espionageManager = EspionageManager.getInstance();
        espionageManager.setCampaign(campaign);

        int soiId = 5;
        SphereOfInfluence soi = new SphereOfInfluence();
        soi.setSoiId(soiId);

        espionageManager.addSphereOfInfluence(soi);

        int playerId = 1;
        int botId = 2;
        int itemId = 3;

        IntelItem testItem = new IntelItem();
        testItem.setItemId(itemId);
        testItem.setItemName("MacGuffin");
        testItem.setItemDescription("It's got what the Inner Sphere craves.");
        testItem.setDiscovered(true);
        testItem.setStartDate(LocalDate.of(3052, 12, 31));
        testItem.setOwnerId(botId);
        testItem.setPossessorId(playerId);

        // TestOutcome for serdes testing
        String outcomeTitle = "The End Result";
        IntelOutcome outcome = new IntelOutcome();
        outcome.setTitle(outcomeTitle);
        outcome.setDescription("What happens to the carrier TestItem no matter what.");
        outcome.setBeneficiaryId(botId);

        // Test function, always true
        outcome.setTestFunction((ISerializableSupplier<Boolean>) () -> {
            return true;
        });

        // Apply function, requires lookups to work
        outcome.setApplyFunction((ISerializableRunnable) () -> {
            EspionageManager manager = EspionageManager.getInstance();
            SphereOfInfluence soi2 = (manager != null) ? manager.getSphereOfInfluence(soiId) : null;
            IntelItem item = (soi2 != null) ? soi2.getIntelItem(itemId) : null;
            if (item != null) {
                item.setOwnerId(playerId);
                item.setCaptured(true);
            }
        });
        testItem.addOutcome(outcome);

        // start region SerDes
        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            testItem.writeToXML(campaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        IntelItem deserialized = IntelItem.generateInstanceFromXML( node, campaign, new Version());
        // end region SerDes

        assertNotNull(deserialized);
        assertEquals(testItem.getItemId(), deserialized.getItemId());
        assertEquals(testItem.getItemName(), deserialized.getItemName());

        // Add item to soi _here_, since we didn't load the entire SOI and we want to act on the loaded IntelItem
        soi.addIntelItem(deserialized);

        // Test IntelOutcome still works
        IntelOutcome deserializedOutcome = deserialized.getOutcomeByTitle(outcomeTitle);
        assertNotNull(deserializedOutcome);
        assertEquals(outcome.getTitle(), deserializedOutcome.getTitle());
        assertEquals(outcome.getDescription(), deserializedOutcome.getDescription());

        if (deserializedOutcome.checkAchieved()) {
            deserializedOutcome.apply();
        }

        assertTrue(deserialized.isCaptured());
        assertEquals(playerId, deserialized.getOwnerId());
    }

}
