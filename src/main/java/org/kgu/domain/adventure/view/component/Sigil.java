package org.kgu.domain.adventure.view.component;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public final class Sigil extends JComponent {
    private final String image;
    private final int type;
    public Sigil(String image, int type) { this.image = image; this.type = type; setPreferredSize(new Dimension(65, 70)); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        Optional<BufferedImage> asset = image == null ? Optional.empty() : GameTheme.asset(image);
        if (asset.isPresent()) GameTheme.cover(g, asset.get(), 0, Math.max(0, (getHeight() - 64) / 2), 64, 64);
        else {
            int cx = getWidth() / 2, cy = getHeight() / 2; g.setColor(GameTheme.PAGE); g.fillOval(cx - 28, cy - 28, 56, 56); g.setColor(ComponentPainter.SELECTED); g.setStroke(new BasicStroke(2));
            if (type == 0) { g.drawOval(cx - 12, cy - 14, 22, 22); g.drawLine(cx + 6, cy + 4, cx + 19, cy + 17); }
            else if (type == 1) { Polygon p = new Polygon(new int[]{cx - 16, cx, cx + 16, cx + 12, cx}, new int[]{cy - 15, cy - 22, cy - 15, cy + 8, cy + 23}, 5); g.drawPolygon(p); }
            else if (type == 2) { g.drawRoundRect(cx - 12, cy - 8, 24, 29, 7, 7); g.drawRect(cx - 6, cy - 18, 12, 10); g.drawLine(cx, cy, cx, cy + 12); g.drawLine(cx - 6, cy + 6, cx + 6, cy + 6); }
            else if (type == 3) {
                g.drawPolyline(new int[]{cx - 12, cx - 4, cx + 13}, new int[]{cy, cy + 8, cy - 10}, 3);
            } else {
                g.setColor(ComponentPainter.MUTED);
                g.drawOval(cx - 13, cy - 13, 26, 26);
                g.drawLine(cx, cy, cx, cy - 7);
                g.drawLine(cx, cy, cx + 6, cy);
            }
        }
        g.dispose();
    }
}
