package org.kgu.domain.game.view.component;

import org.kgu.core.window.UiStyle;
import org.kgu.core.window.GameTheme;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import javax.swing.*;
import java.awt.*;

/** Battle-local button styling; keyboard, focus and accessibility remain standard Swing. */
public final class BattleButton extends JButton {
    private final boolean answer;
    private final boolean primary;

    public BattleButton(String text, boolean answer, boolean primary) {
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
        GradientTokens style = selected ? GradientTokens.SELECTED
            : !isEnabled() && !answer ? GradientTokens.DISABLED
            : primary ? GradientTokens.PRIMARY : GradientTokens.NEUTRAL;
        Color foreground = ComponentPainter.button(g, getWidth(), getHeight(), style, selected, correct, wrong,
            isEnabled() && getModel().isRollover(), isEnabled() && getModel().isPressed(), hasFocus());
        g.dispose();
        setForeground(foreground);
        // Paint readable labels ourselves; disabled answers still need to be studied.
        super.paintComponent(graphics);
    }
}
