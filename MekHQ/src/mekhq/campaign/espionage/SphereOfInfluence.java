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
import megamek.common.Player;
import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.campaign.espionage.IntelEvent.EventState;
import mekhq.campaign.mission.Mission;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class SphereOfInfluence {
    private static final MMLogger LOGGER = MMLogger.create(SphereOfInfluence.class);

    public static final int UNASSIGNED_MISSION = -1;

    private int soiId;
    private int missionId;
    private String title;
    private String description;

    // Events will initially be generated for just the player, but bot events may be added.
    // Use Player / Bot IDs as keys for now
    private HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap;
    private HashMap<Integer, ArrayList<IntelEvent>> eventsMap;

    // IntelItems are more free-floating.
    private ArrayList<IntelItem> items;

    public SphereOfInfluence() {
         this(0, UNASSIGNED_MISSION, "", "", new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    public SphereOfInfluence(
          int soiId,
          int missionId,
          String title,
          String description,
          HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap,
          ArrayList<IntelItem> items,
          HashMap<Integer, ArrayList<IntelEvent>> eventsMap
    ) {
        this.soiId = soiId;
        this.title = title;
        this.description = description;
        this.missionId = missionId;
        this.actorsRatingsMap = actorsRatingsMap;
        this.items = items;
        this.eventsMap = eventsMap;
    }

    public int getSoiId() {
        return soiId;
    }

    public void setSoiId(int soiId) {
        this.soiId = soiId;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
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

    public Set<Integer> getActors() {
        return actorsRatingsMap.keySet();
    }

    public HashMap<Integer, HashMap<Integer, IntelRating>> getActorsRatingsMap() {
        return actorsRatingsMap;
    }

    public void setActorsRatingsMap(HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap) {
        this.actorsRatingsMap = actorsRatingsMap;
    }

    public @Nullable IntelRating getActorRatingForFoe(int actorId, int foeId) {
        if (actorsRatingsMap.containsKey(actorId)) {
            return actorsRatingsMap.get(actorId).get(foeId);
        }
        return null;
    }

    public void addActorRatingForFoe(int actorId, int foeId) {
        setActorRatingForFoe(actorId, foeId, new IntelRating());
    }

    public void addActorRatingForFoe(int actorId, int foeId, int rating ) {
        setActorRatingForFoe(actorId, foeId, new IntelRating(rating));
    }

    /**
     * Directly update the IntelRating for a specific Actor:Foe pairing with a new IntelRating.
     * Note: destructive.
     *
     * @param actorId       The actor executing on this IntelRating
     * @param foeId         The target of the IntelRating
     * @param ratingForFoe  Initialized IntelRating with all its scores
     */
    public void setActorRatingForFoe(int actorId, int foeId, IntelRating ratingForFoe) {
        HashMap<Integer, IntelRating> actorRatingMap = (actorsRatingsMap.containsKey(actorId))
                                                             ? actorsRatingsMap.get(actorId) : new HashMap<>();

        actorRatingMap.put(foeId, ratingForFoe);
        actorsRatingsMap.put(actorId, actorRatingMap);
    }

    public HashMap<Integer, ArrayList<IntelEvent>> getEventsMap() {
        return eventsMap;
    }

    public void setEventsMap(HashMap<Integer, ArrayList<IntelEvent>> eventsMap) {
        this.eventsMap = eventsMap;
    }

    public void setEventsListForActor(int id, ArrayList<IntelEvent> eventsList) {
        eventsMap.put(id, eventsList);
    }

    public void addEventForActor(int id, IntelEvent event) {
        if (!eventsMap.containsKey(id)) {
            eventsMap.put(id, new ArrayList<>());
        }
        eventsMap.get(id).add(event);
    }

    public @Nullable ArrayList<IntelEvent> getEventsListForActor(int id) {
        return eventsMap.get(id);
    }

    public void addIntelItem(IntelItem item) {
        items.add(item);
    }

    public void addIntelItems(ArrayList<IntelItem> items) {
        this.items.addAll(items);
    }

    public @Nullable IntelItem getIntelItem(int itemId) {
        for (IntelItem item : items) {
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public ArrayList<IntelItem> getItems() {
        return items;
    }

    // May need refactoring.  Could just be an ID -> event hashmap since every participant in an event should be
    // listed.
    public @Nullable IntelEvent findIntelEvent(int participant, int id) {
        if (eventsMap != null && eventsMap.containsKey(participant)) {
            for (IntelEvent event : eventsMap.get(participant)) {
                if (event.getEventId() == id) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Call once after instantiating.
     * Create entries for every entity involved in this Mission:
     * 1. Player
     * 2. Opponent force
     *
     * Eventually these will be more detailed, and also include:
     * 3. Opponent faction (may provide bonuses)
     * 4. Player employer
     * 5. ???
     *
     * Initially we will only provide events for Player vs Opponent force, although
     * some may be written as, "prevent Opponent from X" as if the opfor is also generating events.
     * @param campaign  The current campaign; this may be refactored in the near future.
     * @param mission   Let the GUI decide which Mission to look at
     * @param botLevel  For the time being, set a static level for the bot
     */
    public void populate(Campaign campaign, Mission mission, int botLevel) {
        // Do nothing if the campaign and/or mission is not set
        if (campaign == null || mission == null) {
            return;
        }
        Player player = campaign.getPlayer();
        int playerId = player.getId();

        // Don't allow resetting a populated SOI here.
        if (actorsRatingsMap.containsKey(playerId)) {
            return;
        }

        // Set missionId
        missionId = mission.getId();

        // Placeholder values
        int botId = playerId + 1;

        // This will become more iterative once we have more actors in an SOI
        // Create IntelRatings for each _opponent_; this should be reciprocal
        IntelRating playerOnOpFor = new IntelRating();
        StaticIntelRating opForOnPlayer = new StaticIntelRating(botLevel);

        // Create hashmaps for lookups and populate with ratings
        // Keys are _opponent_ IDs here.
        setActorRatingForFoe(playerId, botId, playerOnOpFor);
        setActorRatingForFoe(botId, playerId, opForOnPlayer);
    }

    public String update(LocalDate date) {
        StringBuilder builder = new StringBuilder();
        builder.append(updateIntelItems(date)).append(System.lineSeparator());
        builder.append(updateEvents(date)).append(System.lineSeparator());

        return builder.toString();
    }

    /**
     * Iterate over all the IntelItems in this SOI and apply any outcomes that have
     * @return report String
     */
    public String updateIntelItems(LocalDate date) {
        StringBuilder builder = new StringBuilder();

        // Check all outcomes for each IntelItem and execute.
        for (IntelItem item : items) {
            ArrayList<IntelOutcome> done = new ArrayList<>();
            for (IntelOutcome outcome : item.getOutcomes()) {
                if (outcome.checkAchieved()) {
                    builder.append(outcome.toString()).append("\n");
                    outcome.apply();
                    done.add(outcome);
                }
            }
            item.removeOutcomes(done);
        }

        return builder.toString();
    }

    /**
     * Iterate over all the events in this SOI, check for completion / achievement, apply IntelOutcomes.
     *
     * TODO: determine if event and outcome removal would be better handled _using_ IntelOutcomes at update?
     *
     * Remove completed events and timed-out events.
     * @return report String
     */
    public String updateEvents(LocalDate date) {
        StringBuilder builder = new StringBuilder();

        // Apply updates now
        for (int id : eventsMap.keySet()) {
            ArrayList<IntelEvent> events = eventsMap.get(id);
            for (IntelEvent event : events) {
                builder.append(event.update(date)).append(System.lineSeparator());
            }
        }

        // Then clean up any events that have expired/been completed.
        for (int id : eventsMap.keySet()) {
            ArrayList<IntelEvent> events = eventsMap.get(id);
            ArrayList<IntelEvent> doneEvents = new ArrayList<>();

            // Find all EXPIRED (or greater) events and clean them up.
            for (IntelEvent event : events) {
                if (event.getState().ordinal() >= EventState.EXPIRED.ordinal()) {
                    doneEvents.add(event);
                }
                ArrayList<IntelOutcome> done = new ArrayList<>();

                // Find all outcomes that can be completed, apply them, and remove them.
                for (IntelOutcome outcome : event.getOutcomes()) {
                    if (outcome.checkAchieved()) {
                        builder.append(outcome.toString()).append("\n");
                        outcome.apply();
                        done.add(outcome);
                    }
                }
                event.removeOutcomes(done);
            }

            // Remove done events... is this a good idea?
            events.removeAll(doneEvents);
            eventsMap.put(id, events);
        }

        return builder.toString();
    }

    public static SphereOfInfluence generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        SphereOfInfluence retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (SphereOfInfluence) Class.forName(className).getDeclaredConstructor().newInstance();
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
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "sphereOfInfluence", "soiId", soiId, "type", getClass());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "missionId", missionId);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "title", title);
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "description", description);

        // actorsRatingsMap map of maps
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "actorsRatings");
        for (int actor : actorsRatingsMap.keySet()) {
            MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "actor", "id", actor);
            for (int foe : actorsRatingsMap.get(actor).keySet()) {
                MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "foe", "id", foe);
                actorsRatingsMap.get(actor).get(foe).writeToXML(campaign, pw, indent);
                MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "foe");
            }
            MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "actor");
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "actorsRatings");

        // eventsMap, per actor ID
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "eventsMap", "count", eventsMap.size());
        for (int actor : eventsMap.keySet()) {
            MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "actor", "id", actor);
            for (IntelEvent event : eventsMap.get(actor)) {
                event.writeToXML(campaign, pw, indent);
            }
            MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "actor");
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "eventsMap");

        // items
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "items", "count", items.size());
        for (IntelItem item : items) {
            item.writeToXML(campaign, pw, indent);
        }
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "items");
        return indent;
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "sphereOfInfluence");
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        // Level is stored as an attribute of the node
        try {
            soiId = Integer.parseInt(node.getAttributes().getNamedItem("soiId").getNodeValue());
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("missionId")) {
                    missionId = Integer.parseInt(item.getTextContent());
                } else if (item.getNodeName().equalsIgnoreCase("title")) {
                    title = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("description")) {
                    description = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("actorsRatings")) {
                    loadActorRatingsFromNode(campaign, version, item);
                } else if (item.getNodeName().equalsIgnoreCase("eventsMap")) {
                    NodeList eventsNodes = item.getChildNodes();
                    for (int y = 0; y < eventsNodes.getLength(); y++) {
                        Node eventsNode = eventsNodes.item(y);
                        if (eventsNode.getNodeName().equalsIgnoreCase("actor")) {
                            int actorId = Integer.parseInt(eventsNode.getAttributes()
                                                         .getNamedItem("id")
                                                         .getNodeValue());
                            eventsMap.put(actorId, new ArrayList<>());
                            NodeList innerNodes = eventsNode.getChildNodes();
                            for (int z = 0; z < innerNodes.getLength(); z++) {
                                Node innerNode = innerNodes.item(z);
                                if (innerNode.getNodeName().equalsIgnoreCase("intelEvent")) {
                                    IntelEvent event = IntelEvent.generateInstanceFromXML(innerNode, campaign, version);
                                    eventsMap.get(actorId).add(event);
                                }
                            }
                        }
                    }
                } else if (item.getNodeName().equalsIgnoreCase("items")) {
                    NodeList itemsNodes = item.getChildNodes();
                    for (int y = 0; y < itemsNodes.getLength(); y++) {
                        Node itemNode = itemsNodes.item(y);
                        if (itemNode.getNodeName().equalsIgnoreCase("intelItem")) {
                            items.add(IntelItem.generateInstanceFromXML(itemNode, campaign, version));
                        }
                    }

                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    /**
     * Encapsulate loading the Map-of-Maps-of-IntelRatings that represents everyone's views of
     * everyone else.
     *
     * Expected format is:
     * <actorsRatings>
     *     <actor>
     *         <foe>
     *             <IntelRating in whatever format it uses><\IntelRating>
     *         </foe>
     *     </actor>
     * </actorsRatings>
     *
     * @param campaign  Campaign to load into
     * @param version   Version instance or null
     * @param node      Node to read from
     * @throws ParseException
     */
    private void loadActorRatingsFromNode(Campaign campaign, Version version, Node node) throws ParseException {
        NodeList actorNodes = node.getChildNodes();
        int actorId = 0;
        int foeId = 0;

        try {
            for (int x = 0; x < actorNodes.getLength(); x++) {
                Node actorNode = actorNodes.item(x);
                if (actorNode.getNodeName().equalsIgnoreCase("actor")) {
                    actorId = Integer.parseInt(actorNode.getAttributes().getNamedItem("id").getNodeValue());
                    actorsRatingsMap.put(actorId, new HashMap<>());
                    NodeList foeNodes = actorNode.getChildNodes();
                    for (int y = 0; y < foeNodes.getLength(); y++) {
                        Node foeNode = foeNodes.item(y);
                        if (foeNode.getNodeName().equalsIgnoreCase("foe")) {
                            foeId = Integer.parseInt(foeNode.getAttributes().getNamedItem("id").getNodeValue());
                            for (int z = 0; z < foeNode.getChildNodes().getLength(); z++) {
                                Node ratingNode = foeNode.getChildNodes().item(z);
                                if (ratingNode.getNodeName().equalsIgnoreCase("intelRating")) {
                                    IntelRating rating = IntelRating.generateInstanceFromXML(ratingNode, campaign, version);
                                    actorsRatingsMap.get(actorId).put(foeId, rating);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }
    }
}
