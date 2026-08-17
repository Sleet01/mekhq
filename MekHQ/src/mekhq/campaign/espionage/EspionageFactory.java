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

import megamek.common.Player;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.finances.enums.TransactionType;
import mekhq.campaign.force.PlayerForce;
import mekhq.campaign.mission.Mission;
import mekhq.campaign.universe.Faction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class EspionageFactory {

    protected static final ResourceBundle resources = ResourceBundle.getBundle(
          "mekhq.resources.Espionage",
          MekHQ.getMHQOptions().getLocale());

    private static EspionageFactory instance;
    private int nextSOIIndex = 0;
    // Probably some info about various intel agencies and factions goes here

    // TODO: add another Constructor with more arguments, for tweaking settings
    private EspionageFactory() {};

    public static EspionageFactory getInstance() {
        if (instance == null) {
            instance = new EspionageFactory();
        }
        return instance;
    }

    /**
     * Generates an initial set of SpheresOfInfluence instances for the provided Campaign instance.
     * Can include a tutorial SOI, populated with instructional events, as the first SOI.
     *
     * ==============================
     * A word on Spheres of Influence:
     *
     * An SOI is meant to represent an amorphous arena within which two or more powers pursue their
     * espionage agendas.
     * It will often map directly to a single planet, but depending on the player's
     * Espionage capabilities (and the extents of their forces and locations) may include:
     *  - Fictional / abstract locations on the current planet;
     *  - Locations in the Area of Operations
     *  - Other planets or stations in this system
     *  - Planets in other systems
     *  - "Real" historical locations belonging to an enemy (or employer!) faction
     * So an SOI represents however far espionage operations relating to a specific mission have, or can be,
     * extended.
     *
     * The Espionage system uses SOIs to keep track of Intel Ratings, Events, Items, and personnel:
     * - Each campaign can have any number of Contracts / Missions
     *   - Each mission *will* have one or more Spheres of Influence (but usually just one)
     *     - Each Sphere of Influence records the level of the player's espionage ability in 8 fields,
     *       for each other actor in that SOI.
     *     - Likewise, each other actor similarly has 8 intel ratings for each actor other than themselves
     *       (but initially, this will usually be a static rating only dealing with the player)
     *     - Each Sphere of Influence can have any number of events occurring within it
     *       - Each event can have any number of personnel and units assigned to it
     *     - Each SOI can have any number of items related to events occurring within it
     *
     * @param campaign          Campaign instance assigned to the current player
     * @param includeTutorial   If true, create the tutorial SOI first, to guide the player through Espionage
     *                          usage.
     * @return                  ArrayList of initialized SOIs
     */
    public ArrayList<SphereOfInfluence> generateSpheresOfInfluence(Campaign campaign, boolean includeTutorial) {
        ArrayList<SphereOfInfluence> spheresList = new ArrayList<>();

        List<Mission> missionList = campaign.getActiveMissions(true);
        int i = nextSOIIndex;
        if (includeTutorial) {
            SphereOfInfluence tutorial = generateTutorialSOI(campaign, missionList.getFirst(), i);
            spheresList.add(tutorial);
            i++;
        }
        for (Mission mission: missionList) {
            SphereOfInfluence soi = generateSphereOfInfluence(campaign, mission, i);
            spheresList.add(soi);
            i++;
        }
        nextSOIIndex = i;

        return spheresList;
    }


    /**
     * Creates and populates a new SphereOfInfluence instance appropriate to the provided campaign and mission.
     * This will include a small set of introductory events with few prerequisites.
     * As the player interacts with the SOI, completes events, and assigns personnel, more complex (and dangerous)
     * events will be generated.
     *
     * @param campaign  Campaign instance this factory is attached to.
     * @param mission   The "mission", contract, etc. that this SOI covers
     * @param soiId     ID assigned by the factory.  soiId + mission ID *should* form a unique ID.
     * @return          SOI instance, with title, description, ratings, a few items, and some events populated.
     */
    public static SphereOfInfluence generateSphereOfInfluence(Campaign campaign, Mission mission, int soiId) {
        String title = generateSOITitle(campaign, mission);
        String description = generateSOIDescription(campaign, mission, title);
        HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap = generateActorsRatingsMap(campaign, mission);
        ArrayList<IntelItem> items = generateIntelItems(campaign, mission);
        HashMap<Integer, ArrayList<IntelEvent>> eventsMap = generateEventsMap(campaign, mission);

        SphereOfInfluence soi = new SphereOfInfluence(
              soiId,
              mission.getId(),
              title,
              description,
              actorsRatingsMap,
              items,
              eventsMap
        );

        return soi;
    }

    // Todo: add template system for tutorial
    public static SphereOfInfluence generateTutorialSOI(Campaign campaign, Mission mission, int soiId) {
        // Should read a tutorial SOI definition in from disk here, then populate the SOI.
        // But for now, manually populate the crap out of it!

        // Tracking info
        int playerId = campaign.getPlayer().getId();
        Faction faction = mission.getEmployerFaction();
        String liaisonName = generateMisterSmithName(campaign, mission, faction);

        SphereOfInfluence soi = new SphereOfInfluence();
        soi.setSoiId(soiId);
        soi.setMissionId(mission.getId());
        soi.setTitle(generateSOITitle(campaign, mission) + " (Tutorial)");
        soi.setDescription(generateTutorialSOIDescription(campaign, mission, faction, soi.getTitle(), liaisonName));

        // Add Tutorial event items:
        // 1. "Mister Smith"'s card
        // 2. "Mister Smith" themselves TODO
        soi.addIntelItem(generateMisterSmithCard(soiId, soi.advanceItemId(), playerId));
//        soi.addIntelItem(generateMisterSmith());

        // Add initial event, which will create the tutorial event chain
        soi.addEventForActor(playerId, generateInitialTutorialEvent(campaign, mission, liaisonName, playerId));

        return soi;
    }

    public static String generateSOITitle(Campaign campaign, Mission mission) {
        String title = "Default Title";

        return title;
    }

    private static String generateSOIDescription(Campaign campaign, Mission mission, String title) {
        StringBuilder description = new StringBuilder();
        return description.toString();
    }

    private static String generateTutorialSOIDescription(Campaign campaign, Mission mission,
          Faction faction, String title,
          String liaison) {
        String employer = mission.getEmployerName();
        String factionName = faction.getShortName();
        String intelOrg = generateIntelOrg(campaign, mission, faction);

        StringBuilder description = new StringBuilder();
        description.append("An introduction to the Espionage system.")
              .append("\n")
              .append(employer)
              .append(" has arranged a meeting with \"")
              .append(liaison)
              .append("\", a liaison to the ")
              .append(factionName)
              .append(" intelligence organization known as \"")
              .append(intelOrg)
              .append("\".");

        return description.toString();
    }

    private static IntelEvent generateInitialTutorialEvent(Campaign campaign, Mission mission, String liaison,
          int playerId) {
        // Start from a bare IntelEvent because we don't need prereqs
        IntelEvent initialEvent = new IntelEvent();
        initialEvent.setTitle("An Introduction...");
        initialEvent.setDescription(
              String.format("The liaison, %s, waits patiently inside your office.", liaison)
        );
        initialEvent.setEventId(0);
        LocalDate start = campaign.getLocalDate();
        LocalDate end = start.plusDays(7);
        initialEvent.setStartDate(start);
        initialEvent.setEndDate(end);

        // Participants
        ArrayList<Integer> participantIds = new ArrayList<>();
        participantIds.add(playerId);

        // Give enemy the next ID number, for now.
        // Need a good way to convert this back to a given enemy...
        participantIds.add(playerId + 1);
        initialEvent.setParticipantIds(participantIds);

        return initialEvent;
    }

    private static ISerializableSupplier<Boolean> alwaysTruePrereq() {
        return () -> {
            return true;
        };
    }

    private static ISerializableSupplier<Boolean>  anyoneAddedToSOITestFunction () {
        return () -> {
            EspionageManager manager1 = EspionageManager.getInstance();
            Campaign campaign = manager1.getCampaign(); // Should never be null...

            // TODO: fix
            return true;
        };
    }

    private static String generateMisterSmithName(Campaign campaign, Mission mission, Faction faction) {
        // TODO: add per-faction, per-region names in YAML file
        return "Mr. Smith";
    }

    /**
     * **** CORRECTION: IntelItems are only evaluated at the end of the SOI! ****
     * The Mister Smith card is an encoded message, designed as a test of the player's
     * nascent espionage org.
     * Decoding the card will be event #2, and possessing the decoded card at the end of the
     * SOI will result in a monetary reward of some kind.
     *
     * @return IntelItem    Allows tracking player progress for first two events, and gives reward.
     */
    private static IntelItem generateMisterSmithCard(int soiId, int itemId, int playerId) {
        // Need SOI to manage itemIds?
        IntelItem card = new IntelItem();
        card.setItemId(itemId);
        // The player _has_ the card, but has not yet deciphered it so does not yet own it.
        card.setDiscovered(true);
        card.setCaptured(true);
        card.setOwnerId(IntelItem.UNSET_ID);
        card.setPossessorId(playerId);

        // The outcomes are either:
        // A) Player decodes and becomes the owner of the card by SOI end -> gain some seed money.
        // B) player does not decode the card -> card is lost.
        card.addOutcome(generateTutorialOutcomeOne(soiId, itemId, playerId));

        return card;
    }

    /**
     *
     * @param playerId
     * @return
     */
    private static IntelOutcome generateTutorialOutcomeOne(int soiId, int itemId, int playerId) {
        // TODO: load this text from a YAML file.
        IntelOutcome decodedCard = new IntelOutcome();
        decodedCard.setBeneficiaryId(playerId);
        decodedCard.setTitle("You Completed The Tutorial in Possession of the Decoded Card");
        decodedCard.setDescription(
             new StringBuilder()
                   .append("With the tutorial SOI at an end, you still have the mysterious card.")
                   .append("\nWhile fiddling idly with it, you discover, cleverly concealed beneath")
                   .append("\na thin layer of acrylic paint, a set of data contacts.  With a suitable")
                   .append("\nadapter in hand you connect it to your workstation, and soon uncover")
                   .append("\na single file containing strings of digits that you immediately recognize")
                   .append("\nas an account number at one of the local banks.  It appears that your benefactor")
                   .append("\nhas left your nascent intelligence organization a little seed money!")
                   .toString()
        );
        decodedCard.setTestFunction(testItemStateForActorId(soiId, itemId, playerId, true, true, true, false, false,
             false)
        );
        decodedCard.setApplyFunction(givePlayerMoney(soiId, playerId, 50000.00,
              "Some petty cash to help bring up your network."));
        return decodedCard;
    }

    private static String generateIntelOrg(Campaign campaign, Mission mission, Faction faction) {
        // TODO: add per-faction, per-region intel orgs in YAML file
        return "Spies";
    }

    private static ISerializableRunnable givePlayerMoney(int soiId, int playerId, double amount, String description) {
        return (ISerializableRunnable) () -> {
            Campaign campaign = retrieveCampaign();
            Player player = retrievePlayer(playerId);
            if (campaign != null && player != null) {
                PlayerForce force = campaign.getPlayerForce();
                force.addFunds(
                      TransactionType.STARTING_CAPITAL, campaign.getLocalDate(), Money.of(amount), description
                );
            }
        };
    }

    /**
     * Static helper for items to test that their state matches some desired state for a given player.
     * @param soiId
     * @param itemId
     * @param actorId
     * @param wantDiscovered
     * @param wantCaptured
     * @param wantDeciphered
     * @param wantDelivered
     * @param wantDestroyed
     * @param wantEscaped
     * @return
     */
    private static ISerializableSupplier<Boolean> testItemStateForActorId(int soiId, int itemId, int actorId,
          boolean wantDiscovered, boolean wantCaptured, boolean wantDeciphered, boolean wantDelivered,
          boolean wantDestroyed, boolean wantEscaped) {
        return () -> {
            IntelItem item = retrieveItem(soiId, itemId);

            return (
                  (item != null)
                      && (item.isDiscovered() == wantDiscovered)
                      && (item.isCaptured() == wantCaptured)
                      && (item.isDeciphered() == wantDeciphered)
                      && (item.isDelivered() == wantDelivered)
                      && (item.isDestroyed() == wantDestroyed)
                      && (item.isEscaped() == wantEscaped)
            );
        };
    }

    private static IntelItem retrieveItem(int soiId, int itemId) {
        EspionageManager manager = EspionageManager.getInstance();
        SphereOfInfluence soi = (manager != null) ? manager.getSphereOfInfluence(soiId) : null;
        IntelItem item = (soi != null) ? soi.getIntelItem(itemId) : null;
        return item;
    }

    private static Player retrievePlayer(int playerId) {
        Campaign campaign = retrieveCampaign();
        if (campaign != null) {
            Player player = campaign.getPlayer();
            if (player != null && player.getId() == playerId) {
                return player;
            }
        }
        return null;
    }

    private static Campaign retrieveCampaign() {
        EspionageManager manager = EspionageManager.getInstance();
        return manager.getCampaign();
    }

    private static HashMap<Integer, HashMap<Integer, IntelRating>> generateActorsRatingsMap(Campaign campaign, Mission mission) {
        HashMap<Integer, HashMap<Integer, IntelRating>> ratingsMap = new HashMap<>();
        return ratingsMap;
    }

    private static ArrayList<IntelItem> generateIntelItems(Campaign campaign, Mission mission) {
        ArrayList<IntelItem> itemsList = new ArrayList<>();
        return itemsList;
    }

    private static HashMap<Integer, ArrayList<IntelEvent>> generateEventsMap(Campaign campaign, Mission mission) {
        HashMap<Integer, ArrayList<IntelEvent>> eventMap = new HashMap<>();
        return eventMap;
    }

    public static IntelEvent generateBasicEvent(Campaign campaign, SphereOfInfluence soi) {
        IntelEvent event = new IntelEvent();
        // TODO: fill in
        return event;
    }

    public static IntelItem generateBasicItem(Campaign campaign, SphereOfInfluence soi) {
        IntelItem item = new IntelItem();
        // TODO: fill in
        return item;
    }

    public static ArrayList<IntelEvent> generateTutorialEventChain(Campaign campaign, SphereOfInfluence soi) {
        ArrayList<IntelEvent> events = new ArrayList<>();
        // TODO: fill in
        return events;
    }

    public void setNextSOIIndex(int index) {
        nextSOIIndex = index;
    }

    public int getNextSOIIndex() {
        return nextSOIIndex;
    }
}
