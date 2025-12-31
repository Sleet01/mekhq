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
import java.util.function.Supplier;

public class IntelOutcome implements IResultEvaluator {

    // If added here, objects will be awarded to "winner" of the IntelOutcome outcome.
    private ArrayList<Object> linkedObjects;
    private String title;
    private String description;
    private int beneficiaryId;
    private Supplier<Boolean> testFunction;
    private Runnable applyFunction;

    public IntelOutcome() {
        this(new ArrayList<>());
    }

    public IntelOutcome(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public ArrayList<Object> getLinkedObjects() {
        return linkedObjects;
    }

    public void setLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects = linkedObjects;
    }

    public void addLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.addAll(linkedObjects);
    }

    public void addLinkedObject(Object linkedObject) {
        this.linkedObjects.add(linkedObject);
    }

    public void removeLinkedObjects(ArrayList<Object> linkedObjects) {
        this.linkedObjects.removeAll(linkedObjects);
    }

    public void removeLinkedObject(Object linkedObject) {
        this.linkedObjects.remove(linkedObject);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(int beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public void setTestFunction(Supplier<Boolean> testFunction) {
        this.testFunction = testFunction;
    }

    public void setApplyFunction(Runnable applyFunction) {
        this.applyFunction = applyFunction;
    }

    // All subclasses must implement
    public boolean checkAchieved() {
        return (testFunction != null) ? testFunction.get() : false;
    }

    // All subclasses must implement
    public void apply() {
        if (applyFunction != null) {
            applyFunction.run();
        }
    }

    public String toString() {
        String representation = title;
        if (linkedObjects.size() > 0) {
            representation += (linkedObjects.size() == 1) ? " [1 item]" : String.format(" [%s items]",
              linkedObjects.size());
        }
        if (testFunction != null && testFunction.get()) {
            representation += " (Done)";
        }
        return representation;
    }
}
