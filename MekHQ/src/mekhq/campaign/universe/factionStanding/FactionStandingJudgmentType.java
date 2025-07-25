/*
 * Copyright (C) 2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign.universe.factionStanding;

/**
 * Enum representing the type of action taken during a faction judgment event.
 *
 * <p>Each constant has a unique lookup name for identification.</p>
 *
 * @author Illiani
 * @since 0.50.07
 */
public enum FactionStandingJudgmentType {
    CENSURE("CENSURE"),
    ACCOLADE("ACCOLADE"),
    WELCOME("WELCOME");

    /**
     * The unique lookup name associated with this type.
     */
    private final String lookupName;

    /**
     * Constructs a {@link FactionStandingJudgmentType} with the specified lookup name.
     *
     * @param lookupName the lookup name associated with this type
     */
    FactionStandingJudgmentType(String lookupName) {
        this.lookupName = lookupName;
    }

    /**
     * Returns the lookup name for this type.
     *
     * @return the lookup name
     */
    public String getLookupName() {
        return lookupName;
    }
}
