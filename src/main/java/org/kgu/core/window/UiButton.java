package org.kgu.core.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class UiButton extends JButton {
    private final Color color;
    private float pressOffset;
    private final Timer releaseAnimation;

    public UiButton(String text, Color color) {
        super(text); this.color = color;
        setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setRolloverEnabled(true);
        setForeground(GameTheme.PAPER); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        releaseAnimation = new Timer(15, event -> {
            pressOffset = Math.max(0f, pressOffset - 0.75f); repaint();
            if (pressOffset == 0f) ((Timer)event.getSource()).stop();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) {
                if (!isEnabled() || !SwingUtilities.isLeftMouseButton(event)) return;
                releaseAnimation.stop(); pressOffset = 2f; repaint();
            }
            @Override public void mouseReleased(MouseEvent event) { if (pressOffset > 0f) releaseAnimation.start(); }
        });
    }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); g.translate(0, Math.round(pressOffset));
        GameTheme.paintButton(g, this, color); g.dispose();
    }
    @Override public void removeNotify() { releaseAnimation.stop(); pressOffset = 0; super.removeNotify(); }
}
