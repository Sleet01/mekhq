/*
 * Copyright (C) 2025-2026 The MegaMek Team. All Rights Reserved.
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPL),
 * version 3 or (at your option) any later version,
 * as published by the Free Software Foundation.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * A copy of the GPL should have been included with this project;
 * if not, see <https://www.gnu.org/licenses/>.
 *
 * NOTICE: The MegaMek organization is a non-profit group of volunteers
 * creating free software for the BattleTech community.
 *
 * MechWarrior, BattleMech, `Mech and AeroTech are registered trademarks
 * of The Topps Company, Inc. All Rights Reserved.
 *
 * Catalyst Game Labs and the Catalyst Game Labs logo are trademarks of
 * InMediaRes Productions, LLC.
 *
 * MechWarrior Copyright Microsoft Corporation. MekHQ was created under
 * Microsoft's "Game Content Usage Rules"
 * <https://www.xbox.com/en-US/developers/rules> and it is not endorsed by or
 * affiliated with Microsoft.
 */

package mekhq.campaign.espionage;

import megamek.Version;
import megamek.common.units.Entity;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.Person;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;

public class IntelOutcome implements IResultEvaluator, Serializable {
    protected static final MMLogger LOGGER = MMLogger.create(IntelOutcome.class);

    // If added here, objects will be awarded to "winner" of the IntelOutcome outcome.
    private ArrayList<Object> linkedObjects;
    private String title;
    private String description;
    private int beneficiaryId;
    private ISerializableSupplier<Boolean> testFunction;
    private ISerializableRunnable applyFunction;

    public IntelOutcome() {
        this(new ArrayList<>());
    }

    public IntelOutcome(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public ArrayList<Object> getLinkedObjects() {
        return linkedObjects;
    }

    public void setLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public void addLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.addAll(linkedObjects);
    }

    public void addLinkedObject(Object linkedObject) {
        this.linkedObjects.add(linkedObject);
    }

    public void removeLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.removeAll(linkedObjects);
    }

    public void removeLinkedObject(Object linkedObject) {
        this.linkedObjects.remove(linkedObject);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public void setTestFunction(ISerializableSupplier<Boolean> testFunction) {
        this.testFunction = testFunction;
    }

    public void setApplyFunction(ISerializableRunnable applyFunction) {
        this.applyFunction = applyFunction;
    }

    // All subclasses must implement
    public boolean checkAchieved() {
        return (testFunction != null) ? testFunction.get() : false;
    }

    // All subclasses must implement
    public void apply() {
        if (applyFunction != null) {
            applyFunction.run();
        }
    }

    public String toString() {
        String representation = title;
        if (linkedObjects.size() > 0) {
            representation += (linkedObjects.size() == 1) ? " [1 item]" : String.format(" [%s items]",
              linkedObjects.size());
        }
        if (testFunction != null && testFunction.get()) {
            representation += " (Done)";
        }
        return representation;
    }

    public static IntelOutcome generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        IntelOutcome retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (IntelOutcome) Class.forName(className).getDeclaredConstructor().newInstance();
            retVal.loadFieldsFromXmlNode(campaign, version, node);

        } catch (Exception ex) {
            LOGGER.error("", ex);
        }

        return retVal;
    }

    public void writeToXML(Campaign campaign, final PrintWriter pw, int indent) {
        indent = writeToXMLBegin(campaign, pw, indent);
        writeToXMLEnd(pw, indent);
    }

    protected int writeToXMLBegin(Campaign campaign, final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "intelOutcome", "title", title, "type", getClass());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "description", description);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "beneficiaryId", beneficiaryId);
        MHQXMLUtility.writeSerialCDATA(pw, indent, "testFunction", testFunction);
        MHQXMLUtility.writeSerialCDATA(pw, indent, "applyFunction", applyFunction);
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "linkedObjects");
        for (Object linkedObject: linkedObjects) {
            if (linkedObject instanceof Person person) {
                person.writeToXML(pw, indent, campaign);
            } else if (linkedObject instanceof Entity entity) {
                pw.println(MHQXMLUtility.writeEntityToXmlString(entity, indent, null));
            }
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "linkedObjects");
        return indent;
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "intelOutcome");
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        // Level is stored as an attribute of the node
        try {
            title = node.getAttributes().getNamedItem("title").getNodeValue();
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("description")) {
                    description = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("beneficiaryId")) {
                    beneficiaryId = Integer.parseInt(item.getTextContent());
                } else if (item.getNodeName().equalsIgnoreCase("testFunction")) {
                    // CDATA entry should be the first child.
                    Node firstChild = item.getFirstChild();
                    if (firstChild.getNodeType() == Node.CDATA_SECTION_NODE) {
                        testFunction =
                              (ISerializableSupplier<Boolean>) MHQXMLUtility.parseSerialCDATA(firstChild.getNodeValue());
                    }
                } else if (item.getNodeName().equalsIgnoreCase("applyFunction")) {
                    // CDATA entry should be the first child.
                    Node firstChild = item.getFirstChild();
                    if (firstChild.getNodeType() == Node.CDATA_SECTION_NODE) {
                        applyFunction = (ISerializableRunnable) MHQXMLUtility.parseSerialCDATA(firstChild.getNodeValue());
                    }
                } else if (item.getNodeName().equalsIgnoreCase("linkedObjects")) {
                    NodeList objectChildren = item.getChildNodes();
                    for (int y = 0; y < objectChildren.getLength(); y++) {
                        Node itemChild = objectChildren.item(y);
                        if (itemChild.getNodeName().equalsIgnoreCase("person")) {
                            Person person = Person.generateInstanceFromXML(itemChild, campaign, version);
                            linkedObjects.add(person);
                        } else if (itemChild.getNodeName().equalsIgnoreCase("entity")) {
                            Entity entity = MHQXMLUtility.parseSingleEntityMul((Element) itemChild, campaign);
                            linkedObjects.add(entity);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
