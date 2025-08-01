/*
 * Copyright (c) 2014 Carl Spain. All rights reserved.
 * Copyright (C) 2014-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.dialog;

import static mekhq.campaign.universe.Faction.getActiveMercenaryOrganization;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import megamek.client.ui.preferences.JIntNumberSpinnerPreference;
import megamek.client.ui.preferences.JTablePreference;
import megamek.client.ui.preferences.JToggleButtonPreference;
import megamek.client.ui.preferences.JWindowPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.common.util.sorter.NaturalOrderComparator;
import megamek.logging.MMLogger;
import megamek.utilities.FastJScrollPane;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.JumpPath;
import mekhq.campaign.finances.enums.TransactionType;
import mekhq.campaign.market.contractMarket.AbstractContractMarket;
import mekhq.campaign.market.contractMarket.ContractAutomation;
import mekhq.campaign.mission.AtBContract;
import mekhq.campaign.mission.Contract;
import mekhq.campaign.mission.enums.AtBContractType;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import mekhq.campaign.universe.factionStanding.FactionStandingUtilities;
import mekhq.campaign.universe.factionStanding.FactionStandings;
import mekhq.gui.FactionComboBox;
import mekhq.gui.baseComponents.immersiveDialogs.ImmersiveDialogSimple;
import mekhq.gui.dialog.factionStanding.events.FactionStandingGreeting;
import mekhq.gui.dialog.resupplyAndCaches.DialogContractStart;
import mekhq.gui.sorter.FormattedNumberSorter;
import mekhq.gui.sorter.IntegerStringSorter;
import mekhq.gui.view.ContractSummaryPanel;

/**
 * Presents contract offers generated by ContractMarket
 * <p>
 * Code borrowed heavily from PersonnelMarketDialog
 *
 * @author Neoancient
 */
public class ContractMarketDialog extends JDialog {
    private static final MMLogger logger = MMLogger.create(ContractMarketDialog.class);

    /* Save these settings between instantiations */
    private static boolean payMRBC = true;
    private static int advance = 25;
    private static int signingBonus = 0;
    private static int sharePct = 20;

    private final Campaign campaign;
    private final AbstractContractMarket contractMarket;
    private Contract selectedContract = null;
    private final List<String> possibleRetainerContracts;

    private JScrollPane scrollContractView;
    private ContractSummaryPanel contractView;

    private JCheckBox chkMRBC;
    private JSpinner spnSigningBonus;
    private JSpinner spnAdvance;
    private JSpinner spnSharePct;
    private JTable tableContracts;
    private JLabel lblCurrentRetainer;
    private JLabel lblRetainerEmployer;
    private JButton btnEndRetainer;
    private JLabel lblRetainerAvailable;
    private FactionComboBox cbRetainerEmployer;
    private JButton btnStartRetainer;

    final static ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.ContractMarketDialog",
          MekHQ.getMHQOptions().getLocale());

    public ContractMarketDialog(final JFrame frame, final Campaign campaign) {
        super(frame, true);
        this.campaign = campaign;
        contractMarket = campaign.getContractMarket();
        possibleRetainerContracts = new ArrayList<>();
        if (campaign.getFaction().isMercenary()) {
            countSuccessfulContracts();
        }
        initComponents();
        setLocationRelativeTo(frame);
        setUserPreferences();
    }

    /*
     * A balance of six or more successful contracts with the same
     * employer results in the offer of a retainer contract.
     */
    private void countSuccessfulContracts() {
        List<String> retainers = getPossibleRetainerContracts(campaign);
        for (String key : retainers) {
            possibleRetainerContracts.add(key);
        }
    }

    /**
     * Returns the list of possible retainer contracts for a mercenary faction.
     * A retainer contract becomes available when a faction has 6 or more
     * successful contracts with the same employer.
     *
     * @param campaign the campaign to check retainer contracts for
     * @return the number of available retainer contracts
     */
    private static List<String> getPossibleRetainerContracts(Campaign campaign) {
        HashMap<String, Integer> successfulContracts = new HashMap<>();
        List<String> retainers = new ArrayList<>();
        for (AtBContract contract : campaign.getCompletedAtBContracts()) {
            if (contract.getEmployerCode().equals(campaign.getRetainerEmployerCode())) {
                continue;
            }
            int num = successfulContracts.getOrDefault(contract.getEmployerCode(), 0);
            successfulContracts.put(contract.getEmployerCode(), num + (contract.getStatus().isSuccess() ? 1 : -1));
        }
        for (String key : successfulContracts.keySet()) {
            if (successfulContracts.get(key) >= 6) {
                retainers.add(key);
            }
        }
        return retainers;
    }

    /**
     * Returns the total number of contracts available for the given campaign.
     * This includes regular contracts from the contract market and potential
     * retainer contracts for mercenary factions.
     *
     * @param campaign the campaign to check contracts for
     * @return the total number of available contracts
     */
    public static int getAvailableContractsCount(Campaign campaign) {
        int contractCount = 0;
        
        // Add regular contracts from the contract market
        AbstractContractMarket contractMarket = campaign.getContractMarket();
        if (contractMarket != null) {
            contractCount += contractMarket.getContracts().size();
        }
        
        // Add retainer contracts if faction is mercenary
        if (campaign.getFaction().isMercenary()) {
            contractCount += getPossibleRetainerContracts(campaign).size();
        }
        
        return contractCount;
    }

    private void initComponents() {
        JScrollPane scrollTableContracts = new FastJScrollPane();
        scrollContractView = new FastJScrollPane();
        JPanel panelTable = new JPanel();
        JPanel panelFees = new JPanel();
        JPanel panelRetainer = new JPanel();
        JPanel panelOKBtns = new JPanel();
        contractView = null;
        JButton btnGenerate = new JButton();
        JButton btnRemove = new JButton();
        JButton btnAccept = new JButton();
        JButton btnClose = new JButton();

        chkMRBC = new JCheckBox();
        chkMRBC.addItemListener(evt -> {
            payMRBC = chkMRBC.isSelected();
            for (Contract c : contractMarket.getContracts()) {
                c.setMRBCFee(payMRBC);
                c.calculateContract(campaign);
            }
            if (contractView != null) {
                contractView.refreshAmounts();
            }
        });
        JLabel lblAdvance = new JLabel();
        spnAdvance = new JSpinner(new SpinnerNumberModel(advance, 0, 25, 5));
        spnAdvance.addChangeListener(evt -> {
            advance = (Integer) spnAdvance.getValue();
            for (Contract c : contractMarket.getContracts()) {
                c.setAdvancePct(advance);
                c.calculateContract(campaign);
            }
            if (contractView != null) {
                contractView.refreshAmounts();
            }
        });
        JLabel lblSigningBonus = new JLabel();
        spnSigningBonus = new JSpinner(new SpinnerNumberModel(signingBonus, 0, 10, 1));
        spnSigningBonus.addChangeListener(evt -> {
            signingBonus = (Integer) spnSigningBonus.getValue();
            for (Contract c : contractMarket.getContracts()) {
                c.setSigningBonusPct(signingBonus);
                c.calculateContract(campaign);
            }
            if (contractView != null) {
                contractView.refreshAmounts();
            }
        });

        JLabel lblSharePct = new JLabel();
        spnSharePct = new JSpinner(new SpinnerNumberModel(sharePct, 20, 50, 10));
        spnSharePct.addChangeListener(evt -> {
            sharePct = (Integer) spnSharePct.getValue();
            for (Contract c : contractMarket.getContracts()) {
                if (campaign.getCampaignOptions().isUseAtB() &&
                          campaign.getCampaignOptions().isUseShareSystem() &&
                          c instanceof AtBContract) {
                    ((AtBContract) c).setAtBSharesPercent(sharePct);
                    c.calculateContract(campaign);
                }
            }
            if (contractView != null) {
                contractView.refreshAmounts();
            }
        });

        lblCurrentRetainer = new JLabel();
        lblRetainerEmployer = new JLabel();
        btnEndRetainer = new JButton();
        lblRetainerAvailable = new JLabel();
        cbRetainerEmployer = new FactionComboBox();
        btnStartRetainer = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(resourceMap.getString("Form.title"));
        setName("Form");
        getContentPane().setLayout(new BorderLayout());

        scrollTableContracts.setMinimumSize(new Dimension(500, 400));
        scrollTableContracts.setName("scrollTableContracts");
        scrollTableContracts.setPreferredSize(new Dimension(500, 400));

        chkMRBC.setName("chkMRBC");
        if (campaign.isPirateCampaign()) {
            chkMRBC.setText(resourceMap.getString("checkMRBC.text.pirate"));
        } else {
            Faction mercenaryOrganization = getActiveMercenaryOrganization(campaign.getGameYear());
            String organizationInitials = mercenaryOrganization.getShortName();
            chkMRBC.setText(String.format(resourceMap.getString("checkMRBC.text"), organizationInitials));
        }
        chkMRBC.setSelected(payMRBC);
        panelFees.add(chkMRBC);

        lblAdvance.setText(resourceMap.getString("lblAdvance.text"));
        panelFees.add(lblAdvance);
        panelFees.add(spnAdvance);
        lblSigningBonus.setText(resourceMap.getString("lblSigningBonus.text"));
        panelFees.add(lblSigningBonus);
        panelFees.add(spnSigningBonus);
        lblSharePct.setText(resourceMap.getString("lblSharePct.text"));
        if (campaign.getCampaignOptions().isUseShareSystem()) {
            panelFees.add(lblSharePct);
            panelFees.add(spnSharePct);
        }

        boolean isOverridingCommandCircuit = campaign.isOverridingCommandCircuitRequirements();
        boolean isGM = campaign.isGM();
        boolean isUseCommandCircuit = isOverridingCommandCircuit && isGM;

        boolean isUseFactionStandingCommandCircuits =
              campaign.getCampaignOptions().isUseFactionStandingCommandCircuitSafe();
        FactionStandings factionStandings = campaign.getFactionStandings();

        Vector<Vector<String>> data = new Vector<>();
        for (Contract contract : contractMarket.getContracts()) {
            // Changes in rating or force size since creation can alter some details
            if (contract instanceof AtBContract atbContract) {
                atbContract.initContractDetails(campaign);
                campaign.getContractMarket().calculatePaymentMultiplier(campaign, atbContract);
                atbContract.setPartsAvailabilityLevel(atbContract.getContractType().calculatePartsAvailabilityLevel());
                atbContract.setAtBSharesPercent(campaign.getCampaignOptions().isUseShareSystem() ?
                                                      (Integer) spnSharePct.getValue() :
                                                      0);
                if (!isUseCommandCircuit) {
                    isUseCommandCircuit = FactionStandingUtilities.isUseCommandCircuit(isOverridingCommandCircuit, isGM,
                          isUseFactionStandingCommandCircuits, factionStandings, List.of(atbContract));
                }
            }
            contract.setStartDate(null);
            contract.setMRBCFee(payMRBC);
            contract.setAdvancePct(advance);
            contract.setSigningBonusPct(signingBonus);
            contract.calculateContract(campaign);

            Vector<String> row = new Vector<>();
            if (contract instanceof AtBContract) {
                row.add(((AtBContract) contract).getEmployerName(campaign.getGameYear()));
                row.add(((AtBContract) contract).getEnemyName(campaign.getGameYear()));
                if (((AtBContract) contract).isSubcontract()) {
                    row.add(((AtBContract) contract).getContractType() + " (Subcontract)");
                } else {
                    row.add(((AtBContract) contract).getContractType().toString());
                }
            } else {
                row.add(contract.getEmployer());
                row.add("");
                row.add(contract.getType());
            }

            final JumpPath path = campaign.calculateJumpPath(campaign.getCurrentSystem(), contract.getSystem());
            final int days = (int) Math.ceil(path.getTotalTime(contract.getStartDate(),
                  campaign.getLocation().getTransitTime(), isUseCommandCircuit));
            row.add(Integer.toString(days));
            row.add(String.valueOf(contract.getLength()));
            row.add(contract.getTransportCompString());
            row.add(contract.getSalvagePctString());
            row.add(contract.getStraightSupportString());
            row.add(contract.getBattleLossCompString());
            row.add(contract.getEstimatedTotalProfit(campaign).toAmountAndSymbolString());
            data.add(row);
        }

        Vector<String> colNames = new Vector<>();
        colNames.add("Employer");
        colNames.add("Enemy");
        colNames.add("Mission Type");
        colNames.add("Transit Time");
        colNames.add("Contract Length (months)");
        colNames.add("Transport Terms");
        colNames.add("Salvage Rights");
        colNames.add("Straight Support");
        colNames.add("Battle Loss Compensation");
        colNames.add("Estimated Profit");

        tableContracts = new JTable();
        DefaultTableModel tblContractsModel = new DefaultTableModel(data, colNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableContracts.setModel(tblContractsModel);
        tableContracts.setName("tableContracts");
        tableContracts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableContracts.createDefaultColumnsFromModel();
        tableContracts.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableContracts.getSelectionModel().addListSelectionListener(evt -> {
            if (!evt.getValueIsAdjusting()) {
                contractChanged();
            }
        });

        tableContracts.setIntercellSpacing(new Dimension(0, 0));
        tableContracts.setShowGrid(false);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tblContractsModel);
        sorter.setComparator(0, new NaturalOrderComparator());
        sorter.setComparator(1, new NaturalOrderComparator());
        sorter.setComparator(2, new NaturalOrderComparator());
        sorter.setComparator(3, new IntegerStringSorter());
        sorter.setComparator(4, new FormattedNumberSorter());
        tableContracts.setRowSorter(sorter);
        scrollTableContracts.setViewportView(tableContracts);

        scrollContractView.setMinimumSize(new Dimension(500, 600));
        scrollContractView.setPreferredSize(new Dimension(500, 600));
        scrollContractView.setViewportView(null);

        panelTable.setLayout(new BorderLayout());
        panelTable.add(panelFees, BorderLayout.PAGE_START);
        panelTable.add(scrollTableContracts, BorderLayout.CENTER);
        panelTable.add(panelRetainer, BorderLayout.PAGE_END);

        panelRetainer.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        lblCurrentRetainer.setText(resourceMap.getString("lblCurrentRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        panelRetainer.add(lblCurrentRetainer, gbc);
        if (null != campaign.getRetainerEmployerCode()) {
            lblRetainerEmployer.setText(Factions.getInstance()
                                              .getFaction(campaign.getRetainerEmployerCode())
                                              .getFullName(campaign.getGameYear()));
        }
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelRetainer.add(lblRetainerEmployer, gbc);
        btnEndRetainer.setText(resourceMap.getString("btnEndRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelRetainer.add(btnEndRetainer, gbc);
        lblCurrentRetainer.setVisible(null != campaign.getRetainerEmployerCode());
        lblRetainerEmployer.setVisible(null != campaign.getRetainerEmployerCode());
        btnEndRetainer.setVisible(null != campaign.getRetainerEmployerCode());
        btnEndRetainer.addActionListener(ev -> {
            campaign.setRetainerEmployerCode(null);
            campaign.setRetainerStartDate(null);
            lblCurrentRetainer.setVisible(false);
            lblRetainerEmployer.setVisible(false);
            btnEndRetainer.setVisible(false);
            // Add faction back to available ones
            countSuccessfulContracts();
            lblRetainerAvailable.setVisible(!possibleRetainerContracts.isEmpty());
            cbRetainerEmployer.setVisible(!possibleRetainerContracts.isEmpty());
            btnStartRetainer.setVisible(!possibleRetainerContracts.isEmpty());
        });

        lblRetainerAvailable.setText(resourceMap.getString("lblRetainerAvailable.text"));
        gbc.gridx = 0;
        gbc.gridy = 2;
        panelRetainer.add(lblRetainerAvailable, gbc);
        cbRetainerEmployer.addFactionEntries(possibleRetainerContracts, campaign.getGameYear());
        gbc.gridx = 1;
        gbc.gridy = 2;
        panelRetainer.add(cbRetainerEmployer, gbc);
        btnStartRetainer.setText(resourceMap.getString("btnStartRetainer.text"));
        gbc.gridx = 0;
        gbc.gridy = 3;
        panelRetainer.add(btnStartRetainer, gbc);
        lblRetainerAvailable.setVisible(!possibleRetainerContracts.isEmpty());
        cbRetainerEmployer.setVisible(!possibleRetainerContracts.isEmpty());
        btnStartRetainer.setVisible(!possibleRetainerContracts.isEmpty());
        btnStartRetainer.addActionListener(e -> {
            campaign.setRetainerEmployerCode(cbRetainerEmployer.getSelectedItemKey());
            lblCurrentRetainer.setVisible(true);
            lblRetainerEmployer.setVisible(true);
            btnEndRetainer.setVisible(true);
            lblRetainerEmployer.setText(Factions.getInstance()
                                              .getFaction(campaign.getRetainerEmployerCode())
                                              .getFullName(campaign.getGameYear()));
            // Remove the selected faction and add the previous one, if any
            countSuccessfulContracts();
            lblRetainerAvailable.setVisible(!possibleRetainerContracts.isEmpty());
            cbRetainerEmployer.setVisible(!possibleRetainerContracts.isEmpty());
            btnStartRetainer.setVisible(!possibleRetainerContracts.isEmpty());
        });

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelTable, scrollContractView);
        splitMain.setOneTouchExpandable(true);
        splitMain.setResizeWeight(0.0);
        getContentPane().add(splitMain, BorderLayout.CENTER);

        panelOKBtns.setLayout(new GridBagLayout());

        btnGenerate.setText(resourceMap.getString("btnGenerate.text"));
        btnGenerate.setName("btnGenerate");
        boolean finalIsUseCommandCircuit = isUseCommandCircuit;
        btnGenerate.addActionListener(evt -> {
            AtBContract contract = contractMarket.addAtBContract(campaign);

            if (contract == null) {
                campaign.addReport(resourceMap.getString("report.UnableToGMContract"));
                return;
            }

            contract.initContractDetails(campaign);
            contract.setPartsAvailabilityLevel(contract.getContractType().calculatePartsAvailabilityLevel());
            contract.setAtBSharesPercent(campaign.getCampaignOptions().isUseShareSystem() ?
                                        (Integer) spnSharePct.getValue() :
                                        0);
            contract.setStartDate(null);
            contract.setMRBCFee(payMRBC);
            contract.setAdvancePct(advance);
            contract.setSigningBonusPct(signingBonus);

            contract.calculateContract(campaign);
            Vector<String> row = new Vector<>();
            row.add(contract.getEmployerName(campaign.getGameYear()));
            row.add(contract.getEnemyName(campaign.getGameYear()));
            row.add(contract.getContractType().toString());
            final JumpPath path = campaign.calculateJumpPath(campaign.getCurrentSystem(), contract.getSystem());
            final int days = (int) Math.ceil(path.getTotalTime(contract.getStartDate(),
                  campaign.getLocation().getTransitTime(), finalIsUseCommandCircuit));
            row.add(Integer.toString(days));
            row.add(String.valueOf(contract.getLength()));
            row.add(contract.getTransportCompString());
            row.add(contract.getSalvagePctString());
            row.add(contract.getStraightSupportString());
            row.add(contract.getBattleLossCompString());
            row.add(contract.getEstimatedTotalProfit(campaign).toAmountAndSymbolString());
            ((DefaultTableModel) tableContracts.getModel()).addRow(row);
        });
        btnGenerate.setEnabled(campaign.isGM());
        panelOKBtns.add(btnGenerate, new GridBagConstraints());

        btnRemove.setText(resourceMap.getString("btnRemove.text"));
        btnRemove.setName("btnRemove");
        btnRemove.addActionListener(evt -> {
            if (selectedContract == null) {
                return;
            }
            contractMarket.removeContract(selectedContract);
            ((DefaultTableModel) tableContracts.getModel()).removeRow(tableContracts.convertRowIndexToModel(
                  tableContracts.getSelectedRow()));
        });
        panelOKBtns.add(btnRemove, new GridBagConstraints());

        btnAccept.setText(resourceMap.getString("btnAccept.text"));
        btnAccept.setName("btnAccept");
        btnAccept.addActionListener(this::acceptContract);
        panelOKBtns.add(btnAccept, new GridBagConstraints());

        btnClose.setText(resourceMap.getString("btnClose.text"));
        btnClose.setName("btnClose");
        btnClose.addActionListener(this::btnCloseActionPerformed);
        panelOKBtns.add(btnClose, new GridBagConstraints());

        getContentPane().add(panelOKBtns, BorderLayout.PAGE_END);

        pack();
    }

    /**
     * These need to be migrated to the Suite Constants / Suite Options Setup
     */
    private void setUserPreferences() {
        try {
            PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(ContractMarketDialog.class);

            chkMRBC.setName("payMRBCFee");
            preferences.manage(new JToggleButtonPreference(chkMRBC));

            spnAdvance.setName("advancePercentage");
            preferences.manage(new JIntNumberSpinnerPreference(spnAdvance));

            spnSigningBonus.setName("signingBonusPercentage");
            preferences.manage(new JIntNumberSpinnerPreference(spnSigningBonus));

            spnSharePct.setName("sharePercentage");
            preferences.manage(new JIntNumberSpinnerPreference(spnSharePct));

            tableContracts.setName("contractsTable");
            preferences.manage(new JTablePreference(tableContracts));

            this.setName("dialog");
            preferences.manage(new JWindowPreference(this));
        } catch (Exception ex) {
            logger.error("Failed to set user preferences", ex);
        }
    }

    public Contract getContract() {
        return selectedContract;
    }

    private void acceptContract(ActionEvent evt) {
        if (selectedContract != null) {
            if (selectedContract instanceof AtBContract contract) {
                contract.createEmployerLiaison(campaign);

                if (!triggerConfirmationDialog()) {
                    return;
                }

                if (contract.getEnemy().isClan()) {
                    contract.createClanOpponent(campaign);
                }
            }

            selectedContract.setName(contractView.getContractName());
            campaign.getFinances()
                  .credit(TransactionType.CONTRACT_PAYMENT,
                        campaign.getLocalDate(),
                        selectedContract.getTotalAdvanceAmount(),
                        "Advance funds for " + selectedContract.getName());
            campaign.addMission(selectedContract);
            // must be invoked after campaign.addMission to ensure presence of mission ID
            selectedContract.acceptContract(campaign);

            // Process Faction Standings Changes
            if (campaign.getCampaignOptions().isTrackFactionStanding()) {
                Faction enemy = null;
                boolean isGarrisonType = false;
                if (selectedContract instanceof AtBContract contract) {
                    enemy = contract.getEnemy();
                    isGarrisonType = contract.getContractType().isGarrisonType();
                }

                // Garrison Type contracts have a dynamic enemy. We update Standing whenever a new enemy is chosen.
                if (!isGarrisonType) {
                    FactionStandings factionStandings = campaign.getFactionStandings();
                    String standingsReport =
                          factionStandings.processContractAccept(campaign.getFaction().getShortName(), enemy,
                                campaign.getLocalDate());

                    if (standingsReport != null) {
                        campaign.addReport(standingsReport);
                    }
                }

                new FactionStandingGreeting(campaign, selectedContract);
            } else if (selectedContract instanceof AtBContract && campaign.getCampaignOptions().isUseStratCon()) {
                // The convoy dialog is wrapped in the Faction Standing greeting found just above this comment
                new DialogContractStart(campaign, (AtBContract) selectedContract);
            }

            ContractAutomation.contractStartPrompt(campaign, selectedContract);

            contractMarket.removeContract(selectedContract);
            ((DefaultTableModel) tableContracts.getModel()).removeRow(tableContracts.convertRowIndexToModel(
                  tableContracts.getSelectedRow()));
            refreshContractView();
        }
    }

    /**
     * Displays a confirmation dialog with a message and options to accept or refuse the mission.
     *
     * @return {@code true} if the accept button is clicked, {@code false} if the refuse button is clicked
     */
    private boolean triggerConfirmationDialog() {
        int difficulty = ((AtBContract) selectedContract).getDifficulty();

        // Get the resource string
        String inCharacterResourceKey = "";
        String outOfCharacterResourceKey = null;


        AtBContractType contractType = ((AtBContract) selectedContract).getContractType();
        if (contractType.isGarrisonDuty()) {
            inCharacterResourceKey = "messageChallengeGarrison.inCharacter";
            outOfCharacterResourceKey = "messageChallengeGarrison.outOfCharacter";
        } else if (contractType.isGuerrillaWarfare()) {
            inCharacterResourceKey = "messageChallengeGuerrilla.inCharacter";
            outOfCharacterResourceKey = "messageChallengeGuerrilla.outOfCharacter";
        } else {
            if (difficulty == -99) {
                inCharacterResourceKey = "messageChallengeUnknown.inCharacter";
                outOfCharacterResourceKey = "messageChallengeUnknown.outOfCharacter";
            } else if (difficulty <= 2) {
                inCharacterResourceKey = "messageChallengeVeryEasy.inCharacter";
                outOfCharacterResourceKey = "messageChallengeVeryEasy.outOfCharacter";
            } else if (difficulty > 8) {
                inCharacterResourceKey = "messageChallengeVeryHard.inCharacter";
                outOfCharacterResourceKey = "messageChallengeVeryHard.outOfCharacter";
            } else if (difficulty > 6) {
                inCharacterResourceKey = "messageChallengeHard.inCharacter";
                outOfCharacterResourceKey = "messageChallengeHard.outOfCharacter";
            }
        }

        // If resourceKey is not found, just return true, acting as if the player had
        // accepted the mission
        if (inCharacterResourceKey.isBlank()) {
            return true;
        }

        String inCharacterMessage = resourceMap.getString(inCharacterResourceKey);
        String outOfCharacterMessage = resourceMap.getString(outOfCharacterResourceKey);

        List<String> options = List.of(resourceMap.getString("button.cancel"), resourceMap.getString("button.accept"));

        ImmersiveDialogSimple dialog = new ImmersiveDialogSimple(campaign,
              ((AtBContract) selectedContract).getEmployerLiaison(),
              null,
              inCharacterMessage,
              options,
              outOfCharacterMessage,
              null,
              false);

        return dialog.getDialogChoice() != 0;
    }

    private void btnCloseActionPerformed(ActionEvent evt) {
        selectedContract = null;
        setVisible(false);
    }

    private void contractChanged() {
        int view = tableContracts.getSelectedRow();
        if (view < 0) {
            // selection got filtered away
            selectedContract = null;
            refreshContractView();
            return;
        }
        /* preserve the name given to the previous contract (if any) */
        if (selectedContract != null && contractView != null) {
            selectedContract.setName(contractView.getContractName());
        }

        selectedContract = contractMarket.getContracts().get(tableContracts.convertRowIndexToModel(view));
        refreshContractView();
    }

    void refreshContractView() {
        int row = tableContracts.getSelectedRow();
        if (row < 0) {
            contractView = null;
            scrollContractView.setViewportView(null);
            return;
        }
        contractView = new ContractSummaryPanel(selectedContract,
              campaign,
              campaign.getCampaignOptions().isUseAtB() &&
                    selectedContract instanceof AtBContract &&
                    !((AtBContract) selectedContract).isSubcontract() &&
                    !campaign.isPirateCampaign());
        scrollContractView.setViewportView(contractView);
        // This odd code is to make sure that the scrollbar stays at the top
        // I can't just call it here, because it ends up getting reset somewhere later
        SwingUtilities.invokeLater(() -> scrollContractView.getVerticalScrollBar().setValue(0));
    }
}
