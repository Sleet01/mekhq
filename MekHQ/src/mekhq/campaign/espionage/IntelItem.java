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

import java.time.LocalDate;
import java.util.ArrayList;

public class IntelItem {

    public final static int UNSET_ID = -1;

    private LocalDate startDate;
    private int itemID;
    private int ownerId;
    private int possessorId;
    private String itemName;
    private String itemDescription;
    private boolean discovered = false;
    private boolean captured = false;
    private boolean deciphered = false;
    private boolean delivered = false;
    private boolean destroyed = false;
    private boolean escaped = false;

    // IntelOutcomes will contain reward items, so that asymmetrical awards can be implemented easily
    private ArrayList<IntelOutcome> outcomes;

    public IntelItem() {
        // Use now as the default date; all IntelItems created this way will immediately appear
        this(LocalDate.now(), UNSET_ID, UNSET_ID, UNSET_ID, "", "", new ArrayList<>());
    }

    public IntelItem(LocalDate startDate, int itemID, int ownerID, int possessorId,
          String itemName, String itemDescription, ArrayList<IntelOutcome> outcomes) {
        this.startDate = startDate;
        this.itemID = itemID;
        this.ownerId = ownerID;
        this.possessorId = possessorId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.outcomes = outcomes;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public int getPossessorId() {
        return possessorId;
    }

    public void setPossessorId(int possessorId) {
        this.possessorId = possessorId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    public boolean isDeciphered() {
        return deciphered;
    }

    public void setDeciphered(boolean deciphered) {
        this.deciphered = deciphered;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }

    public boolean isEscaped() {
        return escaped;
    }

    public void setEscaped(boolean escaped) {
        this.escaped = escaped;
    }

    // SOI and/or Manager will likely directly edit these
    public ArrayList<IntelOutcome> getOutcomes() {
        return outcomes;
    }

    // SOI and/or Manager will likely edit these
    public void setOutcomes(ArrayList<IntelOutcome> outcomes) {
        this.outcomes = outcomes;
    }

    public String listOutcomeEntries() {
        StringBuilder outcomeList = new StringBuilder();
        for (IntelOutcome result : outcomes) {
            outcomeList.append(result.toString());
        }
        return outcomeList.toString();
    }

    public ArrayList<Object> listLinkedObjects() {
        ArrayList<Object> linkedObjects = new ArrayList<>();
        for (IntelOutcome outcome : outcomes) {
            linkedObjects.addAll(outcome.getLinkedObjects());
        }
        return linkedObjects;
    }
}
