/*
 * Copyright (C) 2018-2025 The MegaMek Team. All Rights Reserved.
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
package mekhq.gui;

import mekhq.io.FileType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Optional;

/**
 * GUI/Swing utility methods
 * TODO : Windchild : This class shouldn't exist any longer, and we should just convert everything
 * TODO : to use the nicest looking setup for Adoptium Temurin
 */
public class GUI {
    private GUI() {
        // no instances, only static methods on this class
    }

    /**
     * Displays a dialog window from which the user can select a file to open.
     *
     * @return the file selected, if any
     */
    public static Optional<File> fileDialogOpen(JFrame parent, String title, FileType fileType, String directoryPath) {
        return fileDialog(parent, title, fileType, directoryPath, null);
    }

    /**
     * Displays a dialog window from which the user can select a file to save to.
     *
     * @return the file selected, if any
     */
    public static Optional<File> fileDialogSave(JFrame parent, String title, FileType fileType, String directoryPath, String saveFilename) {
        return fileDialog(parent, title, fileType, directoryPath, saveFilename);
    }

    private static Optional<File> fileDialog(JFrame parent, String title, FileType fileType, String directoryPath, String saveFilename ) {
        return awtFileDialog(parent, title, fileType, directoryPath, saveFilename);
    }

    private static Optional<File> awtFileDialog(JFrame parent, String title, FileType fileType, String directoryPath, String saveFilename) {
        FileDialog fd = new FileDialog(parent, title);
        fd.setDirectory(directoryPath);
        if (saveFilename != null) {
            fd.setMode(FileDialog.SAVE);
            fd.setFile(saveFilename);
        } else {
            fd.setMode(FileDialog.LOAD);
        }
        fd.setFilenameFilter((dir, file) -> fileType.getNameFilter().test(file));
        fd.setVisible(true);
        String f = fd.getFile();
        String d = fd.getDirectory();
        if ((f != null) && (d != null)) {
            return Optional.of(new File(d, f));
        } else {
            return Optional.empty();
        }
    }

    private static Optional<File> swingFileDialog(JFrame parent, String title, FileType fileType, String directoryPath, String saveFilename) {
        JFileChooser fd = new JFileChooser(directoryPath);
        fd.setDialogTitle(title);
        if (saveFilename != null) {
            fd.setSelectedFile(new File(saveFilename));
        }
        fd.addChoosableFileFilter(new FileNameExtensionFilter(fileType.getDescription(), fileType.getExtensions().toArray(new String[0])));
        int buttonClicked = saveFilename != null
                          ? fd.showSaveDialog(parent)
                          : fd.showOpenDialog(parent);
        return buttonClicked == JFileChooser.APPROVE_OPTION
             ? Optional.ofNullable(fd.getSelectedFile())
             : Optional.empty();
    }
}
