package org.kgu.ui.common;

import java.awt.*;
import javax.swing.JButton;

/** 단일색·1px 테두리·1px 글자 윤곽선을 가진 재사용 버튼. */
public final class UiButton extends JButton {
    private final Color color;
    public UiButton(String text, Color color) { super(text); this.color = color; setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g);
        g.setColor(getModel().isPressed() ? color.darker() : color); g.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); g.setColor(color.darker()); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        Font font = getFont(); while (g.getFontMetrics(font).stringWidth(getText()) > getWidth() - 18 && font.getSize2D() > 10) font = font.deriveFont(font.getSize2D() - 1);
        Shape text = font.createGlyphVector(g.getFontRenderContext(), getText()).getOutline((getWidth() - g.getFontMetrics(font).stringWidth(getText())) / 2f, getHeight() / 2f + font.getSize2D() / 3f);
        g.setColor(getForeground()); g.fill(text); g.setColor(new Color(0, 0, 0, 90)); g.setStroke(new BasicStroke(1)); g.draw(text); g.dispose();
    }
}
