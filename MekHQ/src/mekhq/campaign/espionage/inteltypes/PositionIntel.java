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

package mekhq.campaign.espionage.inteltypes;

import java.util.ArrayList;

public class PositionIntel extends BasicIntel {

    private ArrayList<Integer> knownPositions;

    public PositionIntel() {
        this(0, new ArrayList<>());
    }

    public PositionIntel(int level) {
        this(level, new ArrayList<>());
    }

    public PositionIntel(int level, ArrayList<Integer> knownPositions) {
        super(level);
        this.knownPositions = knownPositions;
    }

    public PositionIntel(PositionIntel other) {
        super(other);
        this.knownPositions = new ArrayList<Integer>(other.knownPositions);
    }

    /**
     * Creates copy of knownPositions
     * @return ArrayList copy of all knownPositions
     */
    public ArrayList<Integer> getKnownPositions() {
        return new ArrayList<>(knownPositions);
    }

    /**
     * Returns true if position of ID'ed unit is known, _or_ if ID is default (false confidence)
     * @param ID
     * @return true if known / ID is default, otherwise false
     */
    public boolean getKnown(int ID) {
        return knownPositions.contains(ID);
    }

    public void addKnown(int ID) {
        if (!locked) {
            knownPositions.add(ID);
        }
    }
}
