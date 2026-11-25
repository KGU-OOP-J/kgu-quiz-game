package org.kgu.game.view;

import org.kgu.window.UiStyle;
import org.kgu.window.GameTheme;
import javax.swing.*;
import java.awt.*;

/** Battle-local button styling; keyboard, focus and accessibility remain standard Swing. */
final class BattleButton extends JButton {
    private final boolean answer;
    private final boolean primary;

    BattleButton(String text, boolean answer, boolean primary) {
        super(text);
        setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                if (!BattleButton.this.answer) { super.paint(graphics, component); return; }
                Graphics2D g = (Graphics2D) graphics.create();
                UiStyle.prepare(g);
                g.setColor(getForeground());
                Font font = getFont();
                g.setFont(font);
                int inset = Math.max(14, getWidth() / 27);
                java.util.List<String> lines = GameTheme.wrap(getText(), g.getFontMetrics(), getWidth() - inset * 2);
                while (lines.size() * g.getFontMetrics().getHeight() > getHeight() - 12 && font.getSize2D() > 12) {
                    font = font.deriveFont(font.getSize2D() - 1);
                    g.setFont(font);
                    lines = GameTheme.wrap(getText(), g.getFontMetrics(), getWidth() - inset * 2);
                }
                FontMetrics metrics = g.getFontMetrics();
                int y = (getHeight() - lines.size() * metrics.getHeight()) / 2 + metrics.getAscent();
                for (String line : lines) { g.drawString(line, inset, y); y += metrics.getHeight(); }
                g.dispose();
            }
            @Override protected void paintText(Graphics g, AbstractButton button, Rectangle bounds, String text) {
                g.setColor(button.getForeground());
                g.drawString(text, bounds.x, bounds.y + g.getFontMetrics().getAscent());
            }
        });
        this.answer = answer;
        this.primary = primary;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setMargin(new Insets(8, 20, 8, 20));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        UiStyle.prepare(g);
        boolean selected = Boolean.TRUE.equals(getClientProperty("selected"));
        boolean correct = Boolean.TRUE.equals(getClientProperty("correct"));
        boolean wrong = Boolean.TRUE.equals(getClientProperty("wrong"));
        Color background = answer ? new Color(36, 73, 106) : primary ? new Color(24, 109, 135) : new Color(37, 62, 82);
        Color border = answer ? new Color(87, 137, 174) : new Color(75, 104, 125);
        Color foreground = answer ? new Color(230, 245, 255) : new Color(239, 247, 249);
        if (!isEnabled()) {
            background = answer ? new Color(31, 56, 80) : primary ? new Color(35, 65, 89) : new Color(31, 48, 63);
            foreground = answer || primary ? new Color(163, 193, 214) : new Color(145, 164, 179);
        }
        if (selected && isEnabled()) { background = new Color(41, 106, 143); border = new Color(115, 223, 255); }
        if (correct) { background = new Color(29, 90, 80); border = new Color(53, 146, 103); foreground = new Color(190, 250, 223); }
        if (wrong) { background = new Color(100, 53, 73); border = new Color(186, 91, 96); foreground = new Color(255, 211, 222); }
        if (isEnabled() && getModel().isRollover()) background = background.brighter();
        int offset = getModel().isPressed() ? 2 : 0;
        g.setColor(background);
        g.fillRoundRect(1, 1 + offset, getWidth() - 3, getHeight() - 4, 15, 15);
        g.setColor(border);
        g.setStroke(new BasicStroke(selected || correct || wrong || hasFocus() ? 2f : 1f));
        g.drawRoundRect(1, 1 + offset, getWidth() - 3, getHeight() - 4, 15, 15);
        if (hasFocus()) {
            g.setColor(new Color(51, 153, 186));
            g.drawRoundRect(5, 5, getWidth() - 11, getHeight() - 12, 10, 10);
        }
        g.dispose();
        setForeground(foreground);
        // Paint readable labels ourselves; disabled answers still need to be studied.
        super.paintComponent(graphics);
    }
}
