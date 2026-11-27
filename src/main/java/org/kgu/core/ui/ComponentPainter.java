package org.kgu.core.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ComponentPainter {
    public static final Color TEXT = new Color(0x101C3B);
    public static final Color MUTED = new Color(0x626D80);
    public static final Color BORDER = new Color(0xD5DEE9);
    public static final Color SELECTED = new Color(0x0875FF);
    public static final Color FOCUS = new Color(0x95D1FA);
    public static final Color CORRECT = new Color(0x146C43);
    public static final Color INCORRECT = new Color(0xB43328);
    private static final Map<Dimension, BufferedImage> BUTTON_SHADOWS = new ConcurrentHashMap<>();
    private static final ConvolveOp BUTTON_BLUR = buttonBlur();

    private ComponentPainter() { }

    public static void surface(Graphics2D g, int x, int y, int width, int height, int radius,
                               GradientTokens fill, Color border) {
        var shape = new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1, height - 1, radius * 2, radius * 2);
        g.setPaint(fill.paint(y, height));
        g.fill(shape);
        g.setColor(border);
        g.setStroke(new BasicStroke(1));
        g.draw(shape);
    }

    public static void selectionBadge(Graphics2D g, int width) {
        int x = width - 8 - 24;
        int y = 8;
        g.setPaint(GradientTokens.PRIMARY.paint(y, 24));
        g.fillOval(x, y, 24, 24);
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(new int[]{x + 6, x + 10, x + 18}, new int[]{y + 12, y + 16, y + 8}, 3);
    }

    public static Color button(Graphics2D g, int width, int height, GradientTokens style,
                               boolean selected, boolean correct, boolean incorrect,
                               boolean hover, boolean pressed, boolean focused) {
        return button(g, width, height, style, selected, correct, incorrect, hover ? 1f : 0f, pressed ? 1f : 0f, focused);
    }

    public static Color button(Graphics2D g, int width, int height, GradientTokens style,
                               boolean selected, boolean correct, boolean incorrect,
                               float hover, float pressed, boolean focused) {
        if (width < 8 || height < 8) return TEXT;
        float pressure = Math.clamp(pressed, 0f, 1f);
        float y = 1 + 2 * pressure;
        var shape = new RoundRectangle2D.Float(3.5f, y + .5f, width - 7, height - 7 - y, 24, 24);
        if (style != GradientTokens.DISABLED && pressure == 0) g.drawImage(buttonShadow(width, height), -4, -4, null);
        float brightness = style == GradientTokens.DISABLED ? 1f : pressure > 0 ? 1f - .1f * pressure : 1f + .06f * Math.clamp(hover, 0f, 1f);
        Paint fill = style.paint(Math.round(y), Math.max(1, height - 7 - Math.round(y)), brightness);
        g.setPaint(correct ? new Color(0xDDF8E8) : incorrect ? new Color(0xFFE7E4) : fill);
        g.fill(shape);
        Color border = correct ? new Color(0x20BB63) : incorrect ? new Color(0xF25240)
            : style == GradientTokens.DISABLED ? new Color(0x8490A3)
            : selected ? SELECTED : switch (style) {
                case PRIMARY -> new Color(0x075BE5);
                case SECONDARY -> new Color(0xB87819);
                case DISABLED -> new Color(0x8490A3);
                case NEUTRAL -> new Color(0xA0ABBC);
                default -> BORDER;
            };
        g.setColor(border);
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        Shape previousClip = g.getClip();
        g.clip(shape);
        if (pressure > 0) {
            g.setPaint(new GradientPaint(0, y, new Color(24, 48, 76, Math.round(68 * pressure)), 0, y + 8, new Color(24, 48, 76, 0)));
            g.fillRect(0, Math.round(y), width, 8);
        } else {
            g.clipRect(0, 0, width, Math.round(y) + 8);
            g.setColor(new Color(255, 255, 255, style == GradientTokens.DISABLED ? 68 : 204));
            g.draw(new RoundRectangle2D.Float(4.5f, y + 1.5f, width - 9, height - 9 - y, 22, 22));
        }
        g.setClip(previousClip);
        if (focused) {
            g.setColor(FOCUS);
            g.setStroke(new BasicStroke(3));
            g.draw(new RoundRectangle2D.Float(1.5f, 1.5f, width - 3, height - 4, 28, 28));
        }
        if (correct) return CORRECT;
        if (incorrect) return INCORRECT;
        return style == GradientTokens.PRIMARY || style == GradientTokens.DISABLED ? Color.WHITE : TEXT;
    }

    private static BufferedImage buttonShadow(int width, int height) {
        if (BUTTON_SHADOWS.size() >= 64) BUTTON_SHADOWS.clear();
        return BUTTON_SHADOWS.computeIfAbsent(new Dimension(width, height), size -> {
            BufferedImage mask = new BufferedImage(width + 8, height + 8, BufferedImage.TYPE_INT_ARGB);
            Graphics2D shadow = mask.createGraphics();
            shadow.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            shadow.setColor(new Color(24, 48, 76, 56));
            shadow.fill(new RoundRectangle2D.Float(7.5f, 8.5f, width - 7, height - 8, 24, 24));
            shadow.dispose();
            return BUTTON_BLUR.filter(mask, null);
        });
    }

    private static ConvolveOp buttonBlur() {
        float[] weights = new float[81];
        float total = 0;
        for (int y = -4; y <= 4; y++) for (int x = -4; x <= 4; x++) {
            float weight = (float)Math.exp(-(x * x + y * y) / 8.0);
            weights[(y + 4) * 9 + x + 4] = weight;
            total += weight;
        }
        for (int index = 0; index < weights.length; index++) weights[index] /= total;
        return new ConvolveOp(new Kernel(9, 9, weights), ConvolveOp.EDGE_NO_OP, null);
    }

    public static void progress(Graphics2D g, int width, int height, double fraction, GradientTokens fill) {
        var track = new RoundRectangle2D.Double(0, 0, width, height, height, height);
        g.setPaint(GradientTokens.TRACK.paint(0, height));
        g.fill(track);
        int filled = (int)Math.round(width * Math.clamp(fraction, 0, 1));
        if (filled <= 0) return;
        Shape previous = g.getClip();
        g.clip(track);
        g.setPaint(fill.paint(0, height));
        g.fillRoundRect(0, 0, filled, height, height, height);
        g.setClip(previous);
    }
}
