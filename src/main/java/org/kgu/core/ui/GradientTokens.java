package org.kgu.core.ui;

import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.util.Arrays;

public enum GradientTokens {
    PRIMARY(new float[]{0f, 0.18f, 0.55f, 0.88f, 1f}, 0x58B4FF, 0x399EFF, 0x2288F8, 0x075BE5, 0x064FCB),
    SECONDARY(new float[]{0f, 0.18f, 0.55f, 0.88f, 1f}, 0xFFFCF0, 0xFFF6D9, 0xFFE8AE, 0xFFDC8A, 0xF5C96C),
    NEUTRAL(new float[]{0f, 0.22f, 0.6f, 1f}, 0xFFFFFF, 0xF4F9FD, 0xEDEFF3, 0xD9E1F1),
    SELECTED(new float[]{0f, 0.25f, 0.8f, 1f}, 0xEDF6FF, 0xDFEEFF, 0xCFE5FF, 0xBFD9F9),
    DISABLED(new float[]{0f, 0.45f, 1f}, 0xA0ABBC, 0x8490A3, 0x626D80),
    HEADER(new float[]{0f, 0.35f, 0.7f, 1f}, 0x447BC1, 0x3268B0, 0x275898, 0x17457F),
    PANEL(new float[]{0f, 1f}, 0xFFFFFF, 0xF4F9FD),
    PROGRESS(new float[]{0f, 0.3f, 1f}, 0x56BBFF, 0x2495FF, 0x075BE5),
    HP_PLAYER(new float[]{0f, 0.35f, 1f}, 0x79ECA7, 0x30CE79, 0x20BB63),
    HP_ENEMY(new float[]{0f, 0.35f, 1f}, 0xFF9B83, 0xF25240, 0xEB4331),
    XP(new float[]{0f, 0.35f, 1f}, 0xFFE294, 0xFFC54D, 0xEAA52E),
    TRACK(new float[]{0f, 0.3f, 1f}, 0xCCD5E1, 0xD5DEE9, 0xEDF1F6),
    CARD_BLUE(new float[]{0f, 0.55f, 1f}, 0x62B6FF, 0x075BE5, 0x123F96),
    CARD_GOLD(new float[]{0f, 0.55f, 1f}, 0xFFE294, 0xFFC54D, 0x935A18),
    CATEGORY_PURPLE(new float[]{0f, 0.55f, 1f}, 0xD69BF3, 0xA355C5, 0x7444A4);

    private final float[] stops;
    private final Color[] colors;

    GradientTokens(float[] stops, int... colors) {
        this.stops = stops;
        this.colors = Arrays.stream(colors).mapToObj(Color::new).toArray(Color[]::new);
    }

    public LinearGradientPaint paint(int y, int height) {
        return paint(y, height, 1f);
    }

    public LinearGradientPaint paint(int y, int height, float brightness) {
        Color[] adjusted = brightness == 1f ? colors : Arrays.stream(colors).map(color -> new Color(
            Math.clamp(Math.round(color.getRed() * brightness), 0, 255),
            Math.clamp(Math.round(color.getGreen() * brightness), 0, 255),
            Math.clamp(Math.round(color.getBlue() * brightness), 0, 255))).toArray(Color[]::new);
        return new LinearGradientPaint(0, y, 0, y + Math.max(1, height), stops, adjusted);
    }
}
