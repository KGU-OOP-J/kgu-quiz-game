package org.kgu.game.view;

import java.awt.*;
import org.kgu.window.UiStyle;

/** Short category hit, independent of HP and navigation. */
final class BattleAttackEffect {
    private final BattleAttackStyle style;
    private long started;
    private double elapsed = -1;
    BattleAttackEffect(String category) { style = BattleAttackStyle.from(category); }
    void start(long now) { started = now; elapsed = 0; }
    void update(long now) { if (elapsed >= 0) elapsed = Math.max(0, (now - started) / 1_000_000_000.0); }
    void paint(Graphics2D source, int width, int height) {
        if (elapsed < 0 || elapsed > 0.8) return;
        Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g);
        double scale = Math.min(width / 1280.0, height / 960.0);
        g.translate((width - 1280 * scale) / 2, (height - 960 * scale) / 2); g.scale(scale, scale);
        g.clipRect(335, 172, 590, 340);
        style.strike(g, elapsed, false, false); g.dispose();
    }
}
