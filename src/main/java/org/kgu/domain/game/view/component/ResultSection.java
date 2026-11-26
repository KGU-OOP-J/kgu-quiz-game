package org.kgu.domain.game.view.component;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;

public final class ResultSection extends JPanel {
    private final JLabel heading;

    public ResultSection(String title) {
        super(new BorderLayout(0, 8));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        heading = new JLabel(title, SwingConstants.CENTER);
        heading.setForeground(ComponentPainter.TEXT);
        add(heading, BorderLayout.NORTH);
    }

    public void resizeTitle(float scale) {
        heading.setFont(UiStyle.FONT.deriveFont(Font.BOLD, Math.max(9f, Math.round(20 * scale))));
    }

    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create();
        UiStyle.prepare(g);
        ComponentPainter.surface(g, 0, 0, getWidth(), getHeight(), 16, GradientTokens.PANEL, ComponentPainter.BORDER);
        g.dispose();
    }
}
