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

package mekhq.campaign.espionage.inteltypes;

import megamek.Version;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.ParseException;

/**
 * Base class for storing intelligence Information Levels
 */
public abstract class BasicIntel {
    protected static final MMLogger LOGGER = MMLogger.create(BasicIntel.class);
    public final static int HIGHEST_LEVEL = 12;
    public final static int LOWEST_LEVEL = -12;

    protected int level = 0;
    protected int mod = 0;
    protected boolean locked = false;

    public BasicIntel() {
    }

    public BasicIntel(int level) {
        if (level > HIGHEST_LEVEL || level < LOWEST_LEVEL) {
            throw new IllegalArgumentException("Level must be between -12 and 12, inclusive");
        }
        this.level = level;
    }

    // Copy Constructor
    public BasicIntel(BasicIntel other) {
        this.level = other.level;
        this.mod = other.mod;
        this.locked = other.locked;
    }

    public int getLevel() {
        return level + mod;
    }

    public int getBaseLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level > HIGHEST_LEVEL || level < LOWEST_LEVEL) {
            throw new IllegalArgumentException("Level must be between -12 and 12, inclusive");
        }
        if (!locked) {
            this.level = level;
        }
    }

    public void setMod(int mod) {
        if (mod > HIGHEST_LEVEL || mod < LOWEST_LEVEL) {
            throw new IllegalArgumentException("Mod must be between -12 and 12, inclusive");
        }
        this.mod = mod;
    }

    public int getMod() {
        return mod;
    }

    public void decrementLevel() {
        if (!locked) {
            decreaseLevel(1);
        }
    }

    public void decreaseLevel(int delta) {
        if (!locked) {
            try {
                setLevel(level - delta);
            } catch (IllegalArgumentException ignored) {
                // maybe log?
            }
        }
    }

    public void incrementLevel() {
        if (!locked) {
            increaseLevel(1);
        }
    }

    public void increaseLevel(int delta) {
        if (!locked) {
            try {
                setLevel(level + delta);
            } catch (IllegalArgumentException ignored) {
                // maybe log?
            }
        }
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isLocked() {
        return locked;
    }

    public static BasicIntel generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        BasicIntel retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (BasicIntel) Class.forName(className).getDeclaredConstructor().newInstance();
            retVal.loadFieldsFromXmlNode(campaign, version, node);

        } catch (Exception ex) {
            LOGGER.error("", ex);
        }

        return retVal;
    }

    public void writeToXML(Campaign campaign, final PrintWriter pw, int indent) {
        indent = writeToXMLBegin(campaign, pw, indent);
        writeToXMLEnd(pw, indent);
    }

    protected int writeToXMLBegin(Campaign campaign, final PrintWriter pw, int indent) {
        throw new NotImplementedException();
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        throw new NotImplementedException();
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        // Level is stored as an attribute of the node
        try {
            level = Integer.parseInt(node.getAttributes().getNamedItem("level").getNodeValue());
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("mod")) {
                    mod = Integer.parseInt(item.getTextContent());
                }
                if (item.getNodeName().equalsIgnoreCase("locked")) {
                    locked = Boolean.parseBoolean(item.getTextContent());
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
