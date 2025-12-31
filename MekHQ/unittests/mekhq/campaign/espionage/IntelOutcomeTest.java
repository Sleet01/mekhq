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

import megamek.common.Player;
import megamek.common.enums.SkillLevel;
import megamek.common.equipment.EquipmentType;
import megamek.common.game.Game;
import megamek.common.units.Entity;
import megamek.common.units.Mek;
import megamek.common.units.Tank;
import mekhq.campaign.Campaign;
import mekhq.campaign.campaignOptions.CampaignOptions;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.skills.RandomSkillPreferences;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.DOMException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static testUtilities.MHQTestUtilities.getEntityForUnitTesting;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IntelOutcomeTest {
    Campaign campaign;
    Player player = new Player(1, "Test");
    Game game = new Game();

    @BeforeAll
    public static void setUpBeforeClass() throws DOMException {
        EquipmentType.initializeTypes();
    }

    @BeforeEach
    public void setUp() {
        // Initialize the mock objects
        campaign = mock(Campaign.class);
        CampaignOptions options = mock(CampaignOptions.class);
        when(options.getNonBinaryDiceSize()).thenReturn(60);
        when(options.isAutoGenerateOpForCallSigns()).thenReturn(false);
        when(options.getMinimumCallsignSkillLevel()).thenReturn(SkillLevel.VETERAN);
        when(options.isUseTactics()).thenReturn(false);
        when(options.isUseInitiativeBonus()).thenReturn(false);

        RandomSkillPreferences randomSkillPreferences = mock(RandomSkillPreferences.class);
        when(randomSkillPreferences.randomizeSkill()).thenReturn(false);
        when(randomSkillPreferences.getCommandSkillsModifier(org.mockito.ArgumentMatchers.anyInt())).thenReturn(0);

        when(campaign.getPlayer()).thenReturn(player);
        when(campaign.getGame()).thenReturn(game);

        when(campaign.getCampaignOptions()).thenReturn(options);
        when(campaign.getRandomSkillPreferences()).thenReturn(randomSkillPreferences);

        when(campaign.getGameYear()).thenReturn(3025);

        game.addPlayer(player.getId(), player);
    }

    private static Entity getShadowHawk() {
        String unitName = "Shadow Hawk SHD-2H";
        Entity entity = getEntityForUnitTesting(unitName, false);
        assertNotNull(entity, unitName + " couldn't be found");
        return entity;
    }

    @Test
    void setTestFunction() {
        IntelItem macguffin = new IntelItem();

        IntelOutcome findMacGuffin = new IntelOutcome();
        Supplier<Boolean> testFunction = () -> macguffin.isDiscovered();
        findMacGuffin.setTestFunction(testFunction);

        assertFalse(testFunction.get());
        assertFalse(findMacGuffin.checkAchieved());

        macguffin.setDiscovered(true);

        assertTrue(testFunction.get());
        assertTrue(findMacGuffin.checkAchieved());
    }

    @Test
    void setApplyFunction() {
        // This time the Item is a Bad Guy, but we want to kill one of the player's agents
        // if they attempt to capture him.
        IntelItem badguy = new IntelItem();

        // Our plucky investigator
        Person npcAgent = new Person("Bob", "Spyla", null, "DC");
        npcAgent.setHits(0);

        // The Outcome we fear
        IntelOutcome killAPerson = new IntelOutcome();

        // Test function: fire if badguy was captured rather than destroyed
        Supplier<Boolean> testFunction = () -> badguy.isCaptured();
        killAPerson.setTestFunction(testFunction);

        // What will happen
        Runnable killAgent = () -> npcAgent.setHits(6);
        killAPerson.setApplyFunction(killAgent);

        assertFalse(killAPerson.checkAchieved());
        assertEquals(0, npcAgent.getHits());

        // Now pretend our intrepid agent tracked down the Bad Guy, late at night, on their own...
        badguy.setCaptured(true);
        if (killAPerson.checkAchieved()) {
            // Bad luck, Agent!
            killAPerson.apply();
        }

        // Dare we check...?
        assertTrue(killAPerson.checkAchieved());
        assertEquals(6, npcAgent.getHits());
    }

    @Test
    void testGiveUnitsToPlayerOnSuccess() {
        // Give the player two shiny new Shadowhawks if they deliver the MacGuffin to their employer
        // (which will be implemented via an IntelEvent; we just need to record the IntelItem and
        // add the item the IntelOutcome's list of linked objects.
        Mek prize1 = (Mek) getShadowHawk();
        Mek prize2 = (Mek) getShadowHawk();

        IntelItem incriminatingPhotos = new IntelItem();
        IntelOutcome deliveryCompleted = new IntelOutcome();

        // Link the objects here?  Or should this be in IntelItem?
        // At any rate, objects _could_ live in the lambdas but then they're much harder to
        // list up.
        deliveryCompleted.addLinkedObjects(new ArrayList(List.of(prize1, prize2)));

        // This test requires four things to be true.
        Supplier<Boolean> testFunction = () -> { return
            (incriminatingPhotos.isDiscovered() &&
                   incriminatingPhotos.isCaptured() &&
                   incriminatingPhotos.isDeciphered() &&
                   incriminatingPhotos.isDelivered());
        };
        deliveryCompleted.setTestFunction(testFunction);

        // Rather than hard-code the player who will receive the prizes, use the beneficiary field.
        deliveryCompleted.setBeneficiaryId(player.getId());

        // The IntelOutcome has the prizes, so the applyFunction can be relatively simple.
        Runnable applyFunction = () -> {
            for (Object item : deliveryCompleted.getLinkedObjects()) {
                Mek prize = (Mek) item;
                // Give the prize to the _current_ beneficiary; allows for runtime shenanigans.
                prize.setOwner(game.getPlayer(deliveryCompleted.getBeneficiaryId()));
            }
        };
        deliveryCompleted.setApplyFunction(applyFunction);

        // Let's assume the player has found, retrieved, decoded, and delivered the photos
        // (Which should take 4 events of various kinds)
        incriminatingPhotos.setDiscovered(true);
        incriminatingPhotos.setCaptured(true);
        incriminatingPhotos.setDeciphered(true);
        incriminatingPhotos.setDelivered(true);

        // This may get simplified later, since the apply function can also call this method.
        if (deliveryCompleted.checkAchieved()) {
            deliveryCompleted.apply();
        }

        // Check who owns the shiny new meks
        assertEquals(player.getId(), prize1.getOwnerId());
        assertEquals(player.getId(), prize2.getOwnerId());
    }

    @Test
    void testIntelOutcomeToString() {
        String mainTitle = "It's A Brand New Car!";
        String itemCount1 = " [1 item]";
        String itemCount2 = " [2 items]";
        String complete = " (Done)";

        Tank aCar = new Tank();
        Tank anotherCar = new Tank();

        IntelItem buyACar = new IntelItem();
        IntelOutcome outcome = new IntelOutcome();
        outcome.setTitle(mainTitle);

        Supplier<Boolean> testFunction = () -> buyACar.isCaptured();
        outcome.setTestFunction(testFunction);

        assertEquals(mainTitle, outcome.toString());

        outcome.addLinkedObject(aCar);
        assertEquals(mainTitle + itemCount1, outcome.toString());

        outcome.addLinkedObject(anotherCar);
        assertEquals(mainTitle + itemCount2, outcome.toString());

        buyACar.setCaptured(true);
        assertEquals(mainTitle + itemCount2 + complete, outcome.toString());
    }
}
