/*
 * Copyright (C) 2019-2025 The MegaMek Team. All Rights Reserved.
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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import megamek.common.Dropship;
import megamek.common.Jumpship;
import megamek.common.util.ImageUtil;
import megamek.logging.MMLogger;
import mekhq.MHQConstants;
import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.JumpPath;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.PlanetarySystem;
import mekhq.campaign.universe.StarUtil;

/**
 * This panel displays a particular star system with suns and planets and
 * information about
 * the player's unit's position if in the system
 *
 * @author Taharqa (Aaron Gullickson)
 */
public class PlanetarySystemMapPanel extends JPanel {
    private static final MMLogger logger = MMLogger.create(PlanetarySystemMapPanel.class);
    private JLayeredPane pane;
    private JPanel mapPanel;
    private JButton btnBack;

    private Campaign campaign;
    private CampaignGUI hqview;
    private PlanetarySystem system;
    private int selectedPlanet;

    // get the best DropShip and JumpShip of the unit for display
    private Unit dropship;
    private Unit jumpship;

    // various images to paint
    private BufferedImage imgZenithPoint;
    private BufferedImage imgNadirPoint;
    private BufferedImage imgRechargeStation;
    private BufferedImage imgDefaultDropshipFleet;
    private BufferedImage imgDefaultJumpshipFleet;
    private BufferedImage imgDropshipFleet;
    private BufferedImage imgJumpshipFleet;
    private BufferedImage imgSpace;

    private static int minDiameter = 16;
    private static int maxDiameter = 64;
    private static int maxStarWidth = 178;
    private static int starImgSize = 356;

    public PlanetarySystemMapPanel(Campaign c, CampaignGUI view) {
        this.hqview = view;
        this.campaign = c;
        this.system = campaign.getCurrentSystem();
        selectedPlanet = system.getPrimaryPlanetPosition();

        try {
            imgSpace = ImageIO.read(new File("data/images/universe/space.jpg")); // TODO : Remove inline file path
        } catch (IOException e1) {
            imgSpace = null;
            logger.error("missing default space image");
        }
        try {
            imgZenithPoint = ImageIO.read(new File("data/images/universe/default_zenithpoint.png")); // TODO : Remove
                                                                                                     // inline file path
        } catch (IOException e) {
            imgZenithPoint = null;
            logger.error("missing default zenith point image");
        }

        try {
            imgNadirPoint = ImageIO.read(new File("data/images/universe/default_nadirpoint.png")); // TODO : Remove
                                                                                                   // inline file path
        } catch (IOException e) {
            imgNadirPoint = null;
            logger.error("missing default nadir point image");
        }

        try {
            imgRechargeStation = ImageIO.read(new File("data/images/universe/default_recharge_station.png")); // TODO :
                                                                                                              // Remove
                                                                                                              // inline
                                                                                                              // file
                                                                                                              // path
        } catch (IOException e) {
            imgRechargeStation = null;
            logger.error("missing default recharge station image");
        }

        try {
            imgDefaultDropshipFleet = ImageIO.read(new File("data/images/universe/default_dropship_fleet.png")); // TODO
                                                                                                                 // :
                                                                                                                 // Remove
                                                                                                                 // inline
                                                                                                                 // file
                                                                                                                 // path
        } catch (IOException e) {
            imgDefaultDropshipFleet = null;
            logger.error("missing default DropShip fleet image");
        }

        try {
            imgDefaultJumpshipFleet = ImageIO.read(new File("data/images/universe/default_jumpship_fleet.png")); // TODO
                                                                                                                 // :
                                                                                                                 // Remove
                                                                                                                 // inline
                                                                                                                 // file
                                                                                                                 // path
        } catch (IOException e) {
            imgDefaultJumpshipFleet = null;
            logger.error("missing default JumpShip fleet image");
        }

        pane = new JLayeredPane();

        mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // tile the space image
                if (null != imgSpace) {
                    int tileWidth = imgSpace.getWidth();
                    int tileHeight = imgSpace.getHeight();
                    for (int y = 0; y < getHeight(); y += tileHeight) {
                        for (int x = 0; x < getWidth(); x += tileWidth) {
                            g2.drawImage(imgSpace, x, y, this);
                        }
                    }
                }

                // set up some numbers
                int n = system.getPlanets().size();
                // star size is minimum of half the image or 20% of total width
                int starWidth = Math.min(maxStarWidth, (int) Math.round(0.2 * getWidth()));
                int rectWidth = (getWidth() - starWidth) / n;
                int midpoint = rectWidth / 2;
                int y = getHeight() / 2;
                int x = 0;

                int jumpPointImgWidth = 84;
                int jumpPointImgHeight = 72;
                int rechargeImgSize = 64;
                int shipImgSize = 24;
                int nadirX = 12;
                int nadirY = getHeight() - 60 - jumpPointImgHeight;
                int zenithX = 12;
                int zenithY = 60;

                // where is the JumpShip
                int jumpshipX = zenithX + jumpPointImgWidth + 8;
                int jumpshipY = zenithY + (jumpPointImgHeight / 2) - (shipImgSize / 2);
                if (!campaign.getLocation().isJumpZenith()) {
                    jumpshipX = nadirX + jumpPointImgWidth + 8;
                    jumpshipY = nadirY + (jumpPointImgHeight / 2) - (shipImgSize / 2);
                }

                // choose the font based on sizes
                chooseFont(g2, system, campaign, rectWidth - 6);

                // place the sun first
                Image starIcon = ImageUtil.loadImageFromFile("data/" + StarUtil.getIconImage(system)); // TODO : Remove
                                                                                                       // inline file
                                                                                                       // path
                g2.drawImage(starIcon, starWidth - starImgSize, y - (starImgSize / 2), starImgSize, starImgSize, null);

                // draw nadir and zenith points
                if (null != imgZenithPoint) {
                    g2.drawImage(imgZenithPoint, zenithX, zenithY, jumpPointImgWidth, jumpPointImgHeight, null);
                }
                if (system.isZenithCharge(campaign.getLocalDate()) && (null != imgRechargeStation)) {
                    drawRotatedImage(g2, imgRechargeStation, 90.0, zenithX, zenithY + 12, rechargeImgSize,
                            rechargeImgSize);
                }
                if (null != imgNadirPoint) {
                    g2.drawImage(imgNadirPoint, nadirX, nadirY, jumpPointImgWidth, jumpPointImgHeight, null);
                }
                if (system.isNadirCharge(campaign.getLocalDate()) && (null != imgRechargeStation)) {
                    drawRotatedImage(g2, imgRechargeStation, 90.0, nadirX, nadirY + 12, rechargeImgSize,
                            rechargeImgSize);
                }

                // get the biggest diameter allowed within this space for a planet
                int biggestDiameterPixels = rectWidth - 32;
                if (biggestDiameterPixels < minDiameter) {
                    biggestDiameterPixels = minDiameter;
                } else if (biggestDiameterPixels > maxDiameter) {
                    biggestDiameterPixels = maxDiameter;
                }

                // find the biggest diameter among all planets
                double biggestDiameter = 0;
                for (Planet p : system.getPlanets()) {
                    if (p.getDiameter() > biggestDiameter) {
                        biggestDiameter = p.getDiameter();
                    }
                }

                for (int i = 1; i <= n; i++) {
                    x = (rectWidth * (i - 1)) + starWidth + midpoint;
                    Planet p = system.getPlanet(i);

                    if (null != p) {
                        // diameters need to be scaled relative to largest planet, but linear
                        // scale will make all but gas/ice giants tiny. log scale made sizes too close,
                        // but cubic root scale seems to work pretty well.
                        int diameter = (int) ((biggestDiameterPixels)
                                * (Math.cbrt(p.getDiameter()) / Math.cbrt(biggestDiameter)));
                        if (diameter < minDiameter) {
                            diameter = minDiameter;
                        } else if (diameter > maxDiameter) {
                            diameter = maxDiameter;
                        }
                        int radius = diameter / 2;

                        // check for current location - we assume you are on primary planet for now
                        if (campaign.getLocation().getCurrentSystem().equals(system)
                                && i == system.getPrimaryPlanetPosition()) {
                            updateShipImages();

                            JumpPath jp = campaign.getLocation().getJumpPath();
                            int lineX1 = x;
                            int lineY1 = y - radius;
                            int lineX2 = jumpshipX + shipImgSize;
                            int lineY2 = jumpshipY + shipImgSize;
                            if (!campaign.getLocation().isJumpZenith()) {
                                lineY2 = jumpshipY - shipImgSize;
                            }
                            if (null != jp
                                    && (!campaign.getLocation().isAtJumpPoint() || jp.getLastSystem().equals(system))) {
                                // the unit has a flight plan in this system so draw the line
                                // in transit so draw a path
                                g2.setColor(Color.YELLOW);
                                Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                                        new float[] { 9 }, 0);
                                g2.setStroke(dashed);
                                g2.drawLine(lineX1, lineY1, lineX2, lineY2);
                            }
                            if (campaign.getLocation().isAtJumpPoint()) {
                                // draw a ring around jumpship
                                drawRing(g2, jumpshipX + (shipImgSize / 2), jumpshipY + (shipImgSize / 2),
                                        shipImgSize / 2, Color.ORANGE);
                                if (null != imgJumpshipFleet) {
                                    drawRotatedImage(g2, imgJumpshipFleet, 90, jumpshipX, jumpshipY, shipImgSize,
                                            shipImgSize);
                                }
                            } else if (campaign.getLocation().isOnPlanet()) {
                                drawRing(g2, x, y, radius, Color.ORANGE);
                                if (null != imgDropshipFleet) {
                                    g2.drawImage(imgDropshipFleet, x - radius - shipImgSize, y - radius - shipImgSize,
                                            shipImgSize, shipImgSize, null);
                                }
                                // draw jumpship too
                                if (null != imgJumpshipFleet) {
                                    drawRotatedImage(g2, imgJumpshipFleet, 90, jumpshipX, jumpshipY, shipImgSize,
                                            shipImgSize);
                                }
                            } else {
                                if (null != imgDropshipFleet) {
                                    int lengthX = lineX1 - lineX2;
                                    int lengthY = lineY1 - lineY2;
                                    double rotationRequired = getFlightRotation(lengthX, lengthY,
                                            null != jp && jp.getLastSystem().equals(system),
                                            campaign.getLocation().isJumpZenith());
                                    int partialX = lineX2
                                            + (int) Math.round(lengthX * campaign.getLocation().getPercentageTransit());
                                    int partialY = lineY2
                                            + (int) Math.round(lengthY * campaign.getLocation().getPercentageTransit());
                                    drawRing(g2, partialX, partialY, shipImgSize / 2, Color.ORANGE);
                                    drawRotatedImage(g2, imgDropshipFleet, rotationRequired,
                                            partialX - (shipImgSize / 2), partialY - (shipImgSize / 2), shipImgSize,
                                            shipImgSize);
                                }
                                // draw jumpship too
                                if (null != imgJumpshipFleet) {
                                    drawRotatedImage(g2, imgJumpshipFleet, 90, jumpshipX, jumpshipY, shipImgSize,
                                            shipImgSize);
                                }
                            }
                        }

                        // add ring for selected planet
                        if (selectedPlanet == i) {
                            drawRing(g2, x, y, radius, Color.WHITE);
                        }

                        // draw the planet icon
                        Image planetIcon = ImageUtil.loadImageFromFile("data/" + StarUtil.getIconImage(p)); // TODO :
                                                                                                            // Remove
                                                                                                            // inline
                                                                                                            // file path
                        g2.drawImage(planetIcon, x - radius, y - radius, diameter, diameter, null);
                        final String planetName = p.getPrintableName(campaign.getLocalDate());

                        // planet name
                        g2.setColor(Color.WHITE);
                        drawCenteredString(g2, planetName, x,
                                y + (biggestDiameterPixels / 2) + 12 + g.getFontMetrics().getHeight(), rectWidth - 6);
                    }
                }
            }
        };

        btnBack = new JButton();
        btnBack.setFocusable(false);
        btnBack.setPreferredSize(new Dimension(36, 36));
        btnBack.setMargin(new Insets(0, 0, 0, 0));
        btnBack.setBorder(BorderFactory.createEmptyBorder());
        btnBack.setToolTipText("Back to Interstellar Map");
        btnBack.setBackground(Color.DARK_GRAY);
        btnBack.setIcon(new ImageIcon("data/images/misc/back_button.png")); // TODO : Remove inline file path
        btnBack.addActionListener(ev -> back());

        // set up key bindings
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                back();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "left");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "left");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, 0), "left");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "left");
        getActionMap().put("left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int target_pos = selectedPlanet - 1;
                if (target_pos < 1) {
                    return;
                }
                changeSelectedPlanet(target_pos);
                repaint();
            }
        });

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "right");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "right");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, 0), "right");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "right");
        getActionMap().put("right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int target_pos = selectedPlanet + 1;
                if (target_pos > (system.getPlanets().size())) {
                    return;
                }
                changeSelectedPlanet(target_pos);
                repaint();
            }
        });

        mapPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int target_pos = nearestNeighbour(e.getX(), e.getY());
                    if (target_pos < 1) {
                        return;
                    }
                    changeSelectedPlanet(target_pos);
                    repaint();
                }
            }
        });

        pane.add(mapPanel, Integer.valueOf(1));
        pane.add(btnBack, Integer.valueOf(10));

        add(pane);

        repaint();

    }

    @Override
    protected void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        pane.setBounds(0, 0, width, height);
        mapPanel.setBounds(0, 0, width, height);
        btnBack.setBounds(4, 4, 40, 40);
        super.paintComponent(g);
    }

    /**
     * Choose a font size based on how large it needs to be to fit within the space
     *
     * @param g      - a <code>Graphics</code> device
     * @param system - a <code>PlanetarySystem</code>
     * @param c      - a <code>Campaign</code>
     * @param limit  - an integer indicating how large the text can be
     */
    private void chooseFont(Graphics g, PlanetarySystem system, Campaign c, int limit) {
        // start with 16
        int fontSize = 16;
        g.setFont(new Font(MHQConstants.FONT_HELVETICA, Font.PLAIN, fontSize));
        while (areNamesTooBig(g, system, campaign.getLocalDate(), limit) && fontSize >= 10) {
            fontSize--;
            g.setFont(new Font(MHQConstants.FONT_HELVETICA, Font.PLAIN, fontSize));
        }
    }

    /**
     * Determine if planet names are too big for the display at current font size
     *
     * @param g      - a <code>Graphics</code> device
     * @param system - a <code>PlanetarySystem</code>
     * @param when   - <code>DateTime</code> object
     * @param limit  - an integer indicating how large the text can be
     * @return
     */
    private boolean areNamesTooBig(Graphics g, PlanetarySystem system, LocalDate when, int limit) {
        for (Planet p : system.getPlanets()) {
            String name = p.getName(when);
            if (g.getFontMetrics().stringWidth(name) > limit) {
                // try splitting
                if (name.contains(" ")) {
                    for (String line : name.split("\\s+")) {
                        if (g.getFontMetrics().stringWidth(line) > limit) {
                            return true;
                        }
                    }
                } else if (name.contains("-")) {
                    for (String line : name.split("-")) {
                        if (g.getFontMetrics().stringWidth(line) > limit) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Draw a string that is centered on x and y. It will wrap text by spaces of
     * hyphens
     * if too large.
     *
     * @param g     - a <code>Graphics2D</code> device to draw on
     * @param text  - a <code>String</code> of text to be drawn
     * @param x     - an <code>int</code> for the x-axis position
     * @param y     - an <code>int</code> for the y-axis position
     * @param limit - an <code>int</coce> for how big the text can be
     */
    private void drawCenteredString(Graphics2D g, String text, int x, int y, int limit) {
        FontMetrics metrics = g.getFontMetrics();
        y = y - (metrics.getHeight() / 2);
        if (metrics.stringWidth(text) > limit && (text.contains(" ") || text.contains("-"))) {
            if (text.contains(" ")) {
                // try spaces first
                for (String line : text.split("\\s+")) {
                    g.drawString(line, x - (metrics.stringWidth(line) / 2), y += g.getFontMetrics().getHeight());
                }
            } else {
                // otherwise break on the hyphen
                String[] lines = text.split("-");
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i];
                    if (i != (lines.length - 1)) {
                        line = line + "-";
                    }
                    g.drawString(line, x - (metrics.stringWidth(line) / 2), y += g.getFontMetrics().getHeight());
                }
            }
        } else {
            g.drawString(text, x - (metrics.stringWidth(text) / 2), y + g.getFontMetrics().getHeight());
        }
    }

    /**
     * Update and repaint the panel for a new planetary system.
     *
     * @param s - a {@link PlanetarySystem} to paint
     */
    public void updatePlanetarySystem(PlanetarySystem s) {
        if (null == s) {
            return;
        }
        this.system = s;
        selectedPlanet = system.getPrimaryPlanetPosition();
        mapPanel.repaint();
    }

    /**
     * Update and repaint the panel for a new {@link Planet}
     *
     * @param p - a {@link Planet} to paint.
     */
    public void updatePlanetarySystem(Planet p) {
        if (null == p) {
            return;
        }
        this.system = p.getParentSystem();
        selectedPlanet = p.getSystemPosition();
        mapPanel.repaint();
    }

    /**
     *
     * @return the currently selected planet
     */
    public int getSelectedPlanetPosition() {
        return selectedPlanet;
    }

    /**
     * Change the selected planets and notify listeners so view is changed
     *
     * @param pos - an <code>int</code> giving the position of the newly selected
     *            planet
     */
    private void changeSelectedPlanet(int pos) {
        selectedPlanet = pos;
        notifyListeners();
    }

    /**
     * Find any planet in the display that contains the x and y parameters. Used to
     * identify if planets
     * were selected by the cursor.
     *
     * @param x - the x-value of the selection
     * @param y - the y-value of the selection
     * @return - a planet position
     */
    private int nearestNeighbour(double x, double y) {
        int n = system.getPlanets().size();
        int starWidth = Math.min(maxStarWidth, (int) Math.round(0.2 * getWidth()));
        int rectWidth = (getWidth() - starWidth) / n;
        int midpoint = rectWidth / 2;
        int xTarget;
        int yTarget = getHeight() / 2;

        for (int i = 1; i <= n; i++) {
            xTarget = starWidth + rectWidth * (i - 1) + midpoint;
            // must be within total possible radius
            int radius = maxDiameter / 2;
            if (x <= (xTarget + radius) & x >= (xTarget - radius) &
                    y <= (yTarget + radius) & y >= (yTarget - radius)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Draw and image with the given rotation
     *
     * @param g       - the <code>Graphics2D</code> device to draw on
     * @param image   - a <code>BufferedImage</code> to be drawn
     * @param degrees - A <code>double</code> giving the rotation to apply
     * @param x       - the x position for placement
     * @param y       - the y position for placement
     * @param width   - the width to draw the image to
     * @param height  - the height to draw the image to
     */
    private void drawRotatedImage(Graphics2D g, BufferedImage image, double degrees, int x, int y, int width,
            int height) {
        double rotationRequired = Math.toRadians(degrees);
        double locationX = image.getWidth() / 2;
        double locationY = image.getHeight() / 2;
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
        // Drawing the rotated image at the required drawing locations
        g.drawImage(op.filter(image, null), x, y, width, height, null);
    }

    /**
     * Draw a selection ring of the given color around some point. This will draw
     * over everything in the ring with a black background, so
     * should be placed before anything selected.
     *
     * @param g      - the <code>Graphics2D</code> device to draw on
     * @param x      - an <code>int</code> giving the x-position of the center of
     *               the ring
     * @param y      - an <code>int</code> giving the y-position of the center of
     *               the ring
     * @param radius - an <code>int</code> giving the radius of the ring
     * @param c      - the <code>Color</code> of the ring
     */
    private void drawRing(Graphics2D g, int x, int y, int radius, Color c) {
        Arc2D.Double arc = new Arc2D.Double();
        g.setPaint(c);
        arc.setArcByCenter(x, y, radius + 6, 0, 360, Arc2D.OPEN);
        g.fill(arc);
        g.setPaint(Color.BLACK);
        arc.setArcByCenter(x, y, radius + 4, 0, 360, Arc2D.OPEN);
        g.fill(arc);
        g.setPaint(c);
        arc.setArcByCenter(x, y, radius + 3, 0, 360, Arc2D.OPEN);
        g.fill(arc);
        g.setPaint(Color.BLACK);
        arc.setArcByCenter(x, y, radius + 2, 0, 360, Arc2D.OPEN);
        g.fill(arc);
    }

    /**
     * Determine the degree of rotation for a ship in flight to to the planet or
     * jump point
     *
     * @param lengthX    - An <code>int</code> giving the length of the x-axis by
     *                   the triangle formed between the planet and the jump point
     * @param lengthY    - An <code>int</code> giving the length of the y-axis by
     *                   the triangle formed between the planet and the jump point
     * @param inbound    - A <code>boolean</code> for whether the flight is inbound.
     * @param zenithJump - A <code>boolean</code> for whether the the zenith point
     *                   is the jump point (false if nadir).
     * @return a <code>double</code> giving the proper rotation.
     */
    private double getFlightRotation(int lengthX, int lengthY, boolean inbound, boolean zenithJump) {
        double rotation = Math.toDegrees(Math.atan(lengthX / ((1.0 * lengthY))));
        // rotation depends on inbound or outbound
        if ((zenithJump && !inbound) || (!zenithJump && inbound)) {
            return 0 - rotation;
        } else {
            return 180 - rotation;
        }
    }

    /**
     * Get the best DropShip among the player's units. For choosing which one to
     * display on screen for transit. This is
     * determined by weight.
     *
     * @return a <code>Unit</code> for the best DropShip.
     */
    private Unit getBestDropship() {
        Unit bestUnit = null;
        double bestWeight = 0.0;
        for (Unit u : campaign.getHangar().getUnits()) {
            if ((u.getEntity() instanceof Dropship) && (u.getEntity().getWeight() > bestWeight)) {
                bestUnit = u;
                bestWeight = u.getEntity().getWeight();
            }
        }
        return bestUnit;
    }

    /**
     * Get the best JumpShip among the player's units. For choosing which one to
     * display on screen
     * for transit, this is determined by weight.
     *
     * @return a <code>Unit</code> for the best JumpShip.
     */
    private Unit getBestJumpship() {
        Unit bestUnit = null;
        double bestWeight = 0.0;
        for (Unit u : campaign.getHangar().getUnits()) {
            if (u.getEntity() instanceof Jumpship && u.getEntity().getWeight() > bestWeight) {
                bestUnit = u;
                bestWeight = u.getEntity().getWeight();
            }
        }
        return bestUnit;
    }

    /**
     * Update the DropShip and JumpShip fleet images by the player's campaign's best
     * DropShip and
     * JumpShip, respectively. If the campaign has none, then it returns the
     * default.
     */
    public void updateShipImages() {
        dropship = getBestDropship();
        imgDropshipFleet = imgDefaultDropshipFleet;
        if (null != dropship) {
            imgDropshipFleet = Utilities.toBufferedImage(dropship.getImage(this));
        }

        jumpship = getBestJumpship();
        imgJumpshipFleet = imgDefaultJumpshipFleet;
        if (null != jumpship) {
            imgJumpshipFleet = Utilities.toBufferedImage(jumpship.getImage(this));
        }
    }

    private transient List<ActionListener> listeners = new ArrayList<>();

    public void addActionListener(ActionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeActionListener(ActionListener l) {
        listeners.remove(l);
    }

    private void notifyListeners() {
        ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_FIRST, "refresh");
        listeners.forEach(l -> l.actionPerformed(ev));
    }

    /**
     * Switch back to the interstellar map
     */
    private void back() {
        hqview.getMapTab().switchSystemsMap();
    }
}
