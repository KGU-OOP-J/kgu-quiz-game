package org.kgu.domain.adventure.view.component;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

public final class VillageTile extends JPanel {
    private final Optional<BufferedImage> art;
    private final boolean locked, selected;
    public VillageTile(Optional<BufferedImage> art, boolean locked, boolean selected) { this.art = art; this.locked = locked; this.selected = selected; setOpaque(false); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create();
        UiStyle.prepare(g);
        int width = getWidth(), height = getHeight();
        ComponentPainter.surface(g, 0, 0, width, height, 16,
            selected ? GradientTokens.SELECTED : GradientTokens.PANEL,
            selected ? ComponentPainter.SELECTED : ComponentPainter.BORDER);
        int size = Math.min(64, Math.max(20, (int)(height * .39)));
        int x = width - size - 20;
        int y = Math.min(height - size - 8, (int)(height * .74) - size / 2);
        g.setColor(GameTheme.PAGE);
        g.fillOval(x - 4, y - 4, size + 8, size + 8);
        art.ifPresent(image -> g.drawImage(image, x, y, size, size, null));
        if (locked) {
            g.setColor(GameTheme.BLUE_TEXT);
            g.setStroke(new BasicStroke(2));
            int lockX = x + size - 14, lockY = y + size - 14;
            g.drawArc(lockX + 3, lockY - 7, 9, 12, 0, 180);
            g.fillRoundRect(lockX, lockY, 15, 12, 4, 4);
        }
        if (selected) ComponentPainter.selectionBadge(g, width);
        g.dispose();
    }
}
