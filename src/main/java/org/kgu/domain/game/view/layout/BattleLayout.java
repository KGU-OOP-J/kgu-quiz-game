package org.kgu.domain.game.view.layout;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;

import javax.swing.JScrollPane;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class BattleLayout {
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 960;
    private final List<Placement> placements = new ArrayList<>();

    private record Placement(Component component, Rectangle bounds, int fontSize) { }

    public void add(Component component, Rectangle bounds, int fontSize) {
        placements.add(new Placement(component, bounds, fontSize));
    }

    public void place(int width, int height) {
        double scale = scale(width, height);
        int dx = (int)((width - WIDTH * scale) / 2);
        int dy = (int)((height - HEIGHT * scale) / 2);
        for (var placement : placements) {
            var component = placement.component();
            var bounds = placement.bounds();
            component.setBounds(dx + (int)(bounds.x * scale), dy + (int)(bounds.y * scale),
                (int)(bounds.width * scale), (int)(bounds.height * scale));
            Font font = UiStyle.FONT.deriveFont(Font.BOLD, Math.max(12f, (float)(placement.fontSize() * scale)));
            component.setFont(font);
            if (component instanceof JScrollPane scroll && scroll.getViewport().getView() != null) {
                scroll.getViewport().getView().setFont(font);
            }
            if (component instanceof Container container) container.doLayout();
        }
    }

    public static void paintStage(Graphics2D g, int width, int height) {
        GameTheme.backdrop(g, width, height, "battle", 0.40f);
        double scale = scale(width, height);
        g.translate((width - WIDTH * scale) / 2, (height - HEIGHT * scale) / 2);
        g.scale(scale, scale);
        ComponentPainter.surface(g, 28, 18, 1224, 50, 12, GradientTokens.HEADER, new Color(0x275898));
        paintPanel(g, 28, 150, 305, 153);
        paintPanel(g, 28, 314, 305, 192);
        paintPanel(g, 930, 150, 314, 356);
        paintPanel(g, 28, 530, 1224, 414);
        g.setColor(ComponentPainter.BORDER);
        g.drawLine(58, 839, 1222, 839);
    }

    private static void paintPanel(Graphics2D g, int x, int y, int width, int height) {
        ComponentPainter.surface(g, x, y, width, height, 16, GradientTokens.PANEL, ComponentPainter.BORDER);
    }

    private static double scale(int width, int height) {
        return Math.min(width / (double)WIDTH, height / (double)HEIGHT);
    }
}
