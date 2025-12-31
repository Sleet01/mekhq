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

import java.util.function.Supplier;

/**
 * Class that wraps Supplier&lt;Boolean&gt; with a user-parseable reason why it failed.
 * May be replaced by anonymous class generator?
 */
public class IntelEventPrerequisite {

    public static final String UNSPECIFIED_REASON = "unspecified";

    private Supplier<Boolean> supplier;
    private String requirement;

    public IntelEventPrerequisite() {
        this(null, UNSPECIFIED_REASON);
    }

    public IntelEventPrerequisite(Supplier<Boolean> supplier, String requirement) {
        this.supplier = supplier;
        this.requirement = requirement;
    }

    public void setSupplier(Supplier<Boolean> supplier) {
        this.supplier = supplier;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getRequirement() {
        return requirement;
    }

    public boolean get() {
        return supplier.get();
    }

    /**
     * Returns null if supplier is not set; empty string if requirement met; reason if not met.
     * @return String (Nullable) reason why the prereq is not met.
     */
    @Nullable
    public String getReason() {
        if (supplier != null) {
            return (supplier.get()) ? "" : requirement;
        }
        return null;
    }
}
