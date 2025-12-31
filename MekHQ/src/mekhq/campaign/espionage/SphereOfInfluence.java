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

import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;

import java.util.ArrayList;
import java.util.HashMap;

public class SphereOfInfluence {
    private static final MMLogger LOGGER = MMLogger.create(SphereOfInfluence.class);

    // Use Player / Bot IDs as keys for now
    private HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap;
    private ArrayList<IntelItem> items;
    private HashMap<Integer, ArrayList<IntelEvent>> eventsMap;

    public SphereOfInfluence() {
         this(new HashMap<>(), new ArrayList<>(), new HashMap<>());
    }

    public SphereOfInfluence(
          HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap,
          ArrayList<IntelItem> items,
          HashMap<Integer, ArrayList<IntelEvent>> eventsMap
    ) {
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

    public void setActorRatingForFoe(int actorId, int foeId, IntelRating ratingForFoe) {
        HashMap<Integer, IntelRating> newRating = (actorsRatingsMap.containsKey(actorId))
                                            ? actorsRatingsMap.get(actorId) : new HashMap<>();

        newRating.put(foeId, ratingForFoe);
        actorsRatingsMap.put(actorId, newRating);
    }

    public boolean update() {
        return false;
    }

}
