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

import megamek.Version;
import megamek.common.Player;
import megamek.common.enums.SkillLevel;
import megamek.common.equipment.EquipmentType;
import megamek.common.game.Game;
import megamek.common.units.Entity;
import megamek.common.units.Mek;
import megamek.common.units.Tank;
import mekhq.campaign.Campaign;
import mekhq.campaign.campaignOptions.CampaignOptions;
import mekhq.campaign.espionage.inteltypes.PositionIntel;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.skills.RandomSkillPreferences;
import mekhq.utilities.MHQXMLUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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
        ISerializableSupplier<Boolean> testFunction = () -> macguffin.isDiscovered();
        findMacGuffin.setTestFunction(testFunction);

        assertFalse(testFunction.get());
        assertFalse(findMacGuffin.checkAchieved());

        macguffin.setDiscovered(true);

        assertTrue(testFunction.get());
        assertTrue(findMacGuffin.checkAchieved());
    }

    /**
     * NOTE: this test is not actual serializable due to closure over references in the test body!
     */
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
        ISerializableSupplier<Boolean> testFunction = () -> badguy.isCaptured();
        killAPerson.setTestFunction(testFunction);

        // What will happen
        ISerializableRunnable killAgent = () -> npcAgent.setHits(6);
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

    /**
     * NOTE: this test is not actual serializable due to closure over references in the test body!
     */
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
        ISerializableSupplier<Boolean> testFunction = () -> { return
            (incriminatingPhotos.isDiscovered() &&
                   incriminatingPhotos.isCaptured() &&
                   incriminatingPhotos.isDeciphered() &&
                   incriminatingPhotos.isDelivered());
        };
        deliveryCompleted.setTestFunction(testFunction);

        // Rather than hard-code the player who will receive the prizes, use the beneficiary field.
        deliveryCompleted.setBeneficiaryId(player.getId());

        // The IntelOutcome has the prizes, so the applyFunction can be relatively simple.
        ISerializableRunnable applyFunction = () -> {
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

        ISerializableSupplier<Boolean> testFunction = () -> buyACar.isCaptured();
        outcome.setTestFunction(testFunction);

        assertEquals(mainTitle, outcome.toString());

        outcome.addLinkedObject(aCar);
        assertEquals(mainTitle + itemCount1, outcome.toString());

        outcome.addLinkedObject(anotherCar);
        assertEquals(mainTitle + itemCount2, outcome.toString());

        buyACar.setCaptured(true);
        assertEquals(mainTitle + itemCount2 + complete, outcome.toString());
    }

    /**
     * NOTE: this test *is* serializable due to using reference IDs and Singleton getInstance() calls.
     */
    @Test
    void testSerializationIncludingTestFunctionAndApplyFunction()
          throws IOException, ParserConfigurationException, SAXException {
        Campaign mockCampaign = Mockito.mock(Campaign.class);

        // Set up SOI for lookups
        SphereOfInfluence soi = new SphereOfInfluence();
        soi.setSoiId(1);

        EspionageManager espionageManager = EspionageManager.getInstance();
        espionageManager.addSphereOfInfluence(soi);

        String name = "Incriminating Photos";
        IntelItem incriminatingPhotos = new IntelItem();
        incriminatingPhotos.setItemName(name);
        incriminatingPhotos.setItemId(1);

        // Note: these are serializable so can be saved in the IntelOutcome!
        Mek prize1 = (Mek) getShadowHawk();
        Mek prize2 = (Mek) getShadowHawk();

        // IntelOutcome defines one outcome of an IntelItem (usually at the end of a Mission / SOI lifetime)
        // or IntelEvent (usually one of several possible outcomes depending on defined checks)
        String title = name + " Delivered";
        soi.addIntelItem(incriminatingPhotos);
        IntelOutcome deliveryCompleted = new IntelOutcome();
        deliveryCompleted.setTitle(title);
        deliveryCompleted.addLinkedObjects(new ArrayList(List.of(prize1, prize2)));

        // Have to use concrete values, lookups, and calls to Singletons to avoid trying to serialize the whole unittest
        // object itself!
        ISerializableSupplier<Boolean> testFunction = () -> {
            SphereOfInfluence targetSOI = EspionageManager.getInstance().getSphereOfInfluence(1);
            IntelItem target = (targetSOI != null) ? targetSOI.getIntelItem(1) : null;
            if (target != null) {
                return
                      (target.isDiscovered() &&
                             target.isCaptured() &&
                             target.isDeciphered() &&
                             target.isDelivered());
            }
            return false;
        };

        deliveryCompleted.setTestFunction(testFunction);

        deliveryCompleted.setBeneficiaryId(player.getId());

        // The IntelOutcome has the prizes, so the applyFunction can be relatively simple.
        // Have to use concrete values, lookups, and calls to Singletons to avoid trying to serialize the whole unittest
        // object itself!
        ISerializableRunnable applyFunction = () -> {
            SphereOfInfluence targetSOI = EspionageManager.getInstance().getSphereOfInfluence(1);
            IntelItem target = (targetSOI != null) ? targetSOI.getIntelItem(1) : null;
            IntelOutcome outcome = (target != null) ? target.getOutcomeByTitle(title) : null;

            if (outcome != null) {
                for (Object item : outcome.getLinkedObjects()) {
                    Mek prize = (Mek) item;
                    // Give the prize to the _current_ beneficiary; allows for runtime shenanigans.
                    // Except for this test, don't query the campaign, just make a new Player instance
                    prize.setOwner(new Player(outcome.getBeneficiaryId(), "Fake player"));
                }
            }
        };
        deliveryCompleted.setApplyFunction(applyFunction);

        // Let's assume the player has found, retrieved, decoded, and delivered the photos
        // (Which should take 4 events of various kinds)
        incriminatingPhotos.setDiscovered(true);
        incriminatingPhotos.setCaptured(true);
        incriminatingPhotos.setDeciphered(true);
        incriminatingPhotos.setDelivered(true);

        String xmlBlock;

        // Make sure we got something in the writer.
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            deliveryCompleted.writeToXML(mockCampaign, pw, 0);
            xmlBlock = sw.toString();

            assertFalse(xmlBlock.isEmpty());
        }

        // Using factory get an instance of document builder
        DocumentBuilder db = MHQXMLUtility.newSafeDocumentBuilder();

        // Parse using builder to get DOM representation of the XML file
        Document xmlDoc = db.parse(new ByteArrayInputStream(xmlBlock.getBytes()));
        Element node = xmlDoc.getDocumentElement();
        IntelOutcome deserialized = IntelOutcome.generateInstanceFromXML( node, mockCampaign,new Version());

        // This step would normally be performed by the IntelItem deserializer but we're not there yet.
        incriminatingPhotos.addOutcome(deserialized);

        // The deserialized test function should find the correct values
        assertTrue(deserialized.checkAchieved());

        // Run the
        if (deserialized.checkAchieved()) {
            deserialized.apply();
        }

        // This check is a hack because we had to create a new Player with the identical ID, but it illustrates the
        // functionality of a deserialized Runnable.
        for (Object object : deserialized.getLinkedObjects()) {
            if (object instanceof Mek mek) {
                assertEquals(player.getId(), mek.getOwnerId());
            } else {
                // We didn't find the type of objects we expected!
                fail();
            }
        }
    }
}
