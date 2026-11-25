package org.kgu.game.view;

import java.awt.*;
import java.awt.geom.Path2D;

/** Category-specific silhouettes, shared by light hits and full-screen finishers. */
enum BattleAttackStyle {
    SCIENCE(new Color(100, 224, 255)),
    HISTORY(new Color(255, 211, 133)),
    IDOL(new Color(255, 113, 211)),
    PROFESSOR(new Color(198, 224, 255));

    final Color color;
    BattleAttackStyle(Color color) { this.color = color; }
    static BattleAttackStyle from(String category) {
        return switch (category) {
            case "한국사" -> HISTORY;
            case "아이돌", "연예인" -> IDOL;
            case "교수님" -> PROFESSOR;
            default -> SCIENCE;
        };
    }

    void strike(Graphics2D source, double time, boolean ultimate, boolean incoming) {
        if (time < 0 || time > (ultimate ? 1.7 : 0.8)) return;
        Graphics2D g = (Graphics2D) source.create();
        g.translate(640, incoming ? 470 : 325);
        double scale = ultimate ? 2.2 : 0.8;
        g.scale(scale, scale);
        float alpha = (float) Math.min(1, Math.max(0, 1 - time / (ultimate ? 1.7 : 0.8)) * 2);
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        switch (this) {
            case SCIENCE -> electricity(g, time, ultimate);
            case HISTORY -> blade(g, time, ultimate);
            case IDOL -> stage(g, time, ultimate);
            case PROFESSOR -> paperBundle(g, time, ultimate);
        }
        g.dispose();
    }

    /** A separate wind-up makes the science finisher build energy before the impact. */
    void prepare(Graphics2D source, double progress, boolean incoming) {
        if (this != SCIENCE) return;
        Graphics2D g = (Graphics2D) source.create();
        g.translate(640, incoming ? 470 : 325);
        int radius = (int) (25 + progress * 55);
        g.setPaint(new RadialGradientPaint(0, 0, radius * 2f, new float[]{0, 0.25f, 1},
            new Color[]{new Color(230, 251, 255, 210), new Color(55, 170, 255, 120), new Color(45, 65, 200, 0)}));
        g.fillOval(-radius * 2, -radius * 2, radius * 4, radius * 4);
        for (int i = 0; i < 36; i++) {
            double a = i * 2.39996;
            double r = 45 + ((i * 29 + (1 - progress) * 320) % 320);
            int x = (int) (Math.cos(a) * r), y = (int) (Math.sin(a) * r * 0.7);
            g.setColor(new Color(135, 229, 255, 170));
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x, y, (int) (x * 1.15), (int) (y * 1.15));
        }
        g.dispose();
    }

    private void electricity(Graphics2D g, double t, boolean ultimate) {
        int channels = ultimate ? 6 : 3;
        for (int i = 0; i < channels; i++) {
            double age = t - i * 0.055;
            if (age < 0 || age > 0.66) continue;
            Graphics2D boltLayer = (Graphics2D) g.create();
            double sx = -310 + i * 123, sy = -330 - i % 2 * 100;
            Path2D bolt = new Path2D.Double(); bolt.moveTo(sx, sy);
            for (int j = 1; j <= 17; j++) {
                double f = j / 17.0;
                double jag = Math.sin(j * 31.7 + i * 17 + (int) (age * 19) * 2.1) * 43 * Math.sin(f * Math.PI);
                double x = sx * (1 - f) + jag, y = sy * (1 - f);
                bolt.lineTo(x, y);
                if (j == 6 || j == 11) {
                    Path2D fork = new Path2D.Double(); fork.moveTo(x, y);
                    int side = i % 2 == 0 ? 1 : -1;
                    fork.lineTo(x + 25 * side, y - 12); fork.lineTo(x + 14 * side, y + 17); fork.lineTo(x + 56 * side, y + 39);
                    boltLayer.setColor(new Color(146, 227, 255, 145)); boltLayer.setStroke(new BasicStroke(1.5f)); boltLayer.draw(fork);
                }
            }
            int alpha = (int) (255 * Math.min(1, (0.66 - age) * 3));
            boltLayer.setStroke(new BasicStroke(18, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            boltLayer.setColor(new Color(64, 92, 255, alpha / 4)); boltLayer.draw(bolt);
            boltLayer.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            boltLayer.setColor(new Color(58, 194, 255, alpha)); boltLayer.draw(bolt);
            boltLayer.setStroke(new BasicStroke(2)); boltLayer.setColor(new Color(235, 255, 255, alpha)); boltLayer.draw(bolt);
            boltLayer.dispose();
        }
        double burst = Math.max(0, t - 0.19);
        double fade = Math.max(0, 1 - burst / 1.25);
        float radius = (float) (23 + Math.min(1, burst * 4) * (ultimate ? 150 : 105));
        g.setPaint(new RadialGradientPaint(0, 0, radius * 1.5f,
            new float[]{0, 0.18f, 0.45f, 1}, new Color[]{new Color(245, 255, 255, (int) (255 * fade)),
            new Color(112, 233, 255, (int) (240 * fade)), new Color(51, 100, 255, (int) (155 * fade)), new Color(20, 40, 190, 0)}));
        g.fillOval((int) (-radius * 1.5), (int) (-radius * 1.5), (int) (radius * 3), (int) (radius * 3));
        // Irregular white-hot plasma core expands once; it does not strobe the whole screen.
        Path2D core = new Path2D.Double();
        for (int i = 0; i <= 72; i++) {
            double angle = i * Math.PI / 36;
            double edge = radius * (0.33 + Math.sin(i * 7.13) * 0.14 + Math.cos(i * 2.8) * 0.07);
            double x = Math.cos(angle) * edge, y = Math.sin(angle) * edge;
            if (i == 0) core.moveTo(x, y); else core.lineTo(x, y);
        }
        core.closePath(); g.setColor(new Color(229, 255, 255, (int) (230 * fade))); g.fill(core);
        for (int i = 0; i < 58; i++) {
            double angle = i * 2.39996, distance = burst * (140 + i % 8 * 32);
            int x = (int) (Math.cos(angle) * distance), y = (int) (Math.sin(angle) * distance * 0.75);
            int length = 6 + i % 5 * 5;
            g.setColor(new Color(i % 3 == 0 ? 255 : 105, 235, 255, (int) (210 * fade)));
            g.setStroke(new BasicStroke(i % 4 == 0 ? 2 : 1));
            g.drawLine(x, y, x + (int) (Math.cos(angle) * length), y + (int) (Math.sin(angle) * length));
        }
        if (burst > 0.3) {
            g.setColor(new Color(121, 207, 255, (int) (190 * fade))); g.setStroke(new BasicStroke(1.5f));
            for (int arc = 0; arc < 4; arc++) {
                Path2D residual = new Path2D.Double();
                for (int i = 0; i < 18; i++) {
                    double a = arc * Math.PI / 2 + i * 0.035;
                    double r = radius * (0.8 + Math.sin(i * 13 + arc + t * 10) * 0.07);
                    double x = Math.cos(a) * r, y = Math.sin(a) * r * 0.7;
                    if (i == 0) residual.moveTo(x, y); else residual.lineTo(x, y);
                }
                g.draw(residual);
            }
        }
    }

    private void blade(Graphics2D g, double t, boolean ultimate) {
        g.rotate(-0.55);
        // A pressure-varying ink ribbon with frayed edges and dry-brush gaps, not a gold outline.
        double reach = Math.min(1, t * 5 + 0.28);
        Path2D ink = new Path2D.Double();
        for (int side : new int[]{-1, 1}) {
            for (int n = 0; n <= 320; n++) {
                double f = (side == -1 ? n : 320 - n) / 320.0 * reach;
                double x = -310 + f * 620;
                double pressure = Math.pow(Math.sin(f * Math.PI), 0.65) * 42;
                double ragged = Math.sin(n * 12.31) * 1.4 + Math.sin(n * 0.29) * 2;
                double y = Math.sin(f * Math.PI * 1.5) * 18 + side * (pressure + ragged);
                if (side == -1 && n == 0) ink.moveTo(x, y); else ink.lineTo(x, y);
            }
        }
        ink.closePath();
        g.setColor(new Color(152, 167, 170, 75));
        g.translate(0, -1); g.fill(ink); g.translate(0, 1);
        g.setColor(new Color(4, 8, 14, 250)); g.fill(ink);
        Shape clip = g.getClip(); g.clip(ink);
        java.util.Random texture = new java.util.Random(41);
        for (int i = 0; i < 65; i++) {
            double y = -36 + texture.nextDouble() * 72;
            double start = -300 + texture.nextDouble() * 300;
            double end = 60 + texture.nextDouble() * 250;
            Path2D bristle = new Path2D.Double(); bristle.moveTo(start, y);
            bristle.curveTo(start + 70, y + 8, end - 90, y + 4, end, y - 9);
            g.setColor(new Color(76, 94, 109, 35 + texture.nextInt(80)));
            g.setStroke(new BasicStroke(0.3f + texture.nextFloat() * 0.8f)); g.draw(bristle);
        }
        g.setClip(clip);
        for (int i = 0; i < 44; i++) {
            double x = Math.sin(i * 11.1) * (100 + t * 160), y = Math.cos(i * 7.3) * (20 + t * 80);
            g.setColor(new Color(5, 10, 17, 235));
            g.fillOval((int) x, (int) y, 2 + i % 8, 2 + i % 5);
        }
    }

    private void stage(Graphics2D g, double t, boolean ultimate) {
        for (int i = 0; i < 5; i++) {
            int top = -340 + i * 170;
            Path2D light = new Path2D.Double(); light.moveTo(top, -400);
            light.lineTo(-70 + i * 24 + Math.sin(t * 5 + i) * 80, 240);
            light.lineTo(70 + i * 24 + Math.sin(t * 5 + i) * 80, 240); light.closePath();
            g.setColor(i % 2 == 0 ? new Color(255, 60, 190, 50) : new Color(111, 140, 255, 60)); g.fill(light);
        }
        g.setColor(color);
        g.setStroke(new BasicStroke(5));
        for (int i = 0; i < 3; i++) {
            int span = (int) (45 + t * 160 + i * 32);
            g.drawArc(-span, -span / 2, span * 2, span, 195, 150);
        }
        for (int i = 0; i < 20; i++) {
            double a = i * 2.39996, r = 30 + t * (130 + i % 5 * 24);
            int x = (int) (Math.cos(a) * r), y = (int) (Math.sin(a) * r);
            g.setColor(i % 2 == 0 ? color : Color.WHITE);
            if (i % 3 == 0) {
                g.fillOval(x, y, 10, 7); g.fillRect(x + 8, y - 21, 3, 26); g.fillRect(x + 8, y - 21, 12, 4);
            } else {
                Path2D star = new Path2D.Double(); star.moveTo(x, y - 11); star.lineTo(x + 3, y - 3);
                star.lineTo(x + 11, y); star.lineTo(x + 3, y + 3); star.lineTo(x, y + 11);
                star.lineTo(x - 3, y + 3); star.lineTo(x - 11, y); star.lineTo(x - 3, y - 3); star.closePath(); g.fill(star);
            }
        }
    }

    private void paperBundle(Graphics2D g, double t, boolean ultimate) {
        double landing = 0.26;
        double flight = Math.min(1, t / landing);
        double x = (ultimate ? -110 : -270) * (1 - flight);
        double y = (ultimate ? -260 : 140) * (1 - flight) - Math.sin(flight * Math.PI) * 40;
        double hit = t - landing;
        if (t < (ultimate ? 1.1 : 0.65)) {
            Graphics2D bundle = (Graphics2D) g.create();
            bundle.translate(x, y + (hit > 0 ? -Math.sin(Math.min(1, hit / 0.2) * Math.PI) * 16 : 0));
            bundle.rotate(-0.55 * (1 - flight) + 0.12);
            int w = ultimate ? 104 : 84, h = ultimate ? 138 : 112;
            bundle.translate(-w / 2.0, -h / 2.0);
            float opacity = (float) Math.min(1, Math.max(0, ((ultimate ? 1.1 : 0.65) - t) / 0.25));
            bundle.setComposite(AlphaComposite.SrcOver.derive(opacity));
            // Unbound, slightly misaligned sheets. No cover, spine or readable title.
            int sheets = ultimate ? 15 : 6;
            for (int sheet = sheets - 1; sheet >= 0; sheet--) {
                Graphics2D page = (Graphics2D) bundle.create();
                double flutter = Math.sin(t * 13 + sheet * 1.8) * 0.012;
                page.translate(Math.sin(sheet * 2.7) * 3, sheet * 1.35);
                page.rotate(flutter + (sheet % 3 - 1) * 0.014, w / 2.0, h / 2.0);
                page.setColor(new Color(241 - sheet / 2, 244 - sheet / 2, 239 - sheet / 2));
                page.fillRect(0, 0, w, h);
                page.setColor(new Color(154, 169, 175)); page.setStroke(new BasicStroke(0.55f)); page.drawRect(0, 0, w, h);
                if (sheet == 0) {
                    // Short broken grey marks suggest printed paragraphs without legible text.
                    page.setColor(new Color(116, 132, 140));
                    page.fillRect(14, 15, w - 39, 2);
                    for (int line = 0; line < 12; line++) {
                        int baseline = 29 + line * 6;
                        if (baseline > h - 13) break;
                        int end = w - 13 - (line % 4 == 3 ? 15 : 0);
                        for (int word = 13; word < end; word += 10) {
                            page.fillRect(word, baseline, Math.min(7 + line % 2, end - word), 1);
                        }
                    }
                }
                page.dispose();
            }
            bundle.dispose();
        }
        if (hit >= 0 && hit < 0.25) {
            g.setColor(new Color(230, 245, 255, (int) (200 * (1 - hit / 0.25))));
            g.setStroke(new BasicStroke(2));
            for (int i = 0; i < 18; i++) {
                double angle = i * Math.PI / 9, start = 65 + hit * 90, end = start + 20 + hit * 90;
                g.drawLine((int) (Math.cos(angle) * start), (int) (Math.sin(angle) * start),
                    (int) (Math.cos(angle) * end), (int) (Math.sin(angle) * end));
            }
        }
        if (hit > 0) {
            for (int i = 0; i < (ultimate ? 25 : 9); i++) {
                double angle = i * 2.39996;
                Graphics2D page = (Graphics2D) g.create();
                page.translate(Math.cos(angle) * hit * (100 + i % 5 * 35),
                    Math.sin(angle) * hit * 140 + hit * hit * 85);
                page.rotate(angle + hit * (i % 2 == 0 ? 3 : -3));
                float opacity = (float) Math.min(1, Math.max(0, 1 - hit / 1.4));
                page.setComposite(AlphaComposite.SrcOver.derive(opacity));
                page.setColor(new Color(242, 244, 228)); page.fillRect(-7, -10, 14, 20);
                page.setColor(new Color(134, 154, 164)); page.setStroke(new BasicStroke(0.5f));
                for (int line = 0; line < 4; line++) page.drawLine(-4, -5 + line * 3, 4, -5 + line * 3);
                page.dispose();
            }
        }
    }
}
