/*
 * Copyright (C) 2013-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.campaign;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.swing.SwingWorker;

import megamek.common.UnitType;
import megamek.logging.MMLogger;
import mekhq.campaign.force.Force;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.PersonnelOptions;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.personnel.enums.Profession;
import mekhq.campaign.personnel.ranks.Rank;
import mekhq.campaign.personnel.skills.Attributes;
import mekhq.campaign.personnel.skills.Skill;
import mekhq.campaign.personnel.skills.SkillType;
import mekhq.campaign.unit.Unit;

public class MercRosterAccess extends SwingWorker<Void, Void> {
    private static final MMLogger logger = MMLogger.create(MercRosterAccess.class);

    // region Variable Declarations
    private Campaign campaign;
    private String username;
    private String hostname;
    private String passwd;
    private String table;
    private int port;
    private Statement statement = null;
    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private Properties conProperties;

    // we also need some hashes to cross-reference stuff by id
    private Map<String, Integer> skillHash;
    private Map<UUID, Integer> personHash;
    private Map<UUID, Integer> forceHash;

    // to track progress
    private String progressNote;
    private int progressTicker;
    // endregion Variable Declarations

    // region Constructors
    public MercRosterAccess(String h, int port, String t, String u, String p, Campaign c) {
        username = u;
        hostname = h;
        table = t;
        this.port = port;
        passwd = p;
        campaign = c;
        skillHash = new HashMap<>();
        personHash = new HashMap<>();
        forceHash = new HashMap<>();
        progressNote = "";
        progressTicker = 0;
    }
    // endregion Constructors

    public void connect() throws SQLException {
        conProperties = new Properties();
        conProperties.put("user", username);
        conProperties.put("password", passwd);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager.getConnection("jdbc:mysql://" + hostname + ':' + port + '/' + table, conProperties);
        } catch (SQLException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            logger.error("", e);
        }
    }

    public void writeCampaignData() {
        // TODO throw all SQLExceptions - but then where do they go from doInBackground?
        try {
            statement = connect.createStatement();
        } catch (SQLException e) {
            logger.error("", e);
        }

        writeBasicData();
        writeForceData();
        writePersonnelData();
        writeEquipmentData();
        // TODO: writeContractData
        // TODO: write logs?

        // Needed because otherwise progress isn't reaching 100 and the progress meter
        // stays open
        setProgress(100);
    }

    private void writeBasicData() {
        try {
            preparedStatement = connect.prepareStatement("UPDATE " + table + ".command SET name=? where id=1");
            preparedStatement.setString(1, truncateString(campaign.getName(), 100));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("", e);
        }

        progressNote = "Uploading dates";
        determineProgress();
        // write dates
        try {
            preparedStatement = connect.prepareStatement("UPDATE " + table + ".dates SET currentdate=?");
            preparedStatement.setDate(1, Date.valueOf(campaign.getLocalDate().toString()));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("", e);
        }
        progressTicker++;
        progressNote = "Uploading ranks";
        determineProgress();
        // write ranks
        try {
            statement.execute("TRUNCATE TABLE " + table + ".ranks");
            int i = 0;
            for (Rank rank : campaign.getRankSystem().getRanks()) {
                preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                   table +
                                                                   ".ranks (number, rankname) VALUES (?, ?)");
                preparedStatement.setInt(1, i);
                // TODO: This currently only exports MekWarrior Ranks. MercRoster software needs
                // adjusted before this can be.
                preparedStatement.setString(2, truncateString(rank.getName(Profession.MEKWARRIOR), 45));
                preparedStatement.executeUpdate();
                i++;
                progressTicker++;
                determineProgress();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        // write skill types
        progressNote = "Uploading skill types";
        determineProgress();
        try {
            statement.execute("TRUNCATE TABLE " + table + ".skilltypes");
            for (int i = 0; i < SkillType.skillList.length; i++) {
                preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                   table +
                                                                   ".skilltypes (name, shortname) VALUES (?, ?)");
                preparedStatement.setString(1, truncateString(SkillType.skillList[i], 60));
                preparedStatement.setString(2, truncateString(getShortSkillName(SkillType.skillList[i]), 60));
                preparedStatement.executeUpdate();
                skillHash.put(SkillType.skillList[i], i + 1);
                progressTicker++;
                determineProgress();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
        // write crewtypes
        progressNote = "Uploading personnel types";
        determineProgress();
        // TODO: get correct vehicle types and squad status
        try {
            statement.execute("TRUNCATE TABLE " + table + ".crewtypes");
            statement.execute("TRUNCATE TABLE " + table + ".skillrequirements");
            for (PersonnelRole role : PersonnelRole.values()) {
                // write skill requirements
                int equipment = 0;
                switch (role) {
                    case MEKWARRIOR:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_MEK));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_MEK));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case LAM_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_MEK));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_MEK));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_AERO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_AERO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case GROUND_VEHICLE_DRIVER:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_GVEE));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case NAVAL_VEHICLE_DRIVER:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_NVEE));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VTOL_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_VTOL));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VEHICLE_GUNNER:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_VEE));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VEHICLE_CREW:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_MECHANIC));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case AEROSPACE_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_AERO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_AERO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case CONVENTIONAL_AIRCRAFT_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_JET));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_JET));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case PROTOMEK_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_PROTO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case BATTLE_ARMOUR:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_BA));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case SOLDIER:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_SMALL_ARMS));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VESSEL_PILOT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_PILOT_SPACE));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VESSEL_GUNNER:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_GUN_SPACE));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VESSEL_CREW:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_VESSEL));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case VESSEL_NAVIGATOR:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_NAVIGATION));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        equipment = 1;
                        break;
                    case MEK_TECH:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_MEK));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case AERO_TEK:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_AERO));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case MECHANIC:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_MECHANIC));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case BA_TECH:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_TECH_BA));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case ASTECH:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_ASTECH));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case DOCTOR:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_SURGERY));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case MEDIC:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_MEDTECH));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case ADMINISTRATOR_COMMAND:
                    case ADMINISTRATOR_HR:
                    case ADMINISTRATOR_LOGISTICS:
                    case ADMINISTRATOR_TRANSPORT:
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skillrequirements (skilltype, personneltype) VALUES (?, ?)");
                        preparedStatement.setInt(1, skillHash.get(SkillType.S_ADMIN));
                        preparedStatement.setInt(2, role.ordinal());
                        preparedStatement.executeUpdate();
                        break;
                    case DEPENDENT:
                    case NONE:
                    default:
                        break;
                }
                preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                   table +
                                                                   ".crewtypes (type, squad, vehicletype, prefpos, equipment) VALUES (?, ?, ?, ?, ?)");
                preparedStatement.setString(1, truncateString(role.getLabel(campaign.getFaction().isClan()), 45));
                preparedStatement.setInt(2, 0);
                preparedStatement.setInt(3, 1);
                preparedStatement.setInt(4, role.ordinal());
                preparedStatement.setInt(5, equipment);
                preparedStatement.executeUpdate();
                progressTicker += 2;
                determineProgress();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }

        // write equipment types
        progressNote = "Uploading equipment types";
        determineProgress();
        try {
            statement.execute("TRUNCATE TABLE " + table + ".equipmenttypes");
            for (int i = 0; i < UnitType.SIZE; i++) {
                int maxweight = 100;
                int minweight = 20;
                int weightstep = 5;
                String weightscale = "ton";
                // TODO: get these right for various unit types
                preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                   table +
                                                                   ".equipmenttypes (id, name, license, maxweight, minweight, weightstep, weightscale, prefpos, used, requirement) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                preparedStatement.setInt(1, i + 1);
                preparedStatement.setString(2, truncateString(UnitType.getTypeDisplayableName(i), 45));
                preparedStatement.setInt(3, i + 1);
                preparedStatement.setInt(4, maxweight);
                preparedStatement.setInt(5, minweight);
                preparedStatement.setInt(6, weightstep);
                preparedStatement.setString(7, weightscale);
                preparedStatement.setInt(8, i + 1);
                preparedStatement.setInt(9, 1);
                preparedStatement.setInt(10, 1);
                preparedStatement.executeUpdate();
                progressTicker += 1;
                determineProgress();
            }
        } catch (SQLException e) {
            logger.error("", e);
        }
    }

    private void writeForceData() {

        // clear the table and re-enter a top level command
        try {
            statement.execute("TRUNCATE TABLE " + table + ".unit");
            preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                               table +
                                                               ".unit (type, name, parent, prefpos, text) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, "Command");
            preparedStatement.setInt(3, Integer.MAX_VALUE);
            preparedStatement.setInt(4, 0);
            preparedStatement.setString(5, campaign.getForces().getDescription());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("", e);
        }

        progressNote = "Uploading force data";
        determineProgress();
        // TODO: top level force gets written to ?WHERE?
        // now iterate through subforces
        for (Force sub : campaign.getForces().getSubForces()) {
            writeForce(sub, 1);
        }
        for (UUID uid : campaign.getAllUnitsInTheTOE(false)) {
            Unit u = campaign.getUnit(uid);
            if ((u != null) && (u.getCommander() != null)) {
                forceHash.put(u.getCommander().getId(), 1);
            }
        }
    }

    private void writeForce(Force force, int parent) {
        try {
            preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                               table +
                                                               ".unit (type, name, parent, prefpos, text) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, force.getName());
            preparedStatement.setInt(3, parent);
            preparedStatement.setInt(4, 1);
            preparedStatement.setString(5, force.getDescription());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            logger.error("", e);
        }
        // retrieve the MercRoster id of this force
        int id = parent;
        try (ResultSet rs = statement.executeQuery("SELECT id FROM " + table + ".unit ORDER BY id DESC LIMIT 1")) {
            rs.next();
            id = rs.getInt("id");
        } catch (SQLException e) {
            logger.error("", e);
        }

        progressTicker += 2;
        determineProgress();
        // loop through subforces and call again
        for (Force sub : force.getSubForces()) {
            writeForce(sub, id);
        }

        // assign personnel uuids to hash
        for (UUID uid : force.getUnits()) {
            Unit u = campaign.getUnit(uid);
            if ((u != null) && (u.getCommander() != null)) {
                forceHash.put(u.getCommander().getId(), id);
            }
        }
    }

    private void writePersonnelData() {
        // check for a uuid column
        try (ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ".crew")) {
            // add in a UUID column if not already present
            if (!hasColumn(rs, "uuid")) {
                statement.execute("TRUNCATE TABLE " + table + ".crew");
                statement.execute("ALTER TABLE " + table + ".crew ADD uuid VARCHAR(40)");
            }
            statement.execute("TRUNCATE TABLE " + table + ".personnelpositions");
            statement.execute("TRUNCATE TABLE " + table + ".skills");
            statement.execute("TRUNCATE TABLE " + table + ".kills");
        } catch (SQLException e) {
            logger.error("", e);
        }

        progressNote = "Uploading personnel data";
        determineProgress();
        for (Person person : campaign.getPersonnel()) {
            int forceId = 0;
            // assign parent id from force hash
            if (null != forceHash.get(person.getId())) {
                forceId = forceHash.get(person.getId());
            }
            try {
                preparedStatement = connect.prepareStatement("UPDATE " +
                                                                   table +
                                                                   ".crew SET rank=?, lname=?, fname=?, callsign=?, status=?, parent=?, crewnumber=?, joiningdate=?, notes=?, bday=? WHERE uuid=?");
                preparedStatement.setInt(1, person.getRankNumeric());
                preparedStatement.setString(2, truncateString(person.getSurname(), 30));
                preparedStatement.setString(3, truncateString(person.getGivenName(), 30));
                preparedStatement.setString(4, truncateString(person.getCallsign(), 30));
                preparedStatement.setString(5, person.getStatus().toString());
                preparedStatement.setInt(6, forceId);
                preparedStatement.setInt(7, 1);
                // TODO: get joining date right
                preparedStatement.setDate(8, Date.valueOf(person.getDateOfBirth()));
                // TODO: combine personnel log with biography
                preparedStatement.setString(9, person.getBiography());
                preparedStatement.setDate(10, Date.valueOf(person.getDateOfBirth()));
                preparedStatement.setString(11, person.getId().toString());
                if (preparedStatement.executeUpdate() < 1) {
                    // no prior record so insert
                    preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                       table +
                                                                       ".crew (rank, lname, fname, callsign, status, parent, crewnumber, joiningdate, notes, bday, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setInt(1, person.getRankNumeric());
                    preparedStatement.setString(2, truncateString(person.getSurname(), 30));
                    preparedStatement.setString(3, truncateString(person.getGivenName(), 30));
                    preparedStatement.setString(4, truncateString(person.getCallsign(), 30));
                    preparedStatement.setString(5, person.getStatus().toString());
                    preparedStatement.setInt(6, forceId);
                    preparedStatement.setInt(7, 1);
                    preparedStatement.setDate(8, Date.valueOf(person.getDateOfBirth()));
                    preparedStatement.setString(9, person.getBiography());
                    preparedStatement.setDate(10, Date.valueOf(person.getDateOfBirth()));
                    preparedStatement.setString(11, person.getId().toString());
                    preparedStatement.executeUpdate();
                }

                // retrieve the MercRoster id of this person
                preparedStatement = connect.prepareStatement("SELECT id FROM " + table + ".crew WHERE uuid=?");
                preparedStatement.setString(1, person.getId().toString());
                ResultSet rs = preparedStatement.executeQuery();
                rs.next();
                int id = rs.getInt("id");
                rs.close();
                // put id in a hash for equipment assignment
                personHash.put(person.getId(), id);
                // assign the personnel position
                preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                   table +
                                                                   ".personnelpositions (personneltype, person) VALUES (?, ?)");
                preparedStatement.setInt(1, person.getPrimaryRole().ordinal());
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
                // write out skills to skills table
                PersonnelOptions options = person.getOptions();
                Attributes attributes = person.getATOWAttributes();
                int reputation = person.getReputation();
                for (int i = 0; i < SkillType.skillList.length; i++) {
                    if (person.hasSkill(SkillType.skillList[i])) {
                        Skill skill = person.getSkill(SkillType.skillList[i]);
                        preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                           table +
                                                                           ".skills (person, skill, value) VALUES (?, ?, ?)");
                        preparedStatement.setInt(1, id);
                        preparedStatement.setInt(2, i + 1);
                        preparedStatement.setInt(3, skill.getFinalSkillValue(options, attributes, reputation));
                        preparedStatement.executeUpdate();
                    }
                }
                // add kills
                // FIXME: the only issue here is we get duplicate kills for crewed vehicles
                // TODO: clean up the getWhatKilled string
                for (Kill k : campaign.getKillsFor(person.getId())) {
                    preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                       table +
                                                                       ".kills (parent, type, killdate, equipment) VALUES (?, ?, ?, ?)");
                    preparedStatement.setInt(1, id);
                    preparedStatement.setString(2, truncateString(k.getWhatKilled(), 45));
                    preparedStatement.setDate(3, Date.valueOf(k.getDate().toString()));
                    preparedStatement.setString(4, truncateString(k.getKilledByWhat(), 45));
                    preparedStatement.executeUpdate();
                }
                progressTicker += 4;
                determineProgress();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    private void writeEquipmentData() {
        // TODO: we need to clear the equipment table because equipment will come and go

        // check for a uuid column
        try {
            // add in a UUID column if not already present
            ResultSet rs = statement.executeQuery("SELECT * FROM " + table + ".equipment");
            if (!hasColumn(rs, "uuid")) {
                statement.execute("ALTER TABLE " + table + ".equipment ADD uuid VARCHAR(40)");
            }
            rs.close();
        } catch (SQLException e) {
            logger.error("", e);
        }

        progressNote = "Uploading equipment data";
        determineProgress();
        for (Unit u : campaign.getHangar().getUnits()) {
            try {
                preparedStatement = connect.prepareStatement("UPDATE " +
                                                                   table +
                                                                   ".equipment SET type=?, name=?, subtype=?, crew=?, weight=?, regnumber=?, notes=? WHERE uuid=?");
                preparedStatement.setInt(1, u.getEntity().getUnitType() + 1);
                preparedStatement.setString(2, truncateString(u.getEntity().getChassis(), 45));
                preparedStatement.setString(3, truncateString(u.getEntity().getModel(), 45));
                if (null != u.getCommander()) {
                    preparedStatement.setInt(4, personHash.get(u.getCommander().getId()));
                } else {
                    preparedStatement.setInt(4, 0);
                }
                preparedStatement.setInt(5, (int) Math.round(u.getEntity().getWeight()));
                preparedStatement.setInt(6, 1);
                preparedStatement.setString(7, u.getHistory());
                preparedStatement.setString(8, u.getId().toString());
                if (preparedStatement.executeUpdate() < 1) {
                    // no prior record so insert
                    preparedStatement = connect.prepareStatement("INSERT INTO " +
                                                                       table +
                                                                       ".equipment (type, name, subtype, crew, weight, regnumber, notes, uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                    preparedStatement.setInt(1, 1);
                    preparedStatement.setString(2, truncateString(u.getEntity().getChassis(), 45));
                    preparedStatement.setString(3, truncateString(u.getEntity().getModel(), 45));
                    if (null != u.getCommander()) {
                        preparedStatement.setInt(4, personHash.get(u.getCommander().getId()));
                    } else {
                        preparedStatement.setInt(4, 0);
                    }
                    preparedStatement.setInt(5, (int) Math.round(u.getEntity().getWeight()));
                    preparedStatement.setInt(6, 1);
                    preparedStatement.setString(7, u.getHistory());
                    preparedStatement.setString(8, u.getId().toString());
                    preparedStatement.executeUpdate();
                }
                // TODO: connect to a TRO
                progressTicker += 1;
                determineProgress();
            } catch (SQLException e) {
                logger.error("", e);
            }
        }
    }

    public void close() {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception ignored) {

        }
    }

    private static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    private static String getShortSkillName(String name) {
        name = name.split("/")[0];
        name = name.replaceAll("\\s", "");
        name = name.replaceAll("Hyperspace", "");
        return name;
    }

    private static String truncateString(String s, int len) {
        return (s.length() < len) ? s : s.substring(0, len - 1);
    }

    @Override
    protected Void doInBackground() {
        writeCampaignData();
        return null;
    }

    @Override
    public void done() {
        close();
    }

    public String getProgressNote() {
        return progressNote;
    }

    private int getLengthOfTask() {
        return 2 +
                     campaign.getRankSystem().getRanks().size() +
                     SkillType.skillList.length +
                     PersonnelRole.values().length * 2 +
                     UnitType.SIZE +
                     campaign.getPersonnel().size() * +campaign.getHangar().getUnits().size() +
                     campaign.getAllForces().size() * 2;
    }

    public void determineProgress() {
        double percent = ((double) progressTicker) / getLengthOfTask();
        percent = Math.min(percent, 1.0);
        setProgress((int) Math.ceil(percent * 100));
    }
}
