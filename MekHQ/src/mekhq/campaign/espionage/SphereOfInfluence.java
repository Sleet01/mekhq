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
import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.Mission;

import java.util.ArrayList;
import java.util.HashMap;

public class SphereOfInfluence {
    private static final MMLogger LOGGER = MMLogger.create(SphereOfInfluence.class);

    public static final int UNASSIGNED_MISSION = -1;

    private int missionId;
    // Events will initially be generated for just the player, but bot events may be added.
    // Use Player / Bot IDs as keys for now
    private HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap;
    private HashMap<Integer, ArrayList<IntelEvent>> eventsMap;

    // IntelItems are more free-floating.
    private ArrayList<IntelItem> items;

    public SphereOfInfluence() {
         this(UNASSIGNED_MISSION, new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    public SphereOfInfluence(
          int missionId,
          HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap,
          ArrayList<IntelItem> items,
          HashMap<Integer, ArrayList<IntelEvent>> eventsMap
    ) {
        this.missionId = missionId;
        this.actorsRatingsMap = actorsRatingsMap;
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
     *
     * Initially we will only provide events for Player vs Opponent force, although
     * some may be written as, "prevent Opponent from X" as if the opfor is also generating events.
     * @param campaign  The current campaign; this may be replaced in the near future.
     * @param mission   Let the GUI decide which Mission to look at
     * @param botLevel  For this round, set a static level for the bot
     */
    public void populate(Campaign campaign, Mission mission, int botLevel) {
        if (missionId == UNASSIGNED_MISSION || campaign == null) {
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

    public void setActorRatingForFoe(int actorId, int foeId, IntelRating ratingForFoe) {
        HashMap<Integer, IntelRating> actorRatingMap = (actorsRatingsMap.containsKey(actorId))
                                            ? actorsRatingsMap.get(actorId) : new HashMap<>();

        actorRatingMap.put(foeId, ratingForFoe);
        actorsRatingsMap.put(actorId, actorRatingMap);
    }

    /**
     * Iterate over all the IntelItems in this SOI and apply any outcomes that have
     * @return report String
     */
    public String updateIntelItems() {
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
     * Remove completed events and timed-out events.
     * @return report String
     */
    public String updateEvents() {
        StringBuilder builder = new StringBuilder();

        for (int id : eventsMap.keySet()) {
            ArrayList<IntelEvent> events = eventsMap.get(id);

            for (IntelEvent event : events) {
                ArrayList<IntelOutcome> done = new ArrayList<>();
                for (IntelOutcome outcome : event.getOutcomes()) {
                    if (outcome.checkAchieved()) {
                        builder.append(outcome.toString()).append("\n");
                        outcome.apply();
                        done.add(outcome);
                    }
                }
                event.removeOutcomes(done);




            }
        }


        return builder.toString();
    }
}
