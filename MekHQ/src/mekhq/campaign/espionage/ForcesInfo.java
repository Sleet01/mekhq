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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

public class ForcesInfo extends RatingInfo {

    public final static int DEFAULT_ID = -1;

    private ArrayList<SimpleEntry<String, Integer>> knownEntities;

    public ForcesInfo() {
        this(0, new ArrayList<SimpleEntry<String, Integer>>());
    }

    public ForcesInfo(int level) {
        this(level, new ArrayList<SimpleEntry<String, Integer>>());
    }

    public ForcesInfo(int level, ArrayList<SimpleEntry<String, Integer>> knownEntities) {
        super(level);
        this.knownEntities = knownEntities;
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
        this.knownEntities = knownEntities;
    }

    /**
     * Add an entity entry with the default ID value.
     * @param fullName
     */
    public void addEntityByName(String fullName) {
        addKnownEntityEntry(new SimpleEntry<>(fullName, DEFAULT_ID));
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
        addKnownEntityEntry(new SimpleEntry<>(fullName, id));
    }

    /**
     * Add a new SimpleEntry to the knownEntities member.
     * @param entityEntry containing the description of an entity and an ID number
     */
    public void addKnownEntityEntry(SimpleEntry<String, Integer> entityEntry) {
        this.knownEntities.add(entityEntry);
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
}
