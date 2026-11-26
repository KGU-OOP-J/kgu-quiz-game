package org.kgu.domain.adventure.view.component;

import org.kgu.core.progression.ShopItem;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.UiStyle;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

public final class ShopItemIcon extends JComponent {
    private static final int SIZE = 56;
    private static final Color BACKGROUND = new Color(0xE1F0FF);
    private static final Color OUTLINE = new Color(0x275898);
    private final ShopItem item;

    public ShopItemIcon(ShopItem item) {
        this.item = item;
        setPreferredSize(new Dimension(65, SIZE));
    }

    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create();
        UiStyle.prepare(g);
        double size = Math.min(SIZE, Math.min(getWidth(), getHeight()));
        g.translate((getWidth() - size) / 2, (getHeight() - size) / 2);
        g.scale(size / SIZE, size / SIZE);
        g.setColor(BACKGROUND);
        g.fillRoundRect(0, 0, SIZE, SIZE, 16, 16);
        g.setColor(OUTLINE);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (item) {
            case INSIGHT -> drawLens(g);
            case SHIELD -> drawCharm(g);
            case TONIC -> drawPotion(g);
        }
        g.dispose();
    }

    private static void drawLens(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillOval(12, 11, 23, 23);
        g.setColor(OUTLINE);
        g.drawOval(12, 11, 23, 23);
        g.drawLine(32, 31, 43, 42);
        g.setColor(ComponentPainter.SELECTED);
        g.drawArc(17, 16, 13, 13, 95, 84);
    }

    private static void drawCharm(Graphics2D g) {
        Path2D charm = new Path2D.Double();
        charm.moveTo(20, 13);
        charm.lineTo(36, 13);
        charm.lineTo(41, 19);
        charm.lineTo(41, 44);
        charm.lineTo(15, 44);
        charm.lineTo(15, 19);
        charm.closePath();
        g.setColor(Color.WHITE);
        g.fill(charm);
        g.setColor(OUTLINE);
        g.draw(charm);
        g.drawLine(28, 9, 28, 18);
        g.setColor(ComponentPainter.SELECTED);
        g.drawPolyline(new int[]{21, 26, 35}, new int[]{31, 36, 26}, 3);
    }

    private static void drawPotion(Graphics2D g) {
        Path2D bottle = new Path2D.Double();
        bottle.moveTo(32, 17);
        bottle.lineTo(32, 24);
        bottle.curveTo(45, 29, 43, 44, 34, 44);
        bottle.lineTo(22, 44);
        bottle.curveTo(13, 44, 11, 29, 24, 24);
        bottle.lineTo(24, 17);
        bottle.closePath();
        g.setColor(Color.WHITE);
        g.fill(bottle);
        Graphics2D liquid = (Graphics2D)g.create();
        liquid.clip(bottle);
        liquid.setColor(new Color(0x95D1FA));
        liquid.fillRect(13, 33, 30, 12);
        liquid.dispose();
        g.setColor(OUTLINE);
        g.draw(bottle);
        g.drawRoundRect(21, 12, 14, 5, 3, 3);
    }
}
