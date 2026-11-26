package org.kgu.core.window;

import java.awt.*;

public final class UiStyle {
    public static final int WIDTH = 780, HEIGHT = 560;
    public static final Font FONT = koreanFont();
    public static final Color NAVY = new Color(17, 40, 85);

    private UiStyle() {
    }

    private static Font koreanFont() {
        for (String name : new String[]{"Malgun Gothic", "Noto Sans CJK KR", Font.DIALOG}) {
            Font font = new Font(name, Font.PLAIN, 18);
            if (font.canDisplay('한')) return font;
        }
        return new Font(Font.DIALOG, Font.PLAIN, 18);
    }
    public static void prepare(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static void round(Graphics2D graphics, int x, int y, int width, int height, int arc, Color color) {
        graphics.setColor(color);
        graphics.fillRoundRect(x, y, width, height, arc, arc);
    }

    public static void center(Graphics2D graphics, String text, int centerX, int baseline) {
        graphics.drawString(text, centerX - graphics.getFontMetrics().stringWidth(text) / 2, baseline);
    }
}
