package org.kgu.core.window;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class UiButton extends JButton {
    private final Color color;
    private float pressOffset;
    private final Timer releaseAnimation;
    private long releasedAt;
    private long hoverStarted;
    private float hoverAmount = 1;
    private boolean hovered;

    public UiButton(String text, Color color) {
        super(text); this.color = color;
        setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setOpaque(false); setRolloverEnabled(true);
        setForeground(GameTheme.PAPER); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        releaseAnimation = new Timer(15, event -> {
            long now = System.nanoTime();
            if (releasedAt != 0) pressOffset = 2f * (1 - (float)Math.clamp((now - releasedAt) / 80_000_000.0, 0, 1));
            hoverAmount = (float)Math.clamp((now - hoverStarted) / 120_000_000.0, 0, 1);
            repaint();
            if (pressOffset == 0f && hoverAmount == 1f) ((Timer)event.getSource()).stop();
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent event) { hovered = true; hoverStarted = System.nanoTime(); releaseAnimation.start(); if (isEnabled()) GameResources.play(GameResources.Sound.UI_HOVER); }
            @Override public void mouseExited(MouseEvent event) { hovered = false; hoverStarted = System.nanoTime(); releaseAnimation.start(); }

            @Override public void mousePressed(MouseEvent event) {
                if (!isEnabled() || !SwingUtilities.isLeftMouseButton(event)) return;
                releasedAt = 0; releaseAnimation.stop(); pressOffset = 2f; repaint();
            }
            @Override public void mouseReleased(MouseEvent event) { if (pressOffset > 0f) { releasedAt = System.nanoTime(); releaseAnimation.start(); } }
        });
        addActionListener(event -> GameResources.play(GameResources.Sound.UI_CLICK));
    }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create();
        float hover = hovered ? hoverAmount : 1 - hoverAmount;
        GameTheme.paintButton(g, this, color, hover,
            getModel().isPressed() && getModel().isArmed() ? 1f : pressOffset / 2);
        g.dispose();
    }
    @Override public void removeNotify() { releaseAnimation.stop(); pressOffset = 0; super.removeNotify(); }
}
