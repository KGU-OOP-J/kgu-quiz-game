package org.kgu.ui.battle;

import java.awt.*;

/** Professor's defeat cutscene. The engine has already decided failure before this starts. */
public final class FinalAttackEffect {
    private static final long DURATION_NANOS = 2_800_000_000L;
    private long startedAt;
    private double progress = -1;

    public void start(long now) {
        startedAt = now;
        progress = 0;
    }

    public void update(long now) {
        if (isRunning()) progress = Math.min(1, Math.max(0, (now - startedAt) / (double) DURATION_NANOS));
    }

    public boolean isVisible() { return progress >= 0; }
    public boolean isRunning() { return progress >= 0 && progress < 1; }

    public int shakeOffset() {
        if (progress < 0.42 || progress >= 0.7) return 0;
        double impact = (progress - 0.42) / 0.28;
        return (int) (Math.sin(impact * 110) * 30 * (1 - impact));
    }

    public void paint(Graphics2D source, int width, int height) {
        if (!isVisible()) return;
        Graphics2D g = (Graphics2D) source.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int darkness = progress < 0.3 ? (int) (70 + progress * 300) : 180;
        g.setColor(new Color(14, 8, 37, darkness));
        g.fillRect(0, 0, width, height);
        double scale = Math.min(width / 1280.0, height / 960.0);
        g.translate((width - 1280 * scale) / 2, (height - 960 * scale) / 2);
        g.scale(scale, scale);
        if (progress < 0.3) paintCharge(g);
        else if (progress < 0.65) paintImpact(g);
        else paintDefeat(g);
        g.dispose();
    }

    private void paintCharge(Graphics2D g) {
        centered(g, "……", 650, 46, Color.WHITE);
    }

    private void paintImpact(Graphics2D g) {
        double landing = Math.min(1, (progress - 0.3) / 0.12);
        // The stamp rushes toward the screen, lands once, and stays on the failed attempt.
        double stampScale = 1 + 2.2 * Math.pow(1 - landing, 3);
        if (landing >= 1) {
            double impact = (progress - 0.42) / 0.23;
            int radius = (int) (190 + impact * 720);
            g.setColor(new Color(255, 111, 111, (int) (145 * Math.max(0, 1 - impact))));
            g.setStroke(new BasicStroke(9));
            g.drawOval(640 - radius, 350 - radius, radius * 2, radius * 2);
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < 12; i++) {
                double angle = i * Math.PI / 6;
                int inner = radius / 2;
                g.drawLine(640 + (int) (Math.cos(angle) * inner), 350 + (int) (Math.sin(angle) * inner),
                        640 + (int) (Math.cos(angle) * radius), 350 + (int) (Math.sin(angle) * radius));
            }
        }
        if (progress >= 0.42 && progress < 0.47) {
            g.setColor(new Color(255, 235, 235, (int) (210 * (1 - (progress - 0.42) / 0.05))));
            g.fillRect(0, 0, 1280, 960);
        }
        if (progress >= 0.42) {
            g.setColor(new Color(250, 75, 95, 180));
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < 11; i++) {
                double a = i * Math.PI * 2 / 11;
                int x = 640, y = 350;
                for (int j = 1; j <= 4; j++) {
                    int nx = 640 + (int) (Math.cos(a + Math.sin(i * 9 + j) * 0.12) * j * 150);
                    int ny = 350 + (int) (Math.sin(a + Math.sin(i * 9 + j) * 0.12) * j * 150);
                    g.drawLine(x, y, nx, ny); x = nx; y = ny;
                }
            }
        }
        paintStamp(g, stampScale, (float) Math.min(1, 0.35 + landing));
    }

    private void paintDefeat(Graphics2D g) {
        g.setColor(new Color(8, 5, 22, 100));
        g.fillRect(0, 0, 1280, 960);
        paintStamp(g, 1, 1);
        centered(g, "“형편없구만…”", 640, 42, Color.WHITE);
        centered(g, "패 배", 699, 25, new Color(236, 175, 175));
    }

    private void paintStamp(Graphics2D source, double scale, float opacity) {
        Graphics2D g = (Graphics2D) source.create();
        g.setComposite(AlphaComposite.SrcOver.derive(opacity));
        g.translate(640, 350);
        g.scale(scale * 1.18, scale * 1.18);
        g.rotate(Math.toRadians(-12));
        g.setColor(new Color(186, 18, 35, 35));
        g.fillRoundRect(-145, -175, 290, 350, 14, 14);
        g.setColor(new Color(245, 54, 68));
        g.setStroke(new BasicStroke(11));
        g.drawRoundRect(-145, -175, 290, 350, 14, 14);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(-131, -161, 262, 322, 8, 8);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 320);
        Shape letter = font.createGlyphVector(g.getFontRenderContext(), "F").getOutline();
        var bounds = letter.getBounds2D();
        g.translate(-bounds.getCenterX(), -bounds.getCenterY());
        g.fill(letter);
        g.dispose();
    }

    private void centered(Graphics2D g, String text, int baseline, int size, Color color) {
        g.setFont(new Font("Malgun Gothic", Font.BOLD, size));
        int x = (1280 - g.getFontMetrics().stringWidth(text)) / 2;
        g.setColor(new Color(12, 7, 30, 220));
        g.drawString(text, x + 3, baseline + 3);
        g.setColor(color);
        g.drawString(text, x, baseline);
    }
}
