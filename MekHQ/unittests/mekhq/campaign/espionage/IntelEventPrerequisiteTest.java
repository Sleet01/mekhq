package mekhq.campaign.espionage;

import megamek.Version;
import mekhq.campaign.Campaign;
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

import static org.junit.jupiter.api.Assertions.*;

class IntelEventPrerequisiteTest {

    @Test
    void testGetSupplierValues() {
        IntelEventPrerequisite prereq = new IntelEventPrerequisite();

        // getReason should return null when the supplier is not set; we cannot know why nothing has not succeeded!
        assertNull(prereq.getReason());

        // If the prerequisite returns false, that is, not met, then we will know the reason why!
        String negativeReason = "Result cannot be false!";
        prereq.setSupplier(new ISerializableSupplier<Boolean>() {
            @Override
            public Boolean get() {
                return false;
            }
        });
        prereq.setRequirement(negativeReason);
        assertFalse(prereq.get());
        assertEquals(negativeReason, prereq.getReason());

        // If the prerequisite returns true, that is, it is met, then the reason for failure is empty - success!
        prereq.setSupplier(new ISerializableSupplier<Boolean>() {
            @Override
            public Boolean get() {
                return true;
            }
        });
        assertTrue(prereq.get());
        assertNotEquals(negativeReason, prereq.getReason());
        assertEquals("", prereq.getReason());
    }

    @Test
    void testSerializeToXML() throws IOException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);

        // Extremely simple test function
        ISerializableSupplier<Boolean> testFunction = () -> {
            return true;
        };

        IntelEventPrerequisite prerequisite = new IntelEventPrerequisite("True must be true.", testFunction);

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            prerequisite.writeToXML(mockCampaign, pw, 0);

            assertEquals("<intelEventPrerequisite type=\"mekhq.campaign.espionage.IntelEventPrerequisite\">\t" +
                               "<requirement>True must be true.</requirement>\t<supplier>" +
                               "<![CDATA[rO0ABXNyACFqYXZhLmxhbmcuaW52b2tlLlNlcmlhbGl6ZWRMYW1iZGFvYdCULCk2hQIACkkADmltc" +
                               "GxNZXRob2RLaW5kWwAMY2FwdHVyZWRBcmdzdAATW0xqYXZhL2xhbmcvT2JqZWN0O0wADmNhcHR1cmluZ0NsYXN" +
                               "zdAARTGphdmEvbGFuZy9DbGFzcztMABhmdW5jdGlvbmFsSW50ZXJmYWNlQ2xhc3N0ABJMamF2YS9sYW5nL1N0c" +
                               "mluZztMAB1mdW5jdGlvbmFsSW50ZXJmYWNlTWV0aG9kTmFtZXEAfgADTAAiZnVuY3Rpb25hbEludGVyZmFjZU1" +
                               "ldGhvZFNpZ25hdHVyZXEAfgADTAAJaW1wbENsYXNzcQB+AANMAA5pbXBsTWV0aG9kTmFtZXEAfgADTAATaW1wb" +
                               "E1ldGhvZFNpZ25hdHVyZXEAfgADTAAWaW5zdGFudGlhdGVkTWV0aG9kVHlwZXEAfgADeHAAAAAGdXIAE1tMamF" +
                               "2YS5sYW5nLk9iamVjdDuQzlifEHMpbAIAAHhwAAAAAHZyADNtZWtocS5jYW1wYWlnbi5lc3Bpb25hZ2UuSW50Z" +
                               "WxFdmVudFByZXJlcXVpc2l0ZVRlc3QAAAAAAAAAAAAAAHhwdAAubWVraHEvY2FtcGFpZ24vZXNwaW9uYWdlL0l" +
                               "TZXJpYWxpemFibGVTdXBwbGllcnQAA2dldHQAFCgpTGphdmEvbGFuZy9PYmplY3Q7dAAzbWVraHEvY2FtcGFpZ" +
                               "24vZXNwaW9uYWdlL0ludGVsRXZlbnRQcmVyZXF1aXNpdGVUZXN0dAAkbGFtYmRhJHRlc3RTZXJpYWxpemVUb1h" +
                               "NTCRmOWQ5YjczMCQxdAAVKClMamF2YS9sYW5nL0Jvb2xlYW47cQB+AA4=]]>" +
                               "</supplier></intelEventPrerequisite>",
                  sw.toString().replaceAll("\\n|\\r\\n", ""));
        }
    }

    @Test
    void testDeserializeFromXML() throws IOException, ParserConfigurationException, SAXException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);
        IntelEventPrerequisite prerequisite = new IntelEventPrerequisite();
        String requirement = "Pilot Jane Weld must have deployed to the surefire suicide mission, 'Jane Weld Kills " +
                                   "The Draconis Combine'.";
        String mockCallChain = "This would be a call to a chain of 'get' functions to find that a specific person, " +
                                     "<Jane Weld>, was deployed to a specific Scenario, identified by various IDs.";
        String targetPersonName = "Jane Weld";

        // Mock confirming that a specific person was deployed to a specific mission.
        // This will serialize because a String is a base object type.
        ISerializableSupplier<Boolean> testFunction = () -> {
            return (mockCallChain.contains(targetPersonName));
        };

        prerequisite.setRequirement(requirement);
        prerequisite.setSupplier(testFunction);

        String xmlBlock;

        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            prerequisite.writeToXML(mockCampaign, pw, 0);
            xmlBlock = sw.toString();
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        IntelEventPrerequisite deserialized = IntelEventPrerequisite.generateInstanceFromXML( node, mockCampaign,new Version());

        // Assert string _contents_ are equal.
        assertTrue( prerequisite.getRequirement().equals(deserialized.getRequirement()));
        assertTrue(deserialized.get());
        // Functions perform the same, but are fundamentally _different objects_
        assertNotEquals(testFunction, deserialized.getSupplier());
    }
}
