/*
 * Copyright (C) 2021-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.panes;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import megamek.client.ui.models.XTableColumnModel;
import megamek.client.ui.dialogs.iconChooser.EntityImagePanel;
import megamek.client.ui.dialogs.unitSelectorDialogs.EntityViewPane;
import megamek.client.ui.preferences.JIntNumberSpinnerPreference;
import megamek.client.ui.preferences.JTabbedPanePreference;
import megamek.client.ui.preferences.JTablePreference;
import megamek.client.ui.preferences.JToggleButtonPreference;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.common.Compute;
import megamek.common.Entity;
import megamek.common.UnitType;
import megamek.common.annotations.Nullable;
import megamek.common.icons.Camouflage;
import megamek.common.util.sorter.NaturalOrderComparator;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.campaign.finances.Money;
import mekhq.campaign.finances.enums.TransactionType;
import mekhq.campaign.market.enums.UnitMarketType;
import mekhq.campaign.market.unitMarket.UnitMarketOffer;
import mekhq.gui.baseComponents.AbstractMHQSplitPane;
import mekhq.gui.model.UnitMarketTableModel;
import mekhq.gui.sorter.FormattedNumberSorter;
import mekhq.gui.sorter.WeightClassSorter;
import mekhq.gui.utilities.JScrollPaneWithSpeed;
import mekhq.utilities.ReportingUtilities;

public class UnitMarketPane extends AbstractMHQSplitPane {
    private static final MMLogger logger = MMLogger.create(UnitMarketPane.class);

    // region Variable Declarations
    private final Campaign campaign;

    // region Left Panel
    // Filters
    private JCheckBox chkShowMeks;
    private JCheckBox chkShowVehicles;
    private JCheckBox chkShowAerospace;
    private JCheckBox chkShowConvAero;
    private JCheckBox chkFilterByPercentageOfCost;
    private JSpinner spnCostPercentageThreshold;

    // Unit Image
    private EntityImagePanel entityImagePanel;

    // Unit Table
    private JTable marketTable;
    private UnitMarketTableModel marketModel;
    private TableRowSorter<UnitMarketTableModel> marketSorter;
    // endregion Left Panel

    // region Right Panel
    private EntityViewPane entityViewPane;
    // endregion Right Panel
    // endregion Variable Declarations

    // region Constructors
    public UnitMarketPane(final JFrame frame, final Campaign campaign) {
        super(frame, "UnitMarketPane");
        this.campaign = campaign;
        initialize();
    }
    // endregion Constructors

    // region Getters/Setters
    public Campaign getCampaign() {
        return campaign;
    }

    // region Left Panel
    // region Filters
    public JCheckBox getChkShowMeks() {
        return chkShowMeks;
    }

    public void setChkShowMeks(final JCheckBox chkShowMeks) {
        this.chkShowMeks = chkShowMeks;
    }

    public JCheckBox getChkShowVehicles() {
        return chkShowVehicles;
    }

    public void setChkShowVehicles(final JCheckBox chkShowVehicles) {
        this.chkShowVehicles = chkShowVehicles;
    }

    public JCheckBox getChkShowAerospace() {
        return chkShowAerospace;
    }

    public void setChkShowAerospace(final JCheckBox chkShowAerospace) {
        this.chkShowAerospace = chkShowAerospace;
    }

    public JCheckBox getChkShowConvAero() {
        return chkShowConvAero;
    }

    public void setChkShowConvAero(final JCheckBox chkShowConvAero) {
        this.chkShowConvAero = chkShowConvAero;
    }

    public JCheckBox getChkFilterByPercentageOfCost() {
        return chkFilterByPercentageOfCost;
    }

    public void setChkFilterByPercentageOfCost(final JCheckBox chkFilterByPercentageOfCost) {
        this.chkFilterByPercentageOfCost = chkFilterByPercentageOfCost;
    }

    public JSpinner getSpnCostPercentageThreshold() {
        return spnCostPercentageThreshold;
    }

    public void setSpnCostPercentageThreshold(final JSpinner spnCostPercentageThreshold) {
        this.spnCostPercentageThreshold = spnCostPercentageThreshold;
    }
    // endregion Filters

    // region Unit Image
    public EntityImagePanel getEntityImagePanel() {
        return entityImagePanel;
    }

    public void setEntityImagePanel(final EntityImagePanel entityImagePanel) {
        this.entityImagePanel = entityImagePanel;
    }
    // endregion Unit Image

    // region Unit Table
    public JTable getMarketTable() {
        return marketTable;
    }

    public void setMarketTable(final JTable marketTable) {
        this.marketTable = marketTable;
    }

    public UnitMarketTableModel getMarketModel() {
        return marketModel;
    }

    public void setMarketModel(final UnitMarketTableModel marketModel) {
        this.marketModel = marketModel;
    }

    public TableRowSorter<UnitMarketTableModel> getMarketSorter() {
        return marketSorter;
    }

    public void setMarketSorter(final TableRowSorter<UnitMarketTableModel> marketSorter) {
        this.marketSorter = marketSorter;
    }
    // endregion Unit Table
    // endregion Left Panel

    // region Right Panel
    public EntityViewPane getEntityViewPane() {
        return entityViewPane;
    }

    public void setEntityViewPane(final EntityViewPane entityViewPane) {
        this.entityViewPane = entityViewPane;
    }
    // endregion Right Panel
    // endregion Getters/Setters

    // region Initialization
    @Override
    protected Component createLeftComponent() {
        // Create Panel Components
        final JPanel filtersPanel = createFiltersPanel();

        setEntityImagePanel(new EntityImagePanel(null, new Camouflage()));

        final JScrollPane marketTableScrollPane = createMarketTablePane();

        final JLabel lblMarketDescriptions = new JLabel(resources.getString("lblMarketDescriptions.text"));

        // Layout the UI
        JPanel panel = new JPanel();
        panel.setName("filtersPanel");
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                      .addComponent(filtersPanel)
                                                      .addComponent(getEntityImagePanel(), Alignment.LEADING))
                                      .addComponent(marketTableScrollPane)
                                      .addComponent(lblMarketDescriptions));

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(filtersPanel)
                                                        .addComponent(getEntityImagePanel()))
                                        .addComponent(marketTableScrollPane)
                                        .addComponent(lblMarketDescriptions, Alignment.TRAILING));
        return panel;
    }

    private JPanel createFiltersPanel() {
        // Create Panel Components
        setChkShowMeks(new JCheckBox(resources.getString("chkShowMeks.text")));
        getChkShowMeks().setToolTipText(resources.getString("chkShowMeks.toolTipText"));
        getChkShowMeks().setName("chkShowMeks");
        getChkShowMeks().setSelected(true);
        getChkShowMeks().addActionListener(evt -> filterOffers());

        setChkShowVehicles(new JCheckBox(resources.getString("chkShowVehicles.text")));
        getChkShowVehicles().setToolTipText(resources.getString("chkShowVehicles.toolTipText"));
        getChkShowVehicles().setName("chkShowVehicles");
        getChkShowVehicles().setSelected(true);
        getChkShowVehicles().addActionListener(evt -> filterOffers());

        setChkShowAerospace(new JCheckBox(resources.getString("chkShowAerospace.text")));
        getChkShowAerospace().setToolTipText(resources.getString("chkShowAerospace.toolTipText"));
        getChkShowAerospace().setName("chkShowAerospace");
        getChkShowAerospace().addActionListener(evt -> filterOffers());

        setChkShowConvAero(new JCheckBox(resources.getString("chkShowConvAero.text")));
        getChkShowConvAero().setToolTipText(resources.getString("chkShowConvAero.toolTipText"));
        getChkShowConvAero().setName("chkShowConvAero");
        getChkShowConvAero().addActionListener(evt -> filterOffers());

        setChkFilterByPercentageOfCost(new JCheckBox(resources.getString("chkFilterByPercentageOfCost.text")));
        getChkFilterByPercentageOfCost().setToolTipText(resources.getString("chkFilterByPercentageOfCost.toolTipText"));
        getChkFilterByPercentageOfCost().setName("chkFilterByPercentageOfCost");
        getChkFilterByPercentageOfCost().getAccessibleContext()
              .setAccessibleDescription(resources.getString("chkFilterByPercentageOfCost.toolTipText"));
        getChkFilterByPercentageOfCost().addActionListener(evt -> filterOffers());

        setSpnCostPercentageThreshold(new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10)));
        getSpnCostPercentageThreshold().setToolTipText(resources.getString("spnFilterByPercentageOfCost.toolTipText"));
        getSpnCostPercentageThreshold().setName("spnCostPercentageThreshold");
        getSpnCostPercentageThreshold().getAccessibleContext()
              .setAccessibleDescription(resources.getString("spnFilterByPercentageOfCost.toolTipText"));
        getSpnCostPercentageThreshold().addChangeListener(evt -> filterOffers());

        JLabel lblCostPercentageThreshold = new JLabel(resources.getString("lblCostPercentageThreshold.text"));
        lblCostPercentageThreshold.setToolTipText(resources.getString("spnFilterByPercentageOfCost.toolTipText"));
        lblCostPercentageThreshold.setName("lblCostPercentageThreshold");
        lblCostPercentageThreshold.getAccessibleContext()
              .setAccessibleDescription(resources.getString("spnFilterByPercentageOfCost.toolTipText"));
        lblCostPercentageThreshold.setLabelFor(getSpnCostPercentageThreshold());

        // Layout the UI
        JPanel panel = new JPanel();
        panel.setName("filtersPanel");
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);

        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setVerticalGroup(layout.createSequentialGroup()
                                      .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                      .addComponent(getChkShowMeks())
                                                      .addComponent(getChkShowVehicles())
                                                      .addComponent(getChkShowAerospace())
                                                      .addComponent(getChkShowConvAero(), Alignment.LEADING))
                                      .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                      .addComponent(getChkFilterByPercentageOfCost())
                                                      .addComponent(getSpnCostPercentageThreshold())
                                                      .addComponent(lblCostPercentageThreshold, Alignment.LEADING)));

        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(getChkShowMeks())
                                                        .addComponent(getChkShowVehicles())
                                                        .addComponent(getChkShowAerospace())
                                                        .addComponent(getChkShowConvAero()))
                                        .addGroup(layout.createSequentialGroup()
                                                        .addComponent(getChkFilterByPercentageOfCost())
                                                        .addComponent(getSpnCostPercentageThreshold())
                                                        .addComponent(lblCostPercentageThreshold)));
        return panel;
    }

    private JScrollPane createMarketTablePane() {
        // Create Model
        setMarketModel(new UnitMarketTableModel(getCampaign().getUnitMarket().getOffers()));

        // Create Sorter
        setMarketSorter(new TableRowSorter<>(getMarketModel()));
        getMarketSorter().setComparator(UnitMarketTableModel.COL_WEIGHTCLASS, new WeightClassSorter());
        getMarketSorter().setComparator(UnitMarketTableModel.COL_UNIT, new NaturalOrderComparator());
        getMarketSorter().setComparator(UnitMarketTableModel.COL_PRICE, new FormattedNumberSorter());
        getMarketSorter().setComparator(UnitMarketTableModel.COL_PERCENT, new NaturalOrderComparator());

        // Create Column Model
        final XTableColumnModel columnModel = new XTableColumnModel();

        // Create Table
        setMarketTable(new JTable(getMarketModel(), columnModel, null));
        getMarketTable().setName("marketTable");
        getMarketTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        getMarketTable().setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        getMarketTable().createDefaultColumnsFromModel();
        getMarketTable().setRowSorter(getMarketSorter());
        for (int i = 0; i < UnitMarketTableModel.COL_NUM; i++) {
            final TableColumn column = getMarketTable().getColumnModel().getColumn(i);
            column.setPreferredWidth(getMarketModel().getColumnWidth(i));
            column.setCellRenderer(getMarketModel().getRenderer());
        }
        getMarketTable().setIntercellSpacing(new Dimension(0, 0));
        getMarketTable().setShowGrid(false);
        columnModel.setColumnVisible(columnModel.getColumnByModelIndex(UnitMarketTableModel.COL_DELIVERY),
              !getCampaign().getCampaignOptions().isInstantUnitMarketDelivery());
        getMarketTable().getSelectionModel().addListSelectionListener(evt -> updateDisplay());

        final JScrollPane marketTableScrollPane = new JScrollPaneWithSpeed(getMarketTable(),
              ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        marketTableScrollPane.setName("marketTableScrollPane");
        marketTableScrollPane.setMinimumSize(new Dimension(500, 400));
        marketTableScrollPane.setPreferredSize(new Dimension(700, 400));

        return marketTableScrollPane;
    }

    @Override
    protected Component createRightComponent() {
        setEntityViewPane(new EntityViewPane(getFrame(), null));
        return getEntityViewPane();
    }

    @Override
    protected void finalizeInitialization() throws Exception {
        super.finalizeInitialization();
        filterOffers();
    }

    @Override
    protected void setCustomPreferences(final PreferencesNode preferences) throws Exception {
        // Left Component
        preferences.manage(new JToggleButtonPreference(getChkShowMeks()));
        preferences.manage(new JToggleButtonPreference(getChkShowVehicles()));
        preferences.manage(new JToggleButtonPreference(getChkShowAerospace()));
        preferences.manage(new JToggleButtonPreference(getChkShowConvAero()));
        preferences.manage(new JToggleButtonPreference(getChkFilterByPercentageOfCost()));
        preferences.manage(new JIntNumberSpinnerPreference(getSpnCostPercentageThreshold()));
        preferences.manage(new JTablePreference(getMarketTable()));

        // Right Component
        preferences.manage(new JTabbedPanePreference(getEntityViewPane()));
    }
    // endregion Initialization

    public @Nullable Entity getSelectedEntity() {
        return (getMarketTable().getSelectedRow() < 0) ?
                     null :
                     getMarketModel().getOffer(getMarketTable().convertRowIndexToModel(getMarketTable().getSelectedRow()))
                           .map(UnitMarketOffer::getEntity)
                           .orElse(null);
    }

    /**
     * @return a list of all currently selected offers
     */
    public List<UnitMarketOffer> getSelectedOffers() {
        if (getMarketTable().getSelectedRowCount() == 0) {
            return new ArrayList<>();
        }

        final List<UnitMarketOffer> offers = new ArrayList<>();
        for (final int row : getMarketTable().getSelectedRows()) {
            if (row < 0) {
                continue;
            }
            getMarketModel().getOffer(getMarketTable().convertRowIndexToModel(row)).ifPresent(offers::add);
        }
        return offers;
    }

    // region Button Actions
    public void purchaseSelectedOffers() {
        final List<UnitMarketOffer> offers = getSelectedOffers();
        if (offers.isEmpty()) {
            return;
        }

        for (final Iterator<UnitMarketOffer> offersIterator = offers.iterator(); offersIterator.hasNext(); ) {
            final UnitMarketOffer offer = offersIterator.next();

            final Entity entity = offer.getEntity();
            if (entity == null) {
                logger.error("Cannot purchase a null entity");
                getCampaign().getUnitMarket().getOffers().remove(offer);
                offersIterator.remove();
                continue;
            }

            final Money price = offer.getPrice();
            if (getCampaign().getFunds().isLessThan(price)) {
                getCampaign().addReport(String.format("<font color='" +
                                                            ReportingUtilities.getNegativeColor() +
                                                            "'>" +
                                                            resources.getString("UnitMarketPane.CannotAfford.report") +
                                                            "</font>", entity.getShortName()));
                offersIterator.remove();
                continue;
            }

            final int roll = Compute.d6();
            if (offer.getMarketType().isBlackMarket() && (roll < 3)) {
                getCampaign().getFinances()
                      .debit(TransactionType.UNIT_PURCHASE,
                            getCampaign().getLocalDate(),
                            price.dividedBy(roll),
                            String.format(resources.getString("UnitMarketPane.PurchasedUnitBlackMarketSwindled.finances"),
                                  entity.getShortName()));
                getCampaign().addReport("<font color='" +
                                              ReportingUtilities.getNegativeColor() +
                                              "'>" +
                                              resources.getString("UnitMarketPane.BlackMarketSwindled.report") +
                                              "</font>");
                getCampaign().getUnitMarket().getOffers().remove(offer);
                offersIterator.remove();
                continue;
            }

            getCampaign().getFinances()
                  .debit(TransactionType.UNIT_PURCHASE,
                        getCampaign().getLocalDate(),
                        price,
                        String.format(resources.getString("UnitMarketPane.PurchasedUnit.finances"),
                              entity.getShortName()));
        }

        finalizeEntityAcquisition(offers, getCampaign().getCampaignOptions().isInstantUnitMarketDelivery());
    }

    public void addSelectedOffers() {
        final List<UnitMarketOffer> offers = getSelectedOffers();
        if (offers.isEmpty()) {
            return;
        }

        finalizeEntityAcquisition(offers, true);
    }

    /**
     * Finalizes the acquisition of entities from the market.
     *
     * @param offers          the list of UnitMarketOffers to be finalized
     * @param instantDelivery indicates if the delivery should be instantaneous
     */
    private void finalizeEntityAcquisition(final List<UnitMarketOffer> offers, final boolean instantDelivery) {
        for (final UnitMarketOffer offer : offers) {
            getCampaign().addNewUnit(offer.getEntity(),
                  false,
                  instantDelivery ? 0 : offer.getTransitDuration(),
                  UnitMarketType.getQuality(campaign, offer.getMarketType()));

            if (!instantDelivery) {
                getCampaign().addReport("<font color='" +
                                              ReportingUtilities.getPositiveColor() +
                                              "'>" +
                                              String.format(resources.getString(
                                                          "UnitMarketPane.UnitDeliveryLength.report") + "</font>",
                                                    offer.getTransitDuration()));
            }
            getCampaign().getUnitMarket().getOffers().remove(offer);
        }
        getMarketModel().setData(getCampaign().getUnitMarket().getOffers());
    }

    public void removeSelectedOffers() {
        final List<UnitMarketOffer> offers = getSelectedOffers();
        if (offers.isEmpty()) {
            return;
        }
        getCampaign().getUnitMarket().getOffers().removeAll(offers);
        getMarketModel().setData(getCampaign().getUnitMarket().getOffers());
    }
    // endregion Button Actions

    private void updateDisplay() {
        final Entity entity = getSelectedEntity();
        getEntityViewPane().updateDisplayedEntity(entity);
        getEntityImagePanel().updateDisplayedEntity(entity,
              (entity == null) ? new Camouflage() : entity.getCamouflageOrElse(getCampaign().getCamouflage(), false));
    }

    private void filterOffers() {
        getMarketSorter().setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(final Entry<? extends UnitMarketTableModel, ? extends Integer> entry) {
                Optional<UnitMarketOffer> offer = entry.getModel().getOffer(entry.getIdentifier());
                if (offer.isEmpty()) {
                    return false;
                } else if (getChkFilterByPercentageOfCost().isSelected() &&
                                 (offer.get().getPercent() > (Integer) getSpnCostPercentageThreshold().getValue())) {
                    return false;
                }

                switch (offer.get().getUnitType()) {
                    case UnitType.MEK:
                        return getChkShowMeks().isSelected();
                    case UnitType.TANK:
                        return getChkShowVehicles().isSelected();
                    case UnitType.AEROSPACEFIGHTER:
                        return getChkShowAerospace().isSelected();
                    case UnitType.CONV_FIGHTER:
                        return getChkShowConvAero().isSelected();
                    default:
                        return false;
                }
            }
        });
    }
}
