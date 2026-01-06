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
import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;

public class IntelItem implements Serializable {
    protected static final MMLogger LOGGER = MMLogger.create(IntelItem.class);

    public final static int UNSET_ID = -1;

    private LocalDate startDate;
    private int itemId;
    private int ownerId;
    private int possessorId;
    private String itemName;
    private String itemDescription;
    private boolean discovered = false;
    private boolean captured = false;
    private boolean deciphered = false;
    private boolean delivered = false;
    private boolean destroyed = false;
    private boolean escaped = false;

    // IntelOutcomes will contain reward items, so that asymmetrical awards can be implemented easily
    private ArrayList<IntelOutcome> outcomes;

    public IntelItem() {
        // Use now as the default date; all IntelItems created this way will immediately appear
        this(LocalDate.now(), UNSET_ID, UNSET_ID, UNSET_ID, "", "", new ArrayList<>());
    }

    public IntelItem(LocalDate startDate, int itemId, int ownerID, int possessorId,
          String itemName, String itemDescription, ArrayList<IntelOutcome> outcomes) {
        this.startDate = startDate;
        this.itemId = itemId;
        this.ownerId = ownerID;
        this.possessorId = possessorId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.outcomes = outcomes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getPossessorId() {
        return possessorId;
    }

    public void setPossessorId(int possessorId) {
        this.possessorId = possessorId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public boolean isDeciphered() {
        return deciphered;
    }

    public void setDeciphered(boolean deciphered) {
        this.deciphered = deciphered;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isEscaped() {
        return escaped;
    }

    public void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    // SOI and/or Manager will likely directly edit these
    public void addOutcome(IntelOutcome outcome) {
        this.outcomes.add(outcome);
    }

    public void addOutcomes(ArrayList<IntelOutcome> outcomes) {
        this.outcomes.addAll(outcomes);
    }

    public ArrayList<IntelOutcome> getOutcomes() {
        return outcomes;
    }

    public @Nullable IntelOutcome getOutcomeByTitle(String title) {
        for (IntelOutcome outcome : outcomes) {
            if (outcome.getTitle().equals(title)) {
                return outcome;
            }
        }
        return null;
    }

    // SOI and/or Manager will likely edit these
    public void setOutcomes(ArrayList<IntelOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public void removeOutcome(IntelOutcome outcome) {
        this.outcomes.remove(outcome);
    }

    public void removeOutcomes(ArrayList<IntelOutcome> outcomes) {
        this.outcomes.removeAll(outcomes);
    }

    public String listOutcomeEntries() {
        StringBuilder outcomeList = new StringBuilder();
        for (IntelOutcome result : outcomes) {
            outcomeList.append(result.toString());
        }
        return outcomeList.toString();
    }

    public ArrayList<Object> listLinkedObjects() {
        ArrayList<Object> linkedObjects = new ArrayList<>();
        for (IntelOutcome outcome : outcomes) {
            linkedObjects.addAll(outcome.getLinkedObjects());
        }
        return linkedObjects;
    }

    public static IntelItem generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        IntelItem retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (IntelItem) Class.forName(className).getDeclaredConstructor().newInstance();
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
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "intelItem", "itemId", itemId, "type", getClass());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "ownerId", ownerId);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "possessorId", possessorId);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "startDate", startDate.toString());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "itemName", itemName);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "description", itemDescription);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "stateFlags", discovered, captured, deciphered, delivered, destroyed, escaped);

        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "outcomes", "count", outcomes.size());
        for (IntelOutcome outcome : outcomes) {
            outcome.writeToXML(campaign, pw, indent);
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "outcomes");
        return indent;
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "intelItem");
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        // Level is stored as an attribute of the node
        try {
            itemId = Integer.parseInt(node.getAttributes().getNamedItem("level").getNodeValue());
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("ownerId")) {
                    ownerId = Integer.parseInt(item.getTextContent());
                } else if (item.getNodeName().equalsIgnoreCase("possessorId")) {
                    possessorId = Integer.parseInt(item.getTextContent());
                } else if (item.getNodeName().equalsIgnoreCase("startDate")) {
                    startDate = MHQXMLUtility.parseDate(item.getTextContent());
                } else if (item.getNodeName().equalsIgnoreCase("itemName")) {
                    itemName = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("description")) {
                    itemDescription = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("stateFlags")) {
                    boolean[] booleans = MHQXMLUtility.parseBooleanArray(item.getTextContent());
                    discovered = booleans[0];
                    captured = booleans[1];
                    deciphered = booleans[2];
                    delivered = booleans[3];
                    destroyed = booleans[4];
                    escaped = booleans[5];
                } else if (item.getNodeName().equalsIgnoreCase("outcomes")) {
                    NodeList outcomeNodes = item.getChildNodes();
                    for (int y = 0; y < outcomeNodes.getLength(); y++) {
                        IntelOutcome outcome = IntelOutcome.generateInstanceFromXML(outcomeNodes.item(y), campaign, version);
                        outcomes.add(outcome);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
