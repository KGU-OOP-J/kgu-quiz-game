package org.kgu.game.view;

import javax.swing.*;
import java.awt.*;
import org.kgu.window.UiStyle;

/** Independent of the global textured progress UI, including at compact heights. */
final class BattleProgressBar extends JProgressBar {
    BattleProgressBar(int maximum) {
        super(0, maximum);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        int w = getWidth(), h = getHeight();
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, w, h, h, h);
        int filled = (int) Math.round(w * getPercentComplete());
        if (filled > 0) {
            g.setPaint(new GradientPaint(0, 0, getForeground().darker(), Math.max(1, w), h, getForeground().brighter()));
            g.fillRoundRect(0, 0, filled, h, h, h);
            g.setColor(new Color(230, 250, 255, 150));
            g.fillRoundRect(1, 1, Math.max(0, filled - 2), Math.max(1, h / 4), h, h);
        }
        g.dispose();
    }
}
