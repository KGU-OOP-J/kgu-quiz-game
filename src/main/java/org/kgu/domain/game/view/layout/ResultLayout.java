package org.kgu.domain.game.view.layout;

import java.awt.Rectangle;

public final class ResultLayout {
    private ResultLayout() { }

    public record Bounds(float scale, Rectangle header, Rectangle hp, Rectangle rewards, Rectangle status,
                         Rectangle retry, Rectangle actions) { }

    public static Bounds measure(int width, int height, boolean showHp, boolean showRetry) {
        float scale = Math.min(1.18f, Math.min(width / 1280f, height / 960f));
        int margin = Math.round(20 * scale);
        int gap = Math.round(12 * scale);
        int headerHeight = Math.round(168 * scale);
        int hpHeight = showHp ? Math.round(90 * scale) : 0;
        int statusHeight = Math.round(48 * scale);
        int buttonHeight = Math.round(62 * scale);
        int retryHeight = showRetry ? Math.round(43 * scale) : 0;
        int innerWidth = Math.min(width - 2 * margin, Math.round(930 * scale));
        int buttonWidth = Math.min(innerWidth, Math.round(1050 * scale));

        int headerWidth = Math.min(width - 2 * margin, Math.round(1140 * scale));
        int hpTop = margin + headerHeight + gap;
        int hpWidth = Math.min(innerWidth, Math.round(550 * scale));
        int top = showHp ? hpTop + hpHeight + gap : hpTop;
        int buttonsTop = height - margin - buttonHeight;
        int retryTop = buttonsTop - (showRetry ? retryHeight + gap : 0);
        int statusTop = retryTop - statusHeight;
        int available = Math.max(0, statusTop - gap - top);
        int bodyHeight = Math.min(Math.round(440 * scale), available);
        int bodyTop = top + (available - bodyHeight) / 2;
        return new Bounds(scale, centered(width, margin, headerWidth, headerHeight),
            centered(width, hpTop, hpWidth, hpHeight), centered(width, bodyTop, innerWidth, bodyHeight),
            centered(width, statusTop, headerWidth, statusHeight), centered(width, retryTop, buttonWidth, retryHeight),
            centered(width, buttonsTop, buttonWidth, buttonHeight));
    }

    private static Rectangle centered(int totalWidth, int y, int width, int height) {
        return new Rectangle((totalWidth - width) / 2, y, width, height);
    }
}
