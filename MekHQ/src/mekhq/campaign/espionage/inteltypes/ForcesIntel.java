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

package mekhq.campaign.espionage.inteltypes;

import megamek.Version;
import megamek.common.annotations.Nullable;
import mekhq.campaign.Campaign;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class ForcesIntel extends BasicIntel {

    public final static int DEFAULT_ID = -1;

    private ArrayList<SimpleEntry<String, Integer>> knownEntities;

    public ForcesIntel() {
        this(0, new ArrayList<SimpleEntry<String, Integer>>());
    }

    public ForcesIntel(int level) {
        this(level, new ArrayList<SimpleEntry<String, Integer>>());
    }

    public ForcesIntel(int level, ArrayList<SimpleEntry<String, Integer>> knownEntities) {
        super(level);
        this.knownEntities = knownEntities;
    }

    public ForcesIntel(ForcesIntel other) {
        super(other);
        this.knownEntities = new ArrayList<SimpleEntry<String, Integer>>();
        for (SimpleEntry<String, Integer> entry : other.knownEntities) {
            this.knownEntities.add(new SimpleEntry<>(entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Return a deep copy of knownEntities
     * @return ArrayList of SimpleEntries that are copies of all entries in knownEntities
     */
    public ArrayList<SimpleEntry<String, Integer>> getKnownEntitiesList() {
        return knownEntities.stream().map(SimpleEntry::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public void setKnownEntitiesList(ArrayList<SimpleEntry<String, Integer>> knownEntities) throws IllegalArgumentException {
        if (knownEntities == null) {
            throw new IllegalArgumentException("knownEntities cannot be null");
        }
        if (!locked) {
            this.knownEntities = knownEntities;
        }
    }

    /**
     * Add an entity entry with the default ID value.
     * @param fullName
     */
    public void addEntityByName(String fullName) {
        if (!locked) {
            addKnownEntityEntry(new SimpleEntry<>(fullName, DEFAULT_ID));
        }
    }

    /**
     * Add an entity with its associated ID value, for later comparison or lookups
     * @param fullName  Full entity name, for cache lookups
     * @param id        DEFAULT_ID if unknown/fake data, otherwise actual ID within the campaign
     */
    public void addKnownEntity(String fullName, int id) {
        if (fullName == null || fullName.isEmpty() || id < 0) {
            throw new IllegalArgumentException("fullName cannot be null or empty, ID must be positive");
        }
        if (!locked) {
            addKnownEntityEntry(new SimpleEntry<>(fullName, id));
        }
    }

    /**
     * Add a new SimpleEntry to the knownEntities member.
     * @param entityEntry containing the description of an entity and an ID number
     */
    public void addKnownEntityEntry(SimpleEntry<String, Integer> entityEntry) {
        if (!locked) {
            this.knownEntities.add(entityEntry);
        }
    }

    /**
     * Simple entity description lookup from ID
     * Returns a random entry if DEFAULT_ID is provided as the lookup value.
     * @param id
     * @return
     */
    public @Nullable String getFullNameFromID(int id) {
        if (id == DEFAULT_ID) {
            return getRandomEntityName();
        }
        for (SimpleEntry<String, Integer> entry : knownEntities) {
            if (entry.getValue() == id) {
                return entry.getKey();
            }
        }
        return null;
    }

    public @Nullable String getRandomEntityName() {
        SimpleEntry<String, Integer> entry = knownEntities.get(new Random().nextInt(knownEntities.size()));
        return entry.getKey();
    }

    public static ForcesIntel generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        return (ForcesIntel) BasicIntel.generateInstanceFromXML(node, campaign, version);
    }

    protected int writeToXMLBegin(Campaign campaign, final PrintWriter pw, int indent) {
        indent = super.writeToXMLBegin(campaign, pw, indent);
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "knownEntities");
        // Write list of known entities with description and ID (may be default)
        for (SimpleEntry<String, Integer> entry : knownEntities) {
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "entity", entry.getKey(),
                  entry.getValue().toString());
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "knownEntities");
        return indent;
    }


    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        super.loadFieldsFromXmlNode(campaign, version, node);

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);

            try {
                if (item.getNodeName().equalsIgnoreCase("knownEntities")) {
                    NodeList entries = item.getChildNodes();
                    for (int y = 0; y < entries.getLength(); y++) {
                        Node entry = entries.item(y);
                        if (entry.getNodeName().equalsIgnoreCase("entity")) {
                            String content = entry.getTextContent();
                            knownEntities.add(
                                  new SimpleEntry<>(
                                        content.split(",")[0],
                                        Integer.parseInt(content.split(",")[1])
                                  )
                            );
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
