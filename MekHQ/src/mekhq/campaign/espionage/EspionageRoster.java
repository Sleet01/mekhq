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
import mekhq.campaign.personnel.Person;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Track personnel connected to the Espionage tab
 */
public class EspionageRoster {
    private static final MMLogger LOGGER = MMLogger.create(EspionageRoster.class);

    // Record assignment info for each Person
    public record Assignment (int soiId, String coverJob, String alias) {}

    public static final int UNASSIGNED_SOI_ID = -1;
    private final HashMap<UUID, Assignment> memberAssignments;
    private int playerId = -1;
    private String defaultCoverJob = "Clerk";

    public EspionageRoster() {
        this.memberAssignments = new HashMap<>();
    }

    public EspionageRoster(int playerId) {
        this();
        this.playerId = playerId;
    }

    public EspionageRoster(int playerId, Iterable<Person> assignees) {
        this(playerId);
        for (Person assignee: assignees) {
            memberAssignments.put(assignee.getId(), new Assignment(UNASSIGNED_SOI_ID, defaultCoverJob, ""));
        }
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getDefaultCoverJob() {
        return this.defaultCoverJob;
    }

    public void setDefaultCoverJob(String job) {
        this.defaultCoverJob = job;
    }

    public void assignPerson(UUID memberUUID, int soiId, String coverJob, String alias) {
        Assignment current = memberAssignments.get(memberUUID);
        if (current == null) {
            memberAssignments.put(memberUUID, new Assignment(soiId, coverJob, alias));
        }
    }

    public void assignPerson(Person member, int soiId, String coverJob, String alias) {
        assignPerson(member.getId(), soiId, coverJob, alias);
    }

    public void assignPerson(UUID memberUUID) {
        assignPerson(memberUUID, UNASSIGNED_SOI_ID, defaultCoverJob, "");
    }

    public void assignPerson(Person member) {
        assignPerson(member.getId(), UNASSIGNED_SOI_ID, defaultCoverJob, "");
    }

    public void removePerson(UUID memberUUID) {
        memberAssignments.remove(memberUUID);
    }

    public void removePerson(Person member) {
        removePerson(member.getId());
    }

    public void unassignPerson(UUID memberUUID) {
        if (memberAssignments.containsKey(memberUUID)) {
            memberAssignments.put(memberUUID, new Assignment(UNASSIGNED_SOI_ID, defaultCoverJob, ""));
        }
    }

    public void unassignPerson(Person member) {
        unassignPerson(member.getId());
    }

    public @Nullable Person getPersonFromUUID(UUID memberUUID) {
        UUID id = (memberAssignments.containsKey(memberUUID)) ? memberUUID : null;
        if (id != null) {
            Campaign campaign = EspionageManager.getInstance().getCampaign();
            return campaign.getPlayerForce().getHumanResources().getPerson(id);
        }
        return null;
    }

    public boolean containsPerson(UUID memberUUID) {
        return memberAssignments.containsKey(memberUUID);
    }

    public @Nullable Assignment getPersonAssignment(UUID memberUUID) {
        return memberAssignments.get(memberUUID);
    }

    public @Nullable Assignment getPersonAssignment(Person member) {
        return getPersonAssignment(member.getId());
    }

    public void setPersonCoverJob(UUID memberUUID, String name) {
        Assignment current = memberAssignments.get(memberUUID);
        if (current != null) {
            memberAssignments.put(memberUUID, new Assignment(current.soiId, name, current.alias));
        }
    }

    public void reassignPersonToSOI(UUID memberUUID, int soiId) {
        Assignment current = memberAssignments.get(memberUUID);
        if (current != null) {
            memberAssignments.put(memberUUID, new Assignment(soiId, current.coverJob, current.alias));
        }
        else {
            assignPerson(memberUUID, soiId, defaultCoverJob, "");
        }
    }

    public void setPersonAlias(UUID memberUUID, String alias) {
        Assignment current = memberAssignments.get(memberUUID);
        if (current != null) {
            memberAssignments.put(memberUUID, new Assignment(current.soiId, current.coverJob, alias));
        }
    }

    public Set<UUID> getAllUUIDs() {
        return memberAssignments.keySet();
    }

    public Set<Person> getAllPersons() {
        // This is okay because the EspionageManager _must_ exist if this instance exists
        Campaign campaign = EspionageManager.getInstance().getCampaign();
        Set<Person> persons = new HashSet<Person>();
        Person current = null;

        if (campaign != null) {
            mekhq.campaign.ForceHumanResources hr = campaign.getPlayerForce().getHumanResources();
            if (hr != null) {
                for (UUID memberUUID: memberAssignments.keySet()) {
                    current = hr.getPerson(memberUUID);
                    if (current != null) {
                        persons.add(current);
                    }
                }
            }
        }
        return persons;
    }

    public int getPersonCount() {
        return memberAssignments.size();
    }

    public int getPersonCount(int soiId) {
        int count = 0;
        for (Assignment assignment: memberAssignments.values()) {
            if (assignment.soiId == soiId) {
                count++;
            }
        }
        return count;
    }

    public static EspionageRoster generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        EspionageRoster retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (EspionageRoster) Class.forName(className).getDeclaredConstructor().newInstance();
            retVal.loadFieldsFromXmlNode(campaign, version, node);
        } catch (Exception ex) {
            LOGGER.error("Error generating EspionageRoster from XML!", ex);
        }

        return retVal;
    }

    public void writeToXML(Campaign campaign, final PrintWriter pw, int indent) {
        indent = writeToXMLBegin(campaign, pw, indent);
        writeToXMLEnd(pw, indent);
    }

    protected int writeToXMLBegin(Campaign campaign, final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "espionageRoster", "playerId", playerId, "type", getClass());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "defaultCoverJob", defaultCoverJob);
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "assignments");
        for (UUID key: memberAssignments.keySet()) {
            Assignment assignment = memberAssignments.get(key);
            // Attributes are: UUID, soiId, coverJobName, alias
            MHQXMLUtility.writeSimpleXMLTag(pw, indent, "memberAssignment", key.toString(),
                  String.format("%d", assignment.soiId), assignment.coverJob, assignment.alias);
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, indent--, "assignments");

        return indent;
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "espionageRoster");
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        // Player ID is stored as an attribute of the node
        try {
            playerId = Integer.parseInt(node.getAttributes().getNamedItem("playerId").getNodeValue());
        } catch (Exception e) {
            LOGGER.error("Error loading EspionageRoster instance!", e);
        }

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("defaultCoverJob")) {
                    defaultCoverJob = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("assignments")) {
                    NodeList assignmentNodes = item.getChildNodes();
                    for (int y = 0; y < assignmentNodes.getLength(); y++) {
                        Node assignmentNode = assignmentNodes.item(y);
                        if (assignmentNode.getNodeName().equalsIgnoreCase("memberAssignment")) {
                            ArrayList<String> assignmentParts = new ArrayList<String>(List.of(
                                  MHQXMLUtility.parseStringArray(assignmentNode.getTextContent()))
                            );
                            UUID memberUUID = UUID.fromString(assignmentParts.get(0));
                            int soiId = Integer.parseInt(assignmentParts.get(1));
                            String coverJob = assignmentParts.get(2);
                            String alias = (assignmentParts.size() == 4) ? assignmentParts.get(3) : "";

                            Assignment assignment = new Assignment(
                                  soiId,
                                  coverJob,
                                  alias
                            );
                            memberAssignments.put(memberUUID, assignment);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Error reading EspionageRoster entries!", e);
            }
        }
    }
}
