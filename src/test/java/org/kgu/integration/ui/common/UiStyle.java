package org.kgu.integration.ui.common;

import java.awt.*;

/** 화면 전체에서 공유하는 Java 2D 스타일 도구. */
public final class UiStyle {
    public static final int WIDTH = 780, HEIGHT = 576;
    public static final Font FONT = new Font("Malgun Gothic", Font.PLAIN, 14);
    public static final Color NAVY = new Color(24, 48, 76);
    private UiStyle() { }
    public static void prepare(Graphics2D g) { g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); }
    public static void round(Graphics2D g, int x, int y, int w, int h, int r, Color c) { g.setColor(c); g.fillRoundRect(x, y, w, h, r, r); }
    public static void center(Graphics2D g, String text, int x, int y) { g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y); }
    public static void outlinedText(Graphics2D g, String text, int x, int y, Color fill, Color outline) {
        Shape shape = g.getFont().createGlyphVector(g.getFontRenderContext(), text).getOutline(x, y);
        g.setColor(fill); g.fill(shape); g.setColor(outline);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(shape);
    }
}
