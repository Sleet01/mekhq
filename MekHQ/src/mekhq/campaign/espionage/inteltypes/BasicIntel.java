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

/**
 * Base class for storing intelligence Information Levels
 */
public class BasicIntel {
    public final static int HIGHEST_LEVEL = 12;
    public final static int LOWEST_LEVEL = -12;

    private int level = 0;
    protected boolean locked = false;

    public BasicIntel() {
    }

    public BasicIntel(int level) {
        if (level > HIGHEST_LEVEL || level < LOWEST_LEVEL) {
            throw new IllegalArgumentException("Level must be between -12 and 12, inclusive");
        }
        this.level = level;
    }

    // Copy Constructor
    public BasicIntel(BasicIntel other) {
        this.level = other.level;
        this.locked = other.locked;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level > HIGHEST_LEVEL || level < LOWEST_LEVEL) {
            throw new IllegalArgumentException("Level must be between -12 and 12, inclusive");
        }
        if (!locked) {
            this.level = level;
        }
    }

    public void decrementLevel() {
        if (!locked) {
            decreaseLevel(1);
        }
    }

    public void decreaseLevel(int delta) {
        if (!locked) {
            try {
                setLevel(level - delta);
            } catch (IllegalArgumentException ignored) {
                // maybe log?
            }
        }
    }

    public void incrementLevel() {
        if (!locked) {
            increaseLevel(1);
        }
    }

    public void increaseLevel(int delta) {
        if (!locked) {
            try {
                setLevel(level + delta);
            } catch (IllegalArgumentException ignored) {
                // maybe log?
            }
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }
}
