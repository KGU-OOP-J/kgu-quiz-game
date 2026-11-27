package org.kgu.domain.menu.view.component;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class MenuCard extends JPanel {
    public enum Symbol { QUIZ, CARDS, MAP }

    private final String title, description;
    private final Symbol icon;
    private final JButton button;
    private float alpha = 1;
    private boolean hover;
    private Timer timer;
    public MenuCard(String title, String description, Symbol icon, JButton button) {
        this.title = title; this.description = description; this.icon = icon; this.button = button; setLayout(null); setOpaque(false); add(button);
        MouseAdapter mouse = new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); } @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); } };
        addMouseListener(mouse); button.addMouseListener(mouse);
    }
    @Override public void doLayout() { button.setBounds(20, getHeight() - 60, Math.max(30, getWidth() - 40), 44); }
    public void appear(int delay) {
        stop(); alpha = 0;
        long start = System.nanoTime() + delay * 1_000_000L;
        timer = new Timer(16, e -> {
            alpha = (float)Math.clamp((System.nanoTime() - start) / 250_000_000.0, 0, 1);
            repaint(); if (alpha >= 1) ((Timer)e.getSource()).stop();
        });
        timer.setInitialDelay(delay); timer.start();
    }
    public void stop() { if (timer != null) timer.stop(); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        ComponentPainter.surface(g, 0, 0, getWidth(), getHeight(), 16, GradientTokens.PANEL,
            hover ? ComponentPainter.SELECTED : ComponentPainter.BORDER); int cx = getWidth() / 2;
        int buttonTop = getHeight() - 60;
        int iconY = Math.min(100, Math.max(31, buttonTop * 3 / 10));
        paintIcon(g, cx, iconY);
        int titleY = Math.min(iconY + 61, buttonTop - 50);
        g.setColor(ComponentPainter.TEXT);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 23f));
        UiStyle.center(g, title, cx, titleY);
        g.setColor(ComponentPainter.MUTED);
        g.setFont(UiStyle.FONT.deriveFont(14f));
        String[] lines = description.split("\n");
        for (int i = 0; i < lines.length; i++) UiStyle.center(g, lines[i], cx, titleY + 22 + i * 18);
        g.dispose();
    }

    private void paintIcon(Graphics2D source, int x, int y) {
        Graphics2D g = (Graphics2D)source.create();
        g.translate(x, y);
        g.setColor(GameTheme.PAGE);
        g.fillRoundRect(-32, -32, 64, 64, 20, 20);
        g.setColor(ComponentPainter.SELECTED);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (icon) {
            case QUIZ -> {
                g.drawRoundRect(-13, -16, 26, 32, 6, 6);
                g.drawPolyline(new int[]{-7, -2, 7}, new int[]{0, 5, -5}, 3);
            }
            case CARDS -> {
                g.drawRoundRect(-15, -10, 24, 29, 6, 6);
                g.drawRoundRect(-8, -18, 24, 29, 6, 6);
            }
            case MAP -> {
                g.drawPolyline(new int[]{-17, -6, 6, 17, 17, 6, -6, -17, -17},
                    new int[]{-10, -16, -11, -16, 10, 16, 11, 16, -10}, 9);
                g.drawLine(-6, -16, -6, 11);
                g.drawLine(6, -11, 6, 16);
            }
        }
        g.dispose();
    }
}
