package org.kgu.domain.game.view.component;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.domain.game.feature.BattleEngine;
import java.awt.*;
import java.awt.image.BufferedImage;
import org.kgu.domain.game.view.layout.BattleLayout;

public final class BossPhaseEffect {
    private long started;
    private double progress = 1;
    private BattleEngine engine;
    private Image portrait;
    private int duration;
    private String previousBackground;
    private BufferedImage backgroundFrame, backgroundAsset, baseBackground;

    public void start(BattleEngine engine, Image portrait, long now) {
        start(engine, portrait, now, null);
    }
    public void start(BattleEngine engine, Image portrait, long now, String previousBackground) {
        this.engine = engine; this.portrait = portrait; started = now;
        this.previousBackground = previousBackground; backgroundFrame = null; backgroundAsset = null; baseBackground = null;
        duration = engine.stageNumber() == 1 ? 1500 : 1200; progress = 0;
        GameResources.play(engine.stageNumber() == 1 ? GameResources.Sound.BOSS_ENTRANCE : GameResources.Sound.PHASE_TRANSITION);
    }
    public void update(long now) { progress = Math.clamp((now - started) / (duration * 1_000_000.0), 0, 1); }
    public void skip() { progress = 1; }
    public void pause(long nanos) { if (running()) started += nanos; }
    public boolean running() { return progress < 1; }
    public int shake() { return running() ? (int)(Math.sin(progress * 90) * 6 * (1 - progress)) : 0; }
    public void paintBackground(Graphics2D source, int width, int height) {
        if (width <= 0 || height <= 0) return;
        if (previousBackground == null || !running()) { BattleLayout.paintBackground(source, width, height, engine.stage().background()); return; }
        BufferedImage current = GameResources.background(engine.stage().background()).orElse(null);
        BufferedImage base = GameResources.background("battle").orElse(null);
        if (backgroundFrame == null || backgroundFrame.getWidth() != width || backgroundFrame.getHeight() != height || current != backgroundAsset || base != baseBackground) {
            backgroundAsset = current; baseBackground = base;
            backgroundFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D snapshot = backgroundFrame.createGraphics();
            snapshot.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            BattleLayout.paintBackground(snapshot, width, height, engine.stage().background());
            snapshot.dispose();
        }
        BattleLayout.paintBackground(source, width, height, previousBackground);
        Graphics2D g = (Graphics2D)source.create();
        g.setComposite(AlphaComposite.SrcOver.derive((float)progress)); g.drawImage(backgroundFrame, 0, 0, null); g.dispose();
    }
    public void paint(Graphics2D source, int width, int height) {
        if (!running()) return;
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        double p = progress;
        float opacity = (float)Math.min(1, (1 - p) * 5);
        g.setComposite(AlphaComposite.SrcOver.derive(opacity));
        int panelHeight = Math.min(340, height / 2), top = (height - panelHeight) / 2;
        g.setColor(new Color(242, 248, 255, 245)); g.fillRect(0, top, width, panelHeight);
        g.setColor(ComponentPainter.SELECTED); g.setStroke(new BasicStroke(2));
        int radius = (int)(p * width * .7);
        g.drawOval(width / 2 - radius, height / 2 - radius, radius * 2, radius * 2);
        for (int i = 0; i < 32; i++) {
            double a = i * Math.PI * 2 / 32;
            int x = width / 2 + (int)(Math.cos(a) * radius), y = height / 2 + (int)(Math.sin(a) * radius);
            g.fillOval(x, y, 4, 4);
        }
        int portraitWidth = Math.min(200, width / 5);
        if (portrait != null) GameTheme.cover(g, portrait, Math.max(20, width / 8 - (int)((1 - Math.min(1, p * 4)) * width / 3)), top + 16, portraitWidth, panelHeight - 32);
        int textX = width / 3;
        g.setColor(ComponentPainter.TEXT); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 32f));
        g.drawString(engine.stage().title(), textX, top + 75);
        g.setFont(UiStyle.FONT.deriveFont(18f));
        g.drawString(engine.computer().getName() + " · 페이즈 " + engine.stageNumber() + " / " + engine.stageCount(), textX, top + 112);
        int baseline = top + 154;
        for (String line : GameTheme.wrap(engine.stage().dialogue(), g.getFontMetrics(), Math.max(100, width - textX - 45))) {
            g.drawString(line, textX, baseline); baseline += 26;
        }
        g.setColor(ComponentPainter.MUTED); g.drawString("ESC · 연출 건너뛰기", textX, top + panelHeight - 24);
        g.dispose();
    }
}
