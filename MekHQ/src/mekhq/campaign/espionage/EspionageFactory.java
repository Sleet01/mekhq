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

import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.Mission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.ResourceBundle;

public class EspionageFactory {

    protected static final ResourceBundle resources = ResourceBundle.getBundle(
          "mekhq.resources.Espionage",
          MekHQ.getMHQOptions().getLocale());

    private static EspionageFactory instance;
    private int nextSOIIndex = 0;
    // Probably some info about various intel agencies and factions goes here

    // TODO: add another Constructor with more arguments, for tweaking settings
    private EspionageFactory() {};

    public static EspionageFactory getInstance() {
        if (instance == null) {
            instance = new EspionageFactory();
        }
        return instance;
    }

    public ArrayList<SphereOfInfluence> generateSpheresOfInfluence(Campaign campaign, boolean includeTutorial) {
        ArrayList<SphereOfInfluence> spheresList = new ArrayList<>();

        List<Mission> missionList = campaign.getActiveMissions(true);
        int i = nextSOIIndex;
        if (includeTutorial) {
            SphereOfInfluence tutorial = generateTutorialSOI(campaign, missionList.getFirst(), i);
            spheresList.add(tutorial);
            i++;
        }
        for (Mission mission: missionList) {
            SphereOfInfluence soi = generateSphereOfInfluence(campaign, mission, i);
            spheresList.add(soi);
            i++;
        }
        nextSOIIndex = i;

        return spheresList;
    }

    // This is going to do a lot of lifting eventually
    public static SphereOfInfluence generateSphereOfInfluence(Campaign campaign, Mission mission, int soiId) {
        String title = generateTitle(campaign, mission);
        String description = generateDescription(campaign, mission, title);
        HashMap<Integer, HashMap<Integer, IntelRating>> actorsRatingsMap = generateActorsRatingsMap(campaign, mission);
        ArrayList<IntelItem> items = generateIntelItems(campaign, mission);
        HashMap<Integer, ArrayList<IntelEvent>> eventsMap = generateEventsMap(campaign, mission);

        SphereOfInfluence soi = new SphereOfInfluence(
              soiId,
              mission.getId(),
              title,
              description,
              actorsRatingsMap,
              items,
              eventsMap
        );

        return soi;
    }

    // Todo: add template system for tutorial
    public static SphereOfInfluence generateTutorialSOI(Campaign campaign, Mission mission, int soiId) {
        SphereOfInfluence soi = new SphereOfInfluence();
        soi.setSoiId(soiId);
        soi.setMissionId(mission.getId());
        soi.setTitle(generateTitle(campaign, mission) + " (Tutorial)");
        soi.setDescription(generateTutorialDescription(campaign, mission, soi.getTitle()));

        return soi;
    }

    private static HashMap<Integer, ArrayList<IntelEvent>> generateEventsMap(Campaign campaign, Mission mission) {
        HashMap<Integer, ArrayList<IntelEvent>> eventMap = new HashMap<>();
        return eventMap;
    }

    private static ArrayList<IntelItem> generateIntelItems(Campaign campaign, Mission mission) {
        ArrayList<IntelItem> itemsList = new ArrayList<>();
        return itemsList;
    }

    private static HashMap<Integer, HashMap<Integer, IntelRating>> generateActorsRatingsMap(Campaign campaign, Mission mission) {
        HashMap<Integer, HashMap<Integer, IntelRating>> ratingsMap = new HashMap<>();
        return ratingsMap;
    }

    private static String generateDescription(Campaign campaign, Mission mission, String title) {
        StringBuilder description = new StringBuilder();
        return description.toString();
    }

    private static String generateTutorialDescription(Campaign campaign, Mission mission, String title) {
        StringBuilder description = new StringBuilder();
        return description.toString();
    }

    public static String generateTitle(Campaign campaign, Mission mission) {
        String title = "Default Title";
        return title;
    }

    public static IntelEvent generateBasicEvent(Campaign campaign, SphereOfInfluence soi) {
        IntelEvent event = new IntelEvent();
        // TODO: fill in
        return event;
    }

    public static IntelItem generateBasicItem(Campaign campaign, SphereOfInfluence soi) {
        IntelItem item = new IntelItem();
        // TODO: fill in
        return item;
    }

    public static ArrayList<IntelEvent> generateTutorialEventChain(Campaign campaign, SphereOfInfluence soi) {
        ArrayList<IntelEvent> events = new ArrayList<>();
        // TODO: fill in
        return events;
    }

    public void setNextSOIIndex(int index) {
        nextSOIIndex = index;
    }

    public int getNextSOIIndex() {
        return nextSOIIndex;
    }
}
