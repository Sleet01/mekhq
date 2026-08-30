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
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.scenarios.Scenario;
import mekhq.campaign.personnel.Person;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.UUID;

public class EspionageManager {

    protected static final ResourceBundle resources = ResourceBundle.getBundle(
          "mekhq.resources.Espionage",
          MekHQ.getMHQOptions().getLocale());

    private static EspionageManager instance;

    // Instance members
    private transient Campaign campaign;
    private transient EspionageFactory espionageFactory;
    private ArrayList<SphereOfInfluence> spheres;
    private ArrayList<EspionageRoster> assignments;

    private EspionageManager(Campaign campaign, EspionageFactory espionageFactory,
          ArrayList<SphereOfInfluence> spheres, ArrayList<EspionageRoster> assignedUUIDs) {
        this.campaign = campaign;
        this.espionageFactory = espionageFactory;
        this.spheres = spheres;
        this.assignments = assignedUUIDs;
    }

    public static @Nullable EspionageManager getInstance() {
        if (instance == null) {
            instance = new EspionageManager(null, EspionageFactory.getInstance(), new ArrayList<>(),
                  new ArrayList<>());
        }
        return instance;
    }

    public static @Nullable EspionageManager getInstance(Campaign campaign) {
        if (instance == null) {
            instance = new EspionageManager(campaign, EspionageFactory.getInstance(), new ArrayList<>(),
                  new ArrayList<>());
            return instance;
        } else if (instance.campaign == campaign) {
            return instance;
        }
        return null;
    }

    /**
     * Clear all Espionage state and remove references.
     */
    public static void clearInstance() {
        if (instance != null) {
            instance.campaign = null;
            instance.espionageFactory = null;
            instance = null;
        }
    }

    // TODO: update to track Persons assigned _per player_
    public ArrayList<EspionageRoster> getAssignments() {
        return this.assignments;
    }

    @Nullable
    public EspionageRoster getAssignmentsForPlayer(int playerId) {
        for (EspionageRoster roster: this.assignments) {
            if (roster.getPlayerId() ==  playerId) {
                return roster;
            }
        }
        return null;
    }

    public void setAssignments(ArrayList<EspionageRoster> rosters) {
        this.assignments = rosters;
    }

    public void removeAssignmentsForPlayer(int playerId) {
        EspionageRoster toRemove = getAssignmentsForPlayer(playerId);
        if (toRemove != null) {
            assignments.remove(toRemove);
        }
    }

    public void setAssignmentsForPlayer(int playerId, EspionageRoster roster) {
        removeAssignmentsForPlayer(playerId);
        this.assignments.add(roster);
    }

    public void addPersonForPlayer(Person person, int playerId) {
        EspionageRoster roster = getAssignmentsForPlayer(playerId);
        if (roster != null) {
            roster.assignPerson(person);
        }
    }

    public void removePersonForPlayer(Person person, int playerId) {
        EspionageRoster roster = getAssignmentsForPlayer(playerId);
        if (roster != null) {
            roster.removePerson(person);
        }
    }

    public @Nullable Person getPerson(UUID personId) {
        for (EspionageRoster roster: assignments) {
            if (roster.containsPerson(personId)) {
                return roster.getPersonFromUUID(personId);
            }
        }
        return null;
    }

    public void assignPersonToSOI(Person person, int playerId, SphereOfInfluence soi) {
        EspionageRoster roster = getAssignmentsForPlayer(playerId);
        if (roster != null) {
            roster.reassignPersonToSOI(person.getId(), soi.getSoiId());
        }
    }

    public void unassignPersonFromSOI(Person person, int playerId) {
        EspionageRoster roster = getAssignmentsForPlayer(playerId);
        if (roster != null) {
            roster.unassignPerson(person);
        }

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

    public @Nullable SphereOfInfluence getSphereOfInfluence(Scenario scenario) {
        for (SphereOfInfluence sphere : spheres) {
            if (sphere.getContractId() == scenario.getMissionId()) {
                return sphere;
            }
        }
        return null;
    }

    public void populateSpheresOfInfluence(Campaign campaign, boolean includeTutorial) {
        spheres = espionageFactory.generateSpheresOfInfluence(campaign, includeTutorial);
    }

    public void setCampaign(Campaign campaign) {
        this.campaign = campaign;
    }

    public void setEspionageFactory(EspionageFactory espionageFactory) {
        this.espionageFactory = espionageFactory;
    }

    public ArrayList<SphereOfInfluence> getSpheres() {
        if (spheres == null) {
            spheres = new ArrayList<>();
        }
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
