/*
 * Copyright (C) 2026 The MegaMek Team. All Rights Reserved.
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
package mekhq.gui.campaignOptions.contents;

import static mekhq.MHQConstants.ESPIONAGE_IMAGE_DIRECTORY;
import static mekhq.gui.campaignOptions.CampaignOptionsUtilities.createTipPanelUpdater;
import static mekhq.gui.campaignOptions.CampaignOptionsUtilities.getImageDirectory;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import megamek.client.ui.settings.SettingsFormPanel;
import megamek.common.autoResolve.acar.Settings;
import mekhq.gui.campaignOptions.components.CampaignOptionsCheckBox;
import mekhq.gui.campaignOptions.components.CampaignOptionsPagePanel;
import mekhq.gui.campaignOptions.components.CampaignOptionsHeaderPanel;


/**
 * The {@code FactionStandingPage} class builds and manages the Espionage leaf page of the Campaign Options
 * dialog. It owns the widgets for Espionage configuration - the tracking toggles, the regard multiplier, and the
 * per-effect modifier toggles - and synchronises them with a shared {@link SystemsOptionsModel}.
 *
 * <p>This view is a sub-component of {@link SystemsPages}: the model snapshot and the overall load/apply lifecycle still
 * live on {@code SystemsPages}, while this class is responsible only for constructing the Espionage panel and
 * copying Espionage values to and from the model. The page is built lazily; until
 * {@link #createPanel(SystemsOptionsModel)} is called, {@link #readFromModel(SystemsOptionsModel)} and
 * {@link #writeToModel(SystemsOptionsModel)} are no-ops.</p>
 */
class EspionagePage {
    private static final int FORM_LABEL_COLUMN_WIDTH = SettingsFormPanel.DEFAULT_LABEL_WIDTH;
    private static final int FORM_CONTROL_COLUMN_WIDTH = SettingsFormPanel.DEFAULT_CONTROL_WIDTH;
    private static final int CHECKBOX_GRID_COLUMNS = 2;

    private JCheckBox chkEnableEspionageSystem;

    private CampaignOptionsHeaderPanel espionageHeader;

    private boolean created;

    /**
     * Creates the Espionage page panel, containing grouped UI elements for Espionage options and its
     * header.
     *
     * @param model the shared systems options model to populate the freshly built controls from
     *
     * @return a {@link JPanel} component representing the entire Espionage page UI
     */
    @Nonnull JPanel createPanel(@Nullable SystemsOptionsModel model) {
        // Header
        String imageAddress = ESPIONAGE_IMAGE_DIRECTORY + "logo_espionage_symbol.png";
        espionageHeader = new CampaignOptionsHeaderPanel("EspionageTab", imageAddress);

        // Contents
        JPanel pnlEspionageGeneralOptions = createEspionageGeneralPanel();

        // Layout the Panel
        // final JPanel panel = new CampaignOptionsStandardPanel("EspionageTab", true);
        final JPanel panel = CampaignOptionsPagePanel.builder("EspionagePage", "EspionagePage",
                    imageAddress)
               .header(espionageHeader)
               .quote("espionagePage")
               .section("lblEspionagePanel.text",
                     "lblEspionagePanel.summary",
                     pnlEspionageGeneralOptions)
               .build();

        created = true;
        readFromModel(model);

        return panel;
    }

    /**
     * Creates and lays out the general espionage options panel
     *
     * @return a {@link JPanel} containing the general espionage controls
     *
     * @author sleet01
     * @since 0.50.12
     */
    private @Nonnull JPanel createEspionageGeneralPanel() {
        chkEnableEspionageSystem = new CampaignOptionsCheckBox("EnableEspionageSystem");
        chkEnableEspionageSystem.addMouseListener(createTipPanelUpdater("EnableEspionageSystem"));

        // Layout the Panel
        final SettingsFormPanel panel = new SettingsFormPanel("EspionageGeneralOptionsPanel",
              FORM_LABEL_COLUMN_WIDTH,
              FORM_CONTROL_COLUMN_WIDTH);
        panel.addCheckBoxGrid(CHECKBOX_GRID_COLUMNS,
              chkEnableEspionageSystem);

        return panel;
    }

    /**
     * Copies Espionage values from the shared model into this page's controls. This is a no-op until the page has
     * been built.
     *
     * @param model the shared systems options model to read values from
     */
    void readFromModel(@Nullable SystemsOptionsModel model) {
        if (!created || model == null) {
            return;
        }

        // chkEnableEspionageSystem.setSelected(options.isUseEspionageSystem());
        chkEnableEspionageSystem.setSelected(model.useEspionageSystem);
    }

    /**
     * Copies Espionage values from this page's controls into the shared model. This is a no-op until the page has
     * been built.
     *
     * @param model the shared systems options model to write values into
     */
    void writeToModel(@Nullable SystemsOptionsModel model) {
        if (!created || model == null) {
            return;
        }

        model.useEspionageSystem = chkEnableEspionageSystem.isSelected();
    }
}
