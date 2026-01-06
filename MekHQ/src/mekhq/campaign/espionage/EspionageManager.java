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
import mekhq.campaign.Campaign;

import java.time.LocalDate;
import java.util.ArrayList;

public class EspionageManager {

    private static EspionageManager instance;

    // Instance members
    private transient Campaign campaign;
    private transient EspionageFactory espionageFactory;
    private ArrayList<SphereOfInfluence> spheres;

    private EspionageManager(Campaign campaign, EspionageFactory espionageFactory,
          ArrayList<SphereOfInfluence> spheres) {
        this.campaign = campaign;
        this.espionageFactory = espionageFactory;
        this.spheres = spheres;
    }

    // If using this (likely from a Runnable or Supplier) the instance must exist, and the campaign
    // must have been set; otherwise will return null!
    public static @Nullable EspionageManager getInstance() {

        return instance;
    }

    public static @Nullable EspionageManager getInstance(Campaign campaign) {
        if (instance == null) {
            instance = new EspionageManager(campaign, EspionageFactory.getInstance(), new ArrayList<>());
            return instance;
        } else if (instance.campaign == campaign) {
            return instance;
        }
        return null;
    }

    public void addSphereOfInfluence(SphereOfInfluence sphereOfInfluence) {
        spheres.add(sphereOfInfluence);
    }

    public @Nullable SphereOfInfluence getSphereOfInfluence(int Id) {
        for (SphereOfInfluence sphere : spheres) {
            if (sphere.getSoiId() == Id) {
                return sphere;
            }
        }
        return null;
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public void setEspionageFactory(EspionageFactory espionageFactory) {
        this.espionageFactory = espionageFactory;
    }

    public ArrayList<SphereOfInfluence> getSpheres() {
        return spheres;
    }

    public Campaign getCampaign() {
        return campaign;
    }

    public EspionageFactory getEspionageFactory() {
        return espionageFactory;
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
    public String runUpdates(LocalDate date) {
        StringBuilder builder = new StringBuilder();
        builder.append("======== Espionage Update Report ========").append("\n\n");

        for (SphereOfInfluence sphereOfInfluence : spheres) {
            builder.append(sphereOfInfluence.update(date)).append("\n");
        }

        return builder.toString();
    }
}
