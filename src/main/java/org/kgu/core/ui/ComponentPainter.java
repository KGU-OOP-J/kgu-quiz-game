package org.kgu.core.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class ComponentPainter {
    public static final Color TEXT = new Color(0x101C3B);
    public static final Color MUTED = new Color(0x626D80);
    public static final Color BORDER = new Color(0xD5DEE9);
    public static final Color SELECTED = new Color(0x0875FF);
    public static final Color FOCUS = new Color(0x95D1FA);
    public static final Color CORRECT = new Color(0x146C43);
    public static final Color INCORRECT = new Color(0xB43328);

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
        int y = pressed ? 2 : 0;
        var shape = new RoundRectangle2D.Float(1, 1 + y, width - 3, height - 3 - y, 24, 24);
        Paint fill = switch (style) {
            case PRIMARY -> new Color(0x075BE5);
            case NEUTRAL -> new Color(0xF4F9FD);
            case DISABLED -> new Color(0xEDEFF3);
            default -> style.paint(y, height);
        };
        g.setPaint(correct ? new Color(0xDDF8E8) : incorrect ? new Color(0xFFE7E4) : fill);
        g.fill(shape);
        Color border = correct ? new Color(0x20BB63) : incorrect ? new Color(0xF25240)
            : selected ? SELECTED : switch (style) {
                case PRIMARY -> new Color(0x075BE5);
                case SECONDARY -> new Color(0xB87819);
                case DISABLED -> BORDER;
                case NEUTRAL -> new Color(0xA0ABBC);
                default -> BORDER;
            };
        g.setColor(border);
        g.setStroke(new BasicStroke(1f));
        g.draw(shape);
        if (hover || pressed) {
            g.setColor(pressed ? new Color(0x18101C3B, true) : new Color(0x1AFFFFFF, true));
            g.fill(shape);
        }
        if (focused) {
            g.setColor(FOCUS);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(3, 3 + y, width - 7, height - 7 - y, 20, 20);
        }
        if (correct) return CORRECT;
        if (incorrect) return INCORRECT;
        if (style == GradientTokens.DISABLED) return MUTED;
        return style == GradientTokens.PRIMARY ? Color.WHITE : TEXT;
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
