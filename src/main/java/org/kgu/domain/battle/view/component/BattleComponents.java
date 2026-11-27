package org.kgu.domain.battle.view.component;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Shared drawing components. These classes contain no game rules or file-save logic. */
final class BattleComponents {
    private static final Color NAVY = new Color(17, 40, 85);
    private static final Font FONT = new Font("Malgun Gothic", Font.PLAIN, 20);

    private BattleComponents() { }

    static JLabel label(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER); label.setForeground(color); return label;
    }
    static JTextArea textArea(Color color) {
        JTextArea text = new JTextArea(); text.setEditable(false); text.setOpaque(false);
        text.setLineWrap(true); text.setWrapStyleWord(true); text.setForeground(color); return text;
    }
    static BufferedImage backgroundImage() {
        try (var stream = BattleComponents.class.getResourceAsStream("/org/kgu/battle/campus_background.png")) {
            return stream == null ? null : ImageIO.read(stream);
        } catch (java.io.IOException ex) { return null; }
    }
    private static void rounded(Graphics graphics, JComponent component, Color top, Color bottom, int arc) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, top, 0, component.getHeight(), bottom));
        g.fillRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, arc, arc);
        g.setColor(new Color(255, 255, 255, 230)); g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, arc, arc); g.dispose();
    }
    static class RoundedPanel extends JPanel {
        private final Color top, bottom;
        private final int arc;
        RoundedPanel(Color top, Color bottom, int arc) { this.top = top; this.bottom = bottom; this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) { rounded(g, this, top, bottom, arc); }
    }
    static final class ActionButton extends JButton {
        private final Color tint;
        ActionButton(String text, Color tint) {
            super(text); this.tint = tint; setOpaque(false); setContentAreaFilled(false); setBorderPainted(false);
            setRolloverEnabled(true);
            setForeground(Color.WHITE); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D paint = (Graphics2D)g.create();
            org.kgu.core.window.GameTheme.paintButton(paint, this, tint);
            paint.dispose();
        }
    }
    static final class ChoiceButton extends JToggleButton {
        ChoiceButton() {
            setOpaque(false); setContentAreaFilled(false); setBorderPainted(false); setForeground(NAVY);
            setRolloverEnabled(true); setHorizontalAlignment(SwingConstants.LEFT);
            putClientProperty("answer", true);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D paint = (Graphics2D)g.create();
            org.kgu.core.window.GameTheme.paintButton(paint, this, getBackground());
            paint.dispose();
        }
    }
    static final class CardPanel extends RoundedPanel {
        private final JLabel name;
        private final JLabel portrait;
        CardPanel(String text, String imagePath, Color edge) {
            super(edge.brighter(), edge.darker(), 28); setLayout(null);
            name = label(text, Color.WHITE); name.setOpaque(true); name.setBackground(new Color(15, 32, 64));
            portrait = new PortraitLabel(imagePath); add(portrait); add(name);
        }
        CardPanel(String text, BufferedImage image, Color edge) {
            super(edge.brighter(), edge.darker(), 28); setLayout(null);
            name = label(text, Color.WHITE); name.setOpaque(true); name.setBackground(new Color(15, 32, 64));
            portrait = new PortraitLabel(image); add(portrait); add(name);
        }
        @Override public void doLayout() {
            int inset = Math.max(6, getWidth() / 35), height = Math.max(36, getHeight() / 7);
            portrait.setBounds(inset, inset, getWidth() - inset * 2, getHeight() - height - inset * 2);
            name.setBounds(inset, getHeight() - height - inset, getWidth() - inset * 2, height);
            name.setFont(FONT.deriveFont(Font.BOLD, Math.max(14f, getWidth() / 12f)));
        }
    }
    private static final class PortraitLabel extends JLabel {
        private BufferedImage portrait;
        PortraitLabel(String path) {
            super("카드 이미지", SwingConstants.CENTER); setForeground(Color.WHITE);
            try { if (path != null && !path.isBlank()) portrait = ImageIO.read(Path.of(path).toFile()); }
            catch (java.io.IOException | java.nio.file.InvalidPathException ex) { portrait = null; }
        }
        PortraitLabel(BufferedImage image) {
            super("카드 이미지", SwingConstants.CENTER); setForeground(Color.WHITE); portrait = image;
        }
        @Override protected void paintComponent(Graphics graphics) {
            if (portrait == null) { super.paintComponent(graphics); return; }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(new Color(16, 37, 75)); g.fillRect(0, 0, getWidth(), getHeight());
            double scale = Math.min(getWidth() / (double) portrait.getWidth(), getHeight() / (double) portrait.getHeight());
            int w = (int) (portrait.getWidth() * scale), h = (int) (portrait.getHeight() * scale);
            g.drawImage(portrait, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null); g.dispose();
        }
    }
}
