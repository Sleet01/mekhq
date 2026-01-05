package mekhq.campaign.espionage.inteltypes;

import megamek.Version;
import mekhq.campaign.Campaign;
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

import static org.junit.jupiter.api.Assertions.*;

class PositionIntelTest {

    @Test
    void testSerializeToXML() throws IOException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        PositionIntel positionIntel = new PositionIntel(12);
        positionIntel.addKnown(1);
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            positionIntel.writeToXML(mockCampaign, pw, 0);

            assertEquals("<positionIntel level=\"12\" type=\"mekhq.campaign.espionage.inteltypes.PositionIntel\">\t" +
                               "<locked>false</locked>\t<knownPositions>1</knownPositions></positionIntel>",
                  sw.toString().replaceAll("\\n|\\r\\n", ""));
        }
    }

    @Test
    void testDeserializeFromXML() throws IOException, ParserConfigurationException, SAXException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        int level = 6;
        int entityId = 13;

        PositionIntel positionIntel = new PositionIntel(level);
        positionIntel.addKnown(entityId);

        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            positionIntel.writeToXML(mockCampaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        PositionIntel deserialized = PositionIntel.generateInstanceFromXML( node, mockCampaign,new Version());

        assertEquals(level, deserialized.getLevel());
        assertEquals(entityId, deserialized.getKnownPositions().get(0));
    }
}
