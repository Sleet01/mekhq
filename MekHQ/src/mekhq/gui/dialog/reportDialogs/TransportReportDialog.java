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
 */
package mekhq.gui.dialog.reportDialogs;

import mekhq.MHQConstants;
import mekhq.campaign.report.TransportReport;

import javax.swing.*;
import java.awt.*;

public class TransportReportDialog extends AbstractReportDialog {
    //region Variable Declarations
    private final TransportReport transportReport;
    //endregion Variable Declarations

    //region Constructors
    public TransportReportDialog(final JFrame frame, final TransportReport transportReport) {
        super(frame, "TransportReportDialog", "TransportReportDialog.title");
        this.transportReport = transportReport;
        initialize();
    }
    //endregion Constructors

    //region Getters
    public TransportReport getTransportReport() {
        return transportReport;
    }

    @Override
    protected JTextPane createTxtReport() {
        final JTextPane txtReport = new JTextPane();
        txtReport.setText(getTransportReport().getTransportDetails());
        txtReport.setName("txtReport");
        txtReport.setFont(new Font(MHQConstants.FONT_COURIER_NEW, Font.PLAIN, 12));
        txtReport.setEditable(false);
        txtReport.setCaretPosition(0);
        return txtReport;
    }
    //endregion Getters
}
