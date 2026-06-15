/*
 * Copyright (C) 2017-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.gui;

import megamek.client.ui.models.XTableColumnModel;
import megamek.client.ui.preferences.PreferencesNode;
import megamek.client.ui.util.UIUtil;
import megamek.common.event.Subscribe;
import megamek.common.preference.IPreferenceChangeListener;
import megamek.common.ui.EnhancedTabbedPane;
import megamek.common.ui.FastJScrollPane;
import megamek.logging.MMLogger;
import mekhq.MekHQ;
import mekhq.campaign.events.OptionsChangedEvent;
import mekhq.campaign.events.persons.PersonChangedEvent;
import mekhq.campaign.events.persons.PersonLogEvent;
import mekhq.campaign.events.persons.PersonNewEvent;
import mekhq.campaign.events.persons.PersonRemovedEvent;
import mekhq.campaign.events.scenarios.ScenarioResolvedEvent;
import mekhq.campaign.personnel.Person;
import mekhq.gui.baseComponents.JScrollablePanel;
import mekhq.gui.baseComponents.roundedComponents.RoundedLineBorder;
import mekhq.gui.enums.MHQTabType;
import mekhq.gui.enums.PersonnelFilter;
import mekhq.gui.enums.PersonnelTabView;
import mekhq.gui.enums.PersonnelTableModelColumn;
import mekhq.gui.model.PersonnelTableModel;
import mekhq.gui.view.PersonViewPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Tile for filling
 */
class EspionagePersonTile extends JPanel {

    final Person person;

    public EspionagePersonTile(Person person) {
        this.person = person;
    }

}

/**
 * Tab for managing Espionage personnel and events, if enabled
 */
public final class EspionageTab extends CampaignGuiTab {
    private static final MMLogger LOGGER = MMLogger.create(EspionageTab.class);

    public static final int PERSONNEL_VIEW_WIDTH = UIUtil.scaleForGUI(700);

    // New members
    private JPanel jpSOIHeader;
    private JScrollPane jspEspionagePersonnel;
    private JScrollPane jspEspionageEvents;
    private JPanel jpEspionageChartPanel;
    private JPanel jpDetailsPanel;

    // keep
    private JTable jtEspionagePersonnel;
    private PersonnelTableModel personModel;
    private TableRowSorter<PersonnelTableModel> personnelSorter;

    private final IPreferenceChangeListener scalingChangeListener = e -> changePersonnelView();
    private final PersonnelTabView personnelTabView = PersonnelTabView.GENERAL;

    // region Constructors
    public EspionageTab(CampaignGUI gui, String name) {
        super(gui, name);
        MekHQ.registerHandler(this);
        setUserPreferences();
    }
    // endregion Constructors

    @Override
    public MHQTabType tabType() {
        return MHQTabType.ESPIONAGE;
    }

    /*
     * (non-Javadoc)
     *
     * @see mekhq.gui.CampaignGuiTab#initTab()
     */
    @Override
    public void initTab() {
        final ResourceBundle resourceMap = ResourceBundle.getBundle("mekhq.resources.CampaignGUI",
              MekHQ.getMHQOptions().getLocale());

        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        // Header panel
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.anchor = GridBagConstraints.NORTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jpSOIHeader = new JPanel(new GridBagLayout());
        // TODO: fill with content and make better
        jpSOIHeader.add(new JLabel(resourceMap.getString("espionageTab.personnelHeader")));
        jpSOIHeader.setPreferredSize(new Dimension(700, 20));
        jpSOIHeader.setBorder(RoundedLineBorder.createRoundedLineBorder());
        add(jpSOIHeader, gridBagConstraints);

        // Personnel selection panel
        personModel = new PersonnelTableModel(getCampaign());
        personModel.refreshData();
        jtEspionagePersonnel = new JTable(personModel);
        jtEspionagePersonnel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        jtEspionagePersonnel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        XTableColumnModel personColumnModel = new XTableColumnModel();
        jtEspionagePersonnel.setColumnModel(personColumnModel);
        jtEspionagePersonnel.createDefaultColumnsFromModel();
        personnelSorter = new TableRowSorter<>(personModel);
        final ArrayList<RowSorter.SortKey> sortKeys = new ArrayList<>();
        for (final PersonnelTableModelColumn column : PersonnelTableModel.PERSONNEL_COLUMNS) {
            final Comparator<?> comparator = column.getComparator(getCampaign());
            personnelSorter.setComparator(column.ordinal(), comparator);
            final SortOrder sortOrder = column.getDefaultSortOrder();
            if (sortOrder != null) {
                sortKeys.add(new RowSorter.SortKey(column.ordinal(), sortOrder));
            }
        }
        personnelSorter.setSortKeys(sortKeys);
        jtEspionagePersonnel.setRowSorter(personnelSorter);
        jtEspionagePersonnel.setIntercellSpacing(new Dimension(0, 0));
        jtEspionagePersonnel.setShowGrid(false);
        changePersonnelView();
        jtEspionagePersonnel.getSelectionModel().addListSelectionListener(ev -> refreshPersonnelView());

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 0, 0);
        jspEspionagePersonnel = new FastJScrollPane(jtEspionagePersonnel);
        jspEspionagePersonnel.setLayout(new ScrollPaneLayout());
        jspEspionagePersonnel.setBorder(RoundedLineBorder.createRoundedLineBorder());
        jspEspionagePersonnel.setPreferredSize(new Dimension(360, 700));
        add(jspEspionagePersonnel, gridBagConstraints);

        // Radar Chart setup
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.45;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.insets = new Insets(0, 0, 0, 0);
        jpEspionageChartPanel = new JPanel(new GridBagLayout());
        jpEspionageChartPanel.setBorder(RoundedLineBorder.createRoundedLineBorder());
        jpEspionageChartPanel.setPreferredSize(new Dimension(360, 320));
        add(jpEspionageChartPanel, gridBagConstraints);

        // Event details panel
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new Insets(5, 0, 0, 5);
        jpDetailsPanel = new JPanel(new GridBagLayout());
        jpDetailsPanel.setBorder(RoundedLineBorder.createRoundedLineBorder());
        jpDetailsPanel.setPreferredSize(new Dimension(360, 700));
        add(jpDetailsPanel, gridBagConstraints);

        // Events list
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.45;
        gridBagConstraints.anchor = GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        jspEspionageEvents = new JScrollPane();
        jspEspionageEvents.setLayout(new ScrollPaneLayout());
        jspEspionageEvents.setBorder(RoundedLineBorder.createRoundedLineBorder());
        jspEspionageEvents.setPreferredSize(new Dimension(360, 320));
        add(jspEspionageEvents, gridBagConstraints);

        // Set up listener to refresh on tab selection
        getCampaignGui().getTabMain().addChangeListener(this::stateChanged);
    }

    public void stateChanged( ChangeEvent e ) {
        if (e.getSource() instanceof EnhancedTabbedPane tabsPane) {
            if ( tabsPane.getSelectedComponent() == this ) {
                refreshPersonnelView();
            }
        }
    }

    private DefaultComboBoxModel<PersonnelFilter> createPersonGroupModel() {
        final DefaultComboBoxModel<PersonnelFilter> personGroupModel = new DefaultComboBoxModel<>();
        for (PersonnelFilter filter : MekHQ.getMHQOptions().getPersonnelFilterStyle().getFilters(false)) {
            personGroupModel.addElement(filter);
        }
        return personGroupModel;
    }

    /**
     * These need to be migrated to the Suite Constants / Suite Options Setup
     */
    private void setUserPreferences() {
        try {
            PreferencesNode preferences = MekHQ.getMHQPreferences().forClass(EspionageTab.class);
        } catch (Exception ex) {
            LOGGER.error("Failed to set user preferences", ex);
        }
    }

    /* For export */
    public JTable getPersonnelTable() {
        return jtEspionagePersonnel;
    }

    public PersonnelTableModel getPersonModel() {
        return personModel;
    }

    /*
     * (non-Javadoc)
     *
     * @see mekhq.gui.CampaignGuiTab#refreshAll()
     */
    @Override
    public void refreshAll() {
        // Initial refresh
        refreshPersonnelList();
        refreshPersonnelView();
    }


    @Override
    public void activateTab() {
        super.activateTab();
        refreshPersonnelView();
    }
    /**
     * Refreshes personnel table model.
     */
    public void refreshPersonnelList() {
        UUID selectedUUID = null;

        personModel.refreshData();
    }

    public void refreshPersonnelView() {
    }

    public List<Person> getSelectedPersons() {
        int[] selectedRows = jtEspionagePersonnel.getSelectedRows();
        List<Person> selectedPersons = new ArrayList<>();
        for (int viewRow : selectedRows) {
            int modelRow = jtEspionagePersonnel.convertRowIndexToModel(viewRow);
            Person person = personModel.getPerson(modelRow);
            if (person != null) {
                selectedPersons.add(person);
            }
        }
        return selectedPersons;
    }

    private final ActionScheduler personnelListScheduler = new ActionScheduler(this::refreshPersonnelList);

    @Subscribe
    public void handle(OptionsChangedEvent ev) {
        changePersonnelView();
        personnelListScheduler.schedule();
    }

    @Subscribe
    public void handle(PersonChangedEvent ev) {
        personnelListScheduler.schedule();
    }

    @Subscribe
    public void handle(PersonNewEvent ev) {
        personnelListScheduler.schedule();
    }

    @Subscribe
    public void handle(PersonRemovedEvent ev) {
        personnelListScheduler.schedule();
    }

    @Subscribe
    public void handle(PersonLogEvent ev) {
        refreshPersonnelView();
    }

    @Subscribe
    public void handle(ScenarioResolvedEvent ev) {
        personnelListScheduler.schedule();
    }

    private void changePersonnelView() {
        final PersonnelTabView view = PersonnelTabView.GENERAL;
        final XTableColumnModel columnModel = (XTableColumnModel) getPersonnelTable().getColumnModel();
        getPersonnelTable().setRowHeight(UIUtil.scaleForGUI(15));

        // set the renderer
        for (final PersonnelTableModelColumn column : PersonnelTableModel.PERSONNEL_COLUMNS) {
            final TableColumn tableColumn = columnModel.getColumnByModelIndex(column.ordinal());
            tableColumn.setCellRenderer(getPersonModel().getRenderer(personnelTabView));
            tableColumn.setPreferredWidth(column.getWidth());
            columnModel.setColumnVisible(tableColumn, column.isVisible(getCampaign(), view, getPersonnelTable()));
        }
    }

}
