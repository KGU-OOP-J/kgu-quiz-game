package org.kgu.domain.collection.view.layout;

import java.awt.*;
import org.kgu.domain.collection.view.component.CardView;
import org.kgu.core.window.UiStyle;

public final class CollectionLayout {
    public static final int WIDTH = 1280, HEIGHT = 960;
    public static final double SX = WIDTH / (double) UiStyle.WIDTH;
    public static final double SY = HEIGHT / (double) UiStyle.HEIGHT;

    private CollectionLayout() { }

    public static void scale(Component c) {
        Rectangle b = c.getBounds();
        c.setBounds((int) Math.round(b.x * SX), (int) Math.round(b.y * SY), (int) Math.round(b.width * SX), (int) Math.round(b.height * SY));
        if (c.isPreferredSizeSet()) {
            Dimension d = c.getPreferredSize();
            c.setPreferredSize(new Dimension((int) Math.round(d.width * SX), (int) Math.round(d.height * SY)));
        }
        Font f = c.getFont();
        if (f != null) c.setFont(f.deriveFont((float) (f.getSize2D() * SX)));
        // CardView는 그림을 자기 크기에 맞춰 그리므로 안쪽은 건드리지 않는다
        if (c instanceof Container container && !(c instanceof CardView)) {
            for (Component child : container.getComponents()) scale(child);
        }
    }

    public static void place(java.util.Map<Component, Rectangle> positions, int width, int height,
                             int baseWidth, int baseHeight) {
        float sx = width / (float)baseWidth, sy = height / (float)baseHeight;
        positions.forEach((child, bounds) -> child.setBounds(
            Math.round(bounds.x * sx), Math.round(bounds.y * sy),
            Math.round(bounds.width * sx), Math.round(bounds.height * sy)));
    }
}
