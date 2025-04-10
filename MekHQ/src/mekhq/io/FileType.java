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
package mekhq.io;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Enumeration holding information about the file types that are most relevant for MekHQ
 */
 public enum FileType {

    /**
     * Value for personnel files.
     */
    PRSX("Personnel file", "prsx"),

    /**
     * Value for parts files.
     */
    PARTS("Parts file", "parts"),

    /**
     * Value for json files.
     */
    JSON("Json file", "json"),

    /**
     * Value for csv files.
     */
    CSV("CSV file", "csv"),

    /**
     * Value for tsv files.
     */
    TSV("TSV file", "tsv"),

    /**
     * Value for xml files.
     */
    XML("XML file", "xml"),

    /**
     * Value for png files.
     */
    PNG("PNG file", "png"),

    /**
     * Value for mul files.
     */
    MUL("MUL file", "mul"),

    /**
     * Value for campaign files.
     */
    CPNX("Campaign file", "cpnx", "cpnx.gz", "xml");

    private FileType(String description, String... extensions) {
        this.description = description;
        this.extensions = Arrays.asList(extensions);
    }

    private final String description;
    private final List<String> extensions;

    /**
     * @return the description of this file type
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return what extensions the files of type usually have
     */
    public List<String> getExtensions() {
        return extensions;
    }

    /**
     * @return the recommended extension for files of this type
     */
    public String getRecommendedExtension() {
        return extensions.get(0);
    }

    /**
     * @return a matcher to filter files of this type based on the file name
     */
    public Predicate<String> getNameFilter() {
        return fileName -> {
            int lastDotIdx = fileName.lastIndexOf('.');
            if (lastDotIdx < 0) {
                return true;
            } else {
                int len = fileName.length();
                for (String extension : extensions) {
                    // if the extension would be longer than the file
                    // or the entire file name, skip it...
                    if (extension.length()+1 >= fileName.length()) {
                        continue;
                    }
                    // ...otherwise, check that the file name ends with the
                    // extension preceded by a period.
                    if ((fileName.charAt(len - (extension.length()+1)) == '.')
                        && fileName.regionMatches(true, len - extension.length(),
                            extension, 0, extension.length())) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
