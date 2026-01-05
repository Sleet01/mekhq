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

import megamek.Version;
import megamek.common.annotations.Nullable;
import megamek.logging.MMLogger;
import mekhq.campaign.Campaign;
import mekhq.utilities.MHQXMLUtility;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.text.ParseException;

/**
 * Class that wraps ISerializableSupplier&lt;Boolean&gt; with a user-parseable reason why it failed.
 */
public class IntelEventPrerequisite {
    protected static final MMLogger LOGGER = MMLogger.create(IntelEventPrerequisite.class);

    public static final String UNSPECIFIED_REASON = "unspecified";

    private ISerializableSupplier<Boolean> supplier = null;
    private String requirement;

    public IntelEventPrerequisite() {
        this(UNSPECIFIED_REASON, null);
    }

    public IntelEventPrerequisite(String requirement, ISerializableSupplier<Boolean> supplier) {
        this.requirement = requirement;
        this.supplier = supplier;
    }

    public void setSupplier(ISerializableSupplier<Boolean> supplier) {
        this.supplier = supplier;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getRequirement() {
        return requirement;
    }

    public ISerializableSupplier<Boolean> getSupplier() {
        return supplier;
    }

    public boolean get() {
        return supplier.get();
    }

    /**
     * Returns null if supplier is not set; empty string if requirement met; reason if not met.
     * @return String (Nullable) reason why the prereq is not met.
     */
    @Nullable
    public String getReason() {
        if (supplier != null) {
            return (supplier.get()) ? "" : requirement;
        }
        return null;
    }

    public static IntelEventPrerequisite generateInstanceFromXML(Node node, Campaign campaign, Version version) {
        IntelEventPrerequisite retVal = null;
        NamedNodeMap attrs = node.getAttributes();
        Node classNameNode = attrs.getNamedItem("type");
        String className = classNameNode.getTextContent();

        try {
            retVal = (IntelEventPrerequisite) Class.forName(className).getDeclaredConstructor().newInstance();
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
        MHQXMLUtility.writeSimpleXMLOpenTag(pw, indent++, "intelEventPrerequisite", "type", getClass());
        MHQXMLUtility.writeSimpleXMLTag(pw, indent, "requirement", requirement);
        MHQXMLUtility.writeSerialCDATA(pw, indent, "supplier", supplier);
        return indent;
    }

    protected void writeToXMLEnd(final PrintWriter pw, int indent) {
        MHQXMLUtility.writeSimpleXMLCloseTag(pw, --indent, "intelEventPrerequisite");
    }

    public void loadFieldsFromXmlNode(Campaign campaign, Version version, Node node) throws ParseException {
        NodeList childNodes = node.getChildNodes();

        for (int x = 0; x < childNodes.getLength(); x++) {
            Node item = childNodes.item(x);
            try {
                if (item.getNodeName().equalsIgnoreCase("requirement")) {
                    requirement = item.getTextContent();
                } else if (item.getNodeName().equalsIgnoreCase("supplier")) {
                    supplier = (ISerializableSupplier<Boolean>) MHQXMLUtility.parseSerialCDATA(item.getTextContent());
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }
}
