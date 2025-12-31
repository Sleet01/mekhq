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

/**
 * This class represents an IntelRating that does not age or improve.
 * However, its levels may be changed in some circumstances.
 */
public class StaticIntelRating extends IntelRating {

    public StaticIntelRating() {
        super();
    }

    public StaticIntelRating(int rating) {
        super(rating);
    }

    public StaticIntelRating(int forcesLevel, int positionLevel, int logisticsLevel,
          int personnelLevel, int commsLevel, int financialLevel, int localLevel, int counterLevel) {
        super(
              forcesLevel,
              positionLevel,
              logisticsLevel,
              personnelLevel,
              commsLevel,
              financialLevel,
              localLevel,
              counterLevel
        );
    }

    @Override
    public void ageAllIntelByOne() {
        // pass
    }

    @Override
    public void setAnIntelToLevel(String name, int level) {
        // pass
    }

    @Override
    public void setAllIntelToLevel(int level) {
        // pass
    }

    @Override
    public void ageAllIntel(int delta) {
        // pass
    }

    @Override
    public void ageAnIntel(String name, int delta) {
        // pass
    }

    @Override
    public void improveAnIntel(String name, int delta) {
        // pass
    }
}
