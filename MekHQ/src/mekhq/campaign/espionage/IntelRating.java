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
import mekhq.campaign.personnel.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IntelRating {
    private static final MMLogger LOGGER = MMLogger.create(IntelRating.class);

    public final static String FORCES_NAME = "ForcesIntel";
    public final static String POSITION_NAME = "PositionIntel";
    public final static String LOGISTICS_NAME = "LogisticsIntel";
    public final static String PERSONNEL_NAME = "PersonnelIntel";
    public final static String COMMS_NAME = "CommsIntel";
    public final static String FINANCIAL_NAME = "FinancialIntel";
    public final static String LOCAL_NAME = "LocalIntel";
    public final static String COUNTER_NAME = "CounterIntel";

    public final static HashMap<String, List<String>> intelAdjacencyMap = new HashMap<>(Map.of(
          COMMS_NAME, List.of(POSITION_NAME, COUNTER_NAME),
          COUNTER_NAME, List.of(COMMS_NAME, PERSONNEL_NAME),
          PERSONNEL_NAME, List.of(COUNTER_NAME, FORCES_NAME),
          FORCES_NAME, List.of(PERSONNEL_NAME, FINANCIAL_NAME),
          FINANCIAL_NAME, List.of(FORCES_NAME, LOGISTICS_NAME),
          LOGISTICS_NAME, List.of(FINANCIAL_NAME, LOCAL_NAME),
          LOCAL_NAME, List.of(LOGISTICS_NAME, POSITION_NAME),
          POSITION_NAME, List.of(LOCAL_NAME, COMMS_NAME)
    ));

    public final static HashMap<String, String> intelOppositionMap = new HashMap<>(Map.of(
          COMMS_NAME, FINANCIAL_NAME,
          COUNTER_NAME, LOGISTICS_NAME,
          PERSONNEL_NAME, LOCAL_NAME,
          FORCES_NAME, POSITION_NAME,
          FINANCIAL_NAME, COMMS_NAME,
          LOGISTICS_NAME, COUNTER_NAME,
          LOCAL_NAME, PERSONNEL_NAME,
          POSITION_NAME,  FORCES_NAME
    ));

    // Fields that influence per-Scenario OpFor knowledge directly
    private ForcesIntel forcesIntel;
    private PositionIntel positionIntel;
    private LogisticsIntel logisticsIntel;

    // Fields that mainly influence Espionage events
    private PersonnelIntel personnelIntel;
    private CommsIntel commsIntel;
    private FinancialIntel financialIntel;
    private LocalIntel localIntel;
    private CounterIntel counterIntel;

    private ArrayList<UUID> assignedPersonIDs;
    private transient ArrayList<Person> assignedPersons;

    /**
     * Basic constructor, sets all Intel levels to 0
     */
    public IntelRating() {
        this(0, 0, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Convenience Constructor for static Intel Ratings used by bots
     * @param levelForAllIntel  int that all levels should be set to initially
     */
    public IntelRating(int levelForAllIntel) {
        this(
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel,
              levelForAllIntel
        );
    }

    /**
     * Constructor used to set every level
     * @param forcesLevel
     * @param positionLevel
     * @param logisticsLevel
     * @param personnelLevel
     * @param commsLevel
     * @param financialLevel
     * @param localLevel
     * @param counterLevel
     */
    public IntelRating(int forcesLevel, int positionLevel, int logisticsLevel,
          int personnelLevel, int commsLevel, int financialLevel, int localLevel, int counterLevel) {
        forcesIntel = new ForcesIntel(forcesLevel);
        positionIntel = new PositionIntel(positionLevel);
        logisticsIntel = new LogisticsIntel(logisticsLevel);
        personnelIntel = new PersonnelIntel(personnelLevel);
        commsIntel = new CommsIntel(commsLevel);
        financialIntel = new FinancialIntel(financialLevel);
        localIntel = new LocalIntel(localLevel);
        counterIntel = new CounterIntel(counterLevel);

        // These will need to be populated by specialized constructors or setters.
        assignedPersonIDs = new ArrayList<UUID>();
        assignedPersons = new ArrayList<>();
    }

    // All Intel objects will be mutable for ease of reconstitution, and for visitor pattern use.
    public ForcesIntel getForcesIntel() {
        return forcesIntel;
    }

    public PositionIntel getPositionIntel() {
        return positionIntel;
    }

    public LogisticsIntel getLogisticsIntel() {
        return logisticsIntel;
    }

    public PersonnelIntel getPersonnelIntel() {
        return personnelIntel;
    }

    public CommsIntel getCommsIntel() {
        return commsIntel;
    }

    public FinancialIntel getFinancialIntel() {
        return financialIntel;
    }

    public LocalIntel getLocalIntel() {
        return localIntel;
    }

    public CounterIntel getCounterIntel() {
        return counterIntel;
    }

    public void ageAllIntelByOne() {
        ageAllIntel(1);
    }

    public void setAnIntelToLevel(String name, int level) {
        getAnIntel(name).setLevel(level);
    }

    public void setAllIntelToLevel(int level) {
        for (String key : intelAdjacencyMap.keySet()) {
            try {
                setAnIntelToLevel(key, level);
            } catch (NullPointerException e) {
                LOGGER.error(String.format("Could not set level on Intel type: '%s'", key));
            }
        }
    }

    public void ageAllIntel(int delta) {
        for (String key : intelAdjacencyMap.keySet()) {
            ageAnIntel(key, delta);
        }
    }

    public @Nullable BasicIntel getAnIntel(String name) {
        BasicIntel intel = switch (name) {
            case FORCES_NAME -> getForcesIntel();
            case POSITION_NAME -> getPositionIntel();
            case LOGISTICS_NAME -> getLogisticsIntel();
            case PERSONNEL_NAME -> getPersonnelIntel();
            case COMMS_NAME -> getCommsIntel();
            case FINANCIAL_NAME -> getFinancialIntel();
            case LOCAL_NAME -> getLocalIntel();
            case COUNTER_NAME -> getCounterIntel();
            default -> null;
        };

        return intel;
    }

    /**
     * Decrease magnitude of Intel type rating towards 0 by delta.
     * If current level > 0 + delta, new level is (current - delta)
     * If current level is < 0 - delta, new level is (current + delta)
     * if -delta <= current level < delta, new level is 0.
     * @param name      The Intel type name to adjust; see "*_NAME" constants
     * @param delta     Amount to "age" the intel level by; assumes positive progression towards 0
     */
    public void ageAnIntel(String name, int delta) {
        try {
            BasicIntel intel = getAnIntel(name);
            int level = intel.getLevel();
            int newLevel = (level >= 0) ? Math.max(level - delta, 0) : Math.min(level + delta, 0);
            intel.setLevel(newLevel);
        } catch (NullPointerException e) {
            LOGGER.error(String.format("Could age level on Intel type: '%s'", name));
        }
    }

    public void improveAnIntel(String name, int delta) {
        int adjacencyBonus = (int) Math.floor(delta/2.0);
        try {
            getAnIntel(name).increaseLevel(delta);
            for (String adjacent : intelAdjacencyMap.get(name)) {
                try {
                    getAnIntel(adjacent).increaseLevel(adjacencyBonus);
                } catch (NullPointerException e) {
                    LOGGER.error(String.format("Could improve level on adjacent Intel type: '%s'", adjacent));
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error(String.format("Could improve level on Intel type: '%s'", name));
        }
    }

    public void lockAnIntel(String name) {
        try {
            getAnIntel(name).setLocked(true);
        } catch (NullPointerException e) {
            LOGGER.error(String.format("Could not lock level on Intel type: '%s'", name));
        }
    }

    public void unlockAnIntel(String name) {
        try {
            getAnIntel(name).setLocked(false);
        } catch (NullPointerException e) {
            LOGGER.error(String.format("Could not unlock level on Intel type: '%s'", name));
        }
    }

    public void lockAllIntel() {
        for (String key : intelAdjacencyMap.keySet()) {
            lockAnIntel(key);
        }
    }

    public void unlockAllIntel() {
        for (String key : intelAdjacencyMap.keySet()) {
            unlockAnIntel(key);
        }
    }

    public void setAssignedPersonIDs(ArrayList<UUID> personIDs) {
        this.assignedPersonIDs = personIDs;
    }

    public void addPersonID(UUID personID) {
        this.assignedPersonIDs.add(personID);
    }

    public void addPerson(Person prospect) {
        if (prospect != null && !this.assignedPersonIDs.contains(prospect.getId())) {
            this.assignedPersonIDs.add(prospect.getId());
            this.assignedPersons.add(prospect);
        }
    }

    public void removePerson(Person prospect) {
        if (prospect != null && this.assignedPersonIDs.contains(prospect.getId())) {
            this.assignedPersonIDs.remove(prospect.getId());
            this.assignedPersons.remove(prospect);
        }
    }

    public void removePersonID(UUID personID) {
        this.assignedPersonIDs.remove(personID);
    }

    public ArrayList<UUID> getAssignedPersonIDs() {
        return assignedPersonIDs;
    }

    public void updateAssignedPersons(Map<UUID, Person> personsMap) throws UpdateException {
        // Requires that the Campaign is up to date.
        ArrayList<UUID> foundPersonIDs = new ArrayList<>();
        ArrayList<UUID> missingPersonIDs = new ArrayList<>();
        if (this.assignedPersonIDs != null && this.assignedPersons != null) {
            Person prospect = null;
            for (UUID personID: this.assignedPersonIDs) {
                try {
                    prospect = personsMap.get(personID);
                } catch (Exception e) {
                    LOGGER.error(String.format("Could not find person with ID: '%s'", personID));
                }

                if (prospect != null) {
                    foundPersonIDs.add(personID);
                    assignedPersons.add(prospect);
                } else {
                    missingPersonIDs.add(personID);
                }
            }

            // Throw here so as many IDs can be found as possible
            if (foundPersonIDs.size() != this.assignedPersonIDs.size()) {
                throw new UpdateException(String.format("Could not find persons with IDs: '%s'", missingPersonIDs));
            }
        }
    }

    public ArrayList<Person> getAssignedPersons() {
        return assignedPersons;
    }
}

class UpdateException extends Exception {
    public UpdateException(String message) {
        super(message);
    }
}
