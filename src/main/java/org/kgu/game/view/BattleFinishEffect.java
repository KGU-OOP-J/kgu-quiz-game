package org.kgu.game.view;

import java.awt.*;
import org.kgu.window.UiStyle;

/** Timed category finisher. Outcome remains owned by BattleEngine. */
final class BattleFinishEffect {
    static final double DURATION = 2.8;
    private long started;
    private double elapsed = -1;
    private boolean victory;
    private BattleAttackStyle style;

    void start(boolean victory, String category, long now) {
        this.victory = victory;
        style = BattleAttackStyle.from(category);
        started = now; elapsed = 0;
    }
    void update(long now) { if (elapsed >= 0) elapsed = Math.max(0, (now - started) / 1_000_000_000.0); }
    boolean isRunning() { return elapsed >= 0 && elapsed < DURATION; }
    boolean impacted() { return elapsed >= 0.9; }
    int shakeOffset() {
        double t = elapsed - 0.9;
        return t < 0 || t > 0.45 ? 0 : (int) (Math.sin(t * 125) * 27 * (1 - t / 0.45));
    }

    void paint(Graphics2D source, int width, int height) {
        if (elapsed < 0) return;
        // Professor defeat keeps the original full-screen F stamp as the only main effect.
        if (!victory && style == BattleAttackStyle.PROFESSOR) return;
        Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g);
        double scale = Math.min(width / 1280.0, height / 960.0);
        g.translate((width - 1280 * scale) / 2, (height - 960 * scale) / 2); g.scale(scale, scale);
        g.setColor(new Color(2, 7, 19, 175)); g.fillRect(0, 0, 1280, 960);
        g.setColor(new Color(1, 4, 12, 245)); g.fillRect(0, 0, 1280, 68); g.fillRect(0, 892, 1280, 68);
        if (elapsed < 0.9) {
            style.prepare(g, elapsed / 0.9, !victory);
            // Streaks build direction and speed, instead of decorative orbiting rings.
            for (int i = 0; i < 18; i++) {
                int x = (int) ((i * 173 + elapsed * 1400) % 1600) - 160;
                g.setColor(new Color(style.color.getRed(), style.color.getGreen(), style.color.getBlue(), 90));
                g.drawLine(x, 115 + i * 22, x + 130, 100 + i * 22);
            }
        } else {
            double impact = elapsed - 0.9;
            style.strike(g, impact, true, !victory);
            // A single impact flash and brief visual hold; no repeated strobe.
            if (impact < 0.1) {
                g.setColor(new Color(235, 247, 255, (int) (210 * (1 - impact / 0.1)))); g.fillRect(0, 0, 1280, 960);
            }

        }
        g.dispose();
    }
}
