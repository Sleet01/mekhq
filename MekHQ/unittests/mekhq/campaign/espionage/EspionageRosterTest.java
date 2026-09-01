package mekhq.campaign.espionage;

import megamek.Version;
import megamek.common.net.marshalling.SanityInputFilter;
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
import java.io.ObjectInputFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EspionageRosterTest {
    static Campaign campaign;

    @BeforeAll
    static void setup() {
        campaign = Mockito.mock(Campaign.class);
        if (ObjectInputFilter.Config.getSerialFilter() == null) {
            ObjectInputFilter.Config.setSerialFilter(new SanityInputFilter());
        }
    }

    @BeforeEach
    void setUp() {
    }

    String toXML(EspionageRoster roster) throws IOException {
        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            roster.writeToXML(campaign, pw, 0);
            xmlBlock = sw.toString();
        }
        return xmlBlock;
    }

    EspionageRoster fromXML(String xmlBlock) throws IOException, ParserConfigurationException, SAXException {
        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        return EspionageRoster.generateInstanceFromXML( node, campaign, new Version());
    }

    @Test
    void writeToXML() throws IOException {
        EspionageRoster roster = new EspionageRoster(1);
        roster.setDefaultCoverJob("Tinker");
        roster.assignPerson(UUID.randomUUID());
        roster.assignPerson(UUID.randomUUID(), EspionageRoster.UNASSIGNED_SOI_ID, "Tailor", "Smiley");

        String xml = toXML(roster);

        assertTrue(xml.contains("Tinker"));
        assertTrue(xml.contains("Tailor"));
        assertTrue(xml.contains("Smiley"));

    }

    @Test
    void generateInstanceFromXML() {
        String pregenUUID = "31b36cbd-10af-4c7b-81a0-acae5508d18e";
        int playerId = 2;
        int soiId = 1;

        StringBuilder xmlBlock = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        xmlBlock.append(
              String.format("\n<espionageRoster playerId=\"%d\" type=\"mekhq.campaign.espionage.EspionageRoster\">",
                    playerId)
              )
              .append("\n\t<defaultCoverJob>Tinker</defaultCoverJob>")
              .append("\n\t<assignments>")
              .append("\n\t\t<memberAssignment>").append(pregenUUID)
              .append(String.format(",%d,Soldier,</memberAssignment>", soiId))
              .append("\n\t</assignments>\n</espionageRoster>");

        EspionageRoster roster;
        try {
            roster = fromXML(xmlBlock.toString());
            assertEquals(2, roster.getPlayerId());
            EspionageRoster.Assignment assignment = roster.getPersonAssignment(UUID.fromString(pregenUUID));
            assertEquals(1, assignment.soiId());
            assertEquals("Soldier", assignment.coverJob());
            assertEquals("", assignment.alias());
        } catch (Exception e) {
            fail("Failure to read from XML!");
        }
    }
}
