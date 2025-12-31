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

import java.util.ArrayList;

public class EspionageManager {

    private static EspionageManager instance;

    // Instance members
    private EspionageFactory espionageFactory;
    private ArrayList<SphereOfInfluence> spheres;

    private EspionageManager(EspionageFactory espionageFactory, ArrayList<SphereOfInfluence> spheres) {
        this.espionageFactory = espionageFactory;
        this.spheres = spheres;
    }

    public static EspionageManager getInstance() {
        if(instance == null) {
            instance = new EspionageManager(EspionageFactory.getInstance(), new ArrayList<>());
        }
        return instance;
    }

    public void addSphereOfInfluence(SphereOfInfluence sphereOfInfluence) {
        spheres.add(sphereOfInfluence);
    }

    /**
     * Run all updates for all Spheres of Influence currently being managed.
     * This includes:
     * 1. Checking and updating the states of all IntelItems in all SOIs, and spawning follow-ups if needed.
     * 2. Checking and updating the states of all IntelEvents in all SOIs, and spawning follow-ups if needed.
     * 3. Generating any new IntelItems and IntelEvents required.
     *
     * This may take some time.
     * @return reports String
     */
    public String runUpdates() {
        StringBuilder builder = new StringBuilder();
        builder.append("======== Espionage Update Report ========").append("\n\n");

        for (SphereOfInfluence sphereOfInfluence : spheres) {
            builder.append(updateSphereOfInfluence(sphereOfInfluence)).append("\n");
        }

        return builder.toString();
    }

    /**
     * Update one SphereOfInfluence instance and return its reports, if any.
     * @param sphere
     * @return
     */
    private String updateSphereOfInfluence(SphereOfInfluence sphere) {
        StringBuilder builder = new StringBuilder();

        builder.append(sphere.updateIntelItems()).append("\n");
        builder.append(sphere.updateEvents()).append("\n");

        return builder.toString();
    }
}
