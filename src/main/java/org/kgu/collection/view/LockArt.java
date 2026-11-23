package org.kgu.collection.view;

import org.kgu.window.UiStyle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/**
 * 잠긴 마을(경기대) 위의 봉인 연출. 잠겨 있을 때는 어둠·마법진·룬 쇠사슬·번개·검은 자물쇠를,
 * 풀릴 때는 금빛 균열 → 섬광과 함께 자물쇠가 산산조각 → 충격파·빛줄기·반짝이로 어둠이 걷히는 모습을 그린다.
 * 좌표는 지도 픽셀, s는 780 기준 배율이다.
 */
final class LockArt {
    private static final Color PURPLE = new Color(186, 96, 255), GOLD = new Color(255, 214, 110);

    private LockArt() {
    }

    /**
     * @param t 0이면 잠김, 0~1이면 풀리는 중(1이면 호출하지 않는다)
     */
    static void paint(Graphics2D g, WaveMap map, int v, float s, double seconds, double t, String hint) {
        Rectangle b = map.villageBounds(v);
        double dark = 1 - phase(t, 0.42, 0.95);
        double gold = phase(t, 0.12, 0.38);  // 보랏빛이 금빛으로 바뀌는 정도
        map.dimVillage(g, v, (float) dark);
        map.auraVillage(g, v, (float) (dark * (0.42 + 0.1 * Math.sin(seconds * 2.2))));
        int cx = b.x + b.width / 2, cy = b.y + b.height * 9 / 20;
        seal(g, cx, b.y + b.height * 0.56, b.width * 0.36, s, seconds, t, gold);
        wisps(g, b, s, seconds, dark);
        chains(g, b, cx, cy, s, seconds, t, gold);
        if (t <= 0) lightning(g, b, cx, cy, s, seconds);
        lock(g, cx, cy, s * 1.3f, seconds, t, gold);
        burst(g, b, cx, cy, s, t);
        if (t <= 0) hint(g, b, cx, s, hint);
    }

    private static double phase(double t, double from, double to) {
        return Math.max(0, Math.min(1, (t - from) / (to - from)));
    }

    private static double easeOut(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    /** 같은 (i, k)에는 늘 같은 0~1 값(조각·반짝이 위치를 매 프레임 같게). */
    private static double rnd(int i, int k) {
        double x = Math.sin(i * 127.1 + k * 311.7) * 43758.5453;
        return x - Math.floor(x);
    }

    private static Color mix(Color a, Color b, double f, double alpha) {
        f = Math.max(0, Math.min(1, f));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * f), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
            (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * f), (int) Math.max(0, Math.min(255, alpha)));
    }

    /** 넓게 흐린 선 위에 얇고 밝은 선을 겹쳐 빛나는 선처럼 그린다. */
    private static void glowStroke(Graphics2D g, Shape shape, Color color, float width) {
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 4));
        g.setStroke(new BasicStroke(width * 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
        g.setColor(color);
        g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(shape);
    }

    /** 섬 바닥에 비스듬히 누운 채 천천히 도는 마법진. 풀릴 때 금빛으로 바뀌고 커지며 사라진다. */
    private static void seal(Graphics2D source, double x, double y, double radius, float s, double seconds, double t, double gold) {
        double out = phase(t, 0.4, 0.85);
        if (out >= 1) return;
        Graphics2D g = (Graphics2D) source.create();
        g.translate(x, y);
        g.scale(1, 0.42);
        double r = radius * (1 + 0.5 * easeOut(out));
        int alpha = (int) (210 * (1 - out) * (0.75 + 0.25 * Math.sin(seconds * 2.6)));
        Color c = mix(PURPLE, GOLD, gold, alpha);
        float w = 1.6f * s;
        glowStroke(g, new Ellipse2D.Double(-r, -r, r * 2, r * 2), c, w * 1.3f);
        glowStroke(g, new Ellipse2D.Double(-r * 0.8, -r * 0.8, r * 1.6, r * 1.6), c, w);
        glowStroke(g, new Ellipse2D.Double(-r * 0.5, -r * 0.5, r, r), c, w * 0.8f);
        Graphics2D runes = (Graphics2D) g.create();
        runes.rotate(seconds * 0.35 + t * 6);
        for (int i = 0; i < 16; i++) {  // 바깥 띠의 룬 글자
            Graphics2D rune = (Graphics2D) runes.create();
            rune.rotate(i * Math.PI / 8);
            rune.translate(0, -r * 0.9);
            double k = r * 0.05;
            Path2D p = new Path2D.Double();
            switch (i % 4) {
                case 0 -> { p.moveTo(-k, k); p.lineTo(0, -k); p.lineTo(k, k); }
                case 1 -> { p.moveTo(0, -k); p.lineTo(0, k); p.moveTo(-k, 0); p.lineTo(k, -k); }
                case 2 -> { p.moveTo(-k, -k); p.lineTo(k, -k); p.lineTo(-k, k); p.lineTo(k, k); }
                default -> p.append(new Ellipse2D.Double(-k * 0.7, -k * 0.7, k * 1.4, k * 1.4), false);
            }
            glowStroke(rune, p, c, w * 0.8f);
            rune.dispose();
        }
        runes.dispose();
        Graphics2D star = (Graphics2D) g.create();  // 안쪽 육망성은 반대로 돈다
        star.rotate(-seconds * 0.5 - t * 4);
        for (int tri = 0; tri < 2; tri++) {
            Path2D p = new Path2D.Double();
            for (int k = 0; k < 3; k++) {
                double a = tri * Math.PI / 3 + k * 2 * Math.PI / 3 - Math.PI / 2;
                if (k == 0) p.moveTo(Math.cos(a) * r * 0.78, Math.sin(a) * r * 0.78);
                else p.lineTo(Math.cos(a) * r * 0.78, Math.sin(a) * r * 0.78);
            }
            p.closePath();
            glowStroke(star, p, c, w * 0.9f);
        }
        star.dispose();
        g.dispose();
    }

    /** 섬 위로 피어오르는 검보랏빛 연기. */
    private static void wisps(Graphics2D source, Rectangle b, float s, double seconds, double dark) {
        if (dark <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        for (int i = 0; i < 11; i++) {
            double life = (seconds * 0.28 + i / 11.0) % 1;
            double x = b.x + b.width * (0.1 + 0.8 * rnd(i, 1)) + Math.sin(seconds * 1.1 + i) * 10 * s;
            double y = b.y + b.height * (0.9 - 0.95 * life);
            float r = (float) ((16 + 12 * rnd(i, 2)) * s * (0.5 + life));
            int alpha = (int) (130 * Math.sin(Math.PI * life) * dark);
            Color core = i % 3 == 0 ? new Color(170, 80, 240, alpha) : new Color(40, 10, 60, alpha);
            g.setPaint(new RadialGradientPaint((float) x, (float) y, r, new float[]{0f, 1f}, new Color[]{core, new Color(20, 0, 40, 0)}));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
        g.dispose();
    }

    /** 섬을 X자로 묶은 룬 쇠사슬. 룬 빛이 자물쇠 쪽으로 흐르고, 풀릴 때 금빛이 됐다가 터지듯 흩어진다. */
    private static void chains(Graphics2D source, Rectangle b, int cx, int cy, float s, double seconds, double t, double gold) {
        double snap = phase(t, 0.38, 0.62);
        if (snap >= 1) return;
        Graphics2D g = (Graphics2D) source.create();
        double rattle = t > 0 && t < 0.38 ? Math.sin(t * 220) * 2.5 * s * (t / 0.38) : 0;
        double[][] ends = {
            {b.x + b.width * 0.04, b.y + b.height * 0.2, b.x + b.width * 0.96, b.y + b.height * 0.76},
            {b.x + b.width * 0.96, b.y + b.height * 0.18, b.x + b.width * 0.06, b.y + b.height * 0.78}};
        float k = s * 1.55f;
        double step = 7.6 * k;
        for (int c = 0; c < ends.length; c++) {
            double[] e = ends[c];
            double len = Math.hypot(e[2] - e[0], e[3] - e[1]), angle = Math.atan2(e[3] - e[1], e[2] - e[0]);
            int count = (int) (len / step);
            for (int i = 0; i <= count; i++) {
                double u = i / (double) count;
                double x = e[0] + (e[2] - e[0]) * u, y = e[1] + (e[3] - e[1]) * u + Math.sin(Math.PI * u) * 10 * s + rattle;
                double spin = 0;
                if (snap > 0) {  // 자물쇠에서 멀어지는 쪽으로 튕겨 나가며 돌고 떨어진다
                    double dx = x - cx, dy = y - cy, d = Math.max(1, Math.hypot(dx, dy));
                    double push = easeOut(snap) * (35 + 55 * rnd(i, c)) * s;
                    x += dx / d * push;
                    y += dy / d * push + 110 * s * snap * snap;
                    spin = (rnd(i, c + 7) - 0.5) * 9 * snap;
                }
                Graphics2D link = (Graphics2D) g.create();
                link.setComposite(AlphaComposite.SrcOver.derive((float) (1 - snap)));
                link.translate(x, y);
                link.rotate(angle + spin);
                link.scale(k, k);
                if (i % 2 == 0) {
                    link.setStroke(new BasicStroke(4.2f));
                    link.setColor(new Color(10, 8, 16));
                    link.drawOval(-6, -3, 12, 6);
                    link.setStroke(new BasicStroke(2.2f));
                    link.setColor(new Color(112, 108, 132));
                    link.drawOval(-6, -3, 12, 6);
                    link.setColor(new Color(200, 196, 220, 160));
                    link.setStroke(new BasicStroke(0.8f));
                    link.drawArc(-5, -3, 10, 5, 30, 110);
                } else {
                    link.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(10, 8, 16));
                    link.drawLine(-6, 0, 6, 0);
                    link.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(84, 80, 102));
                    link.drawLine(-6, 0, 6, 0);
                }
                if (i % 4 == 2) {  // 룬 빛: 사슬 끝에서 자물쇠 쪽으로 흐른다
                    double toCenter = Math.abs(u - 0.5) * 2;
                    double pulse = 0.35 + 0.65 * Math.pow(0.5 + 0.5 * Math.sin(seconds * 5 + toCenter * 9 + (gold > 0 ? -t * 40 : 0)), 3);
                    Color rc = mix(PURPLE, GOLD, gold, 255 * pulse);
                    link.setPaint(new RadialGradientPaint(0, 0, 7, new float[]{0f, 1f},
                        new Color[]{rc, new Color(rc.getRed(), rc.getGreen(), rc.getBlue(), 0)}));
                    link.fillOval(-7, -7, 14, 14);
                    link.setColor(new Color(255, 245, 255, (int) (220 * pulse)));
                    link.fillOval(-1, -1, 3, 3);
                }
                link.dispose();
            }
        }
        g.dispose();
    }

    /** 잠겨 있을 때 몇 초마다 자물쇠에서 사슬 끝으로 튀는 보랏빛 번개. */
    private static void lightning(Graphics2D source, Rectangle b, int cx, int cy, float s, double seconds) {
        double period = 2.4, local = seconds % period;
        if (local > 0.2) return;
        int n = (int) (seconds / period);
        double alpha = 1 - local / 0.2;
        double[][] targets = {{0.06, 0.22}, {0.94, 0.2}, {0.08, 0.76}, {0.94, 0.74}};
        double[] target = targets[(int) (rnd(n, 3) * 4) % 4];
        double tx = b.x + b.width * target[0], ty = b.y + b.height * target[1];
        Path2D bolt = new Path2D.Double();
        bolt.moveTo(cx, cy);
        int steps = 9;
        for (int i = 1; i <= steps; i++) {
            double u = i / (double) steps;
            double jitter = i == steps ? 0 : (rnd(n * 31 + i, 5) - 0.5) * 26 * s;
            double nx = -(ty - cy), ny = tx - cx, nl = Math.hypot(nx, ny);
            bolt.lineTo(cx + (tx - cx) * u + nx / nl * jitter, cy + (ty - cy) * u + ny / nl * jitter);
        }
        Graphics2D g = (Graphics2D) source.create();
        glowStroke(g, bolt, new Color(196, 120, 255, (int) (230 * alpha)), 2.2f * s);
        g.setColor(new Color(250, 236, 255, (int) (255 * alpha)));
        g.setStroke(new BasicStroke(0.9f * s));
        g.draw(bolt);
        g.dispose();
    }

    /** 검은 쇠 자물쇠. 풀릴 때 점점 세게 떨리고 금빛 균열이 번진 뒤(0~0.4) 조각나 흩어진다(0.4~0.85). */
    private static void lock(Graphics2D source, int cx, int cy, float k, double seconds, double t, double gold) {
        double crack = phase(t, 0.12, 0.38), shatter = phase(t, 0.4, 0.85);
        if (shatter >= 1) return;
        Graphics2D g = (Graphics2D) source.create();
        float glow = (float) (0.55 + 0.45 * Math.sin(seconds * 3));
        double shake = t > 0 && t < 0.4 ? Math.sin(t * 260) * 0.03 * (1 + 4 * t / 0.4) : 0;
        float haloR = (float) ((48 + 30 * crack) * k / 1.3);
        if (shatter <= 0) {  // 뒤쪽 후광: 보라 → 금
            g.setPaint(new RadialGradientPaint(cx, cy + 6 * k, haloR, new float[]{0f, 1f},
                new Color[]{mix(new Color(120, 40, 190), GOLD, gold, 130 * glow + 120 * crack), new Color(30, 0, 60, 0)}));
            g.fill(new Ellipse2D.Float(cx - haloR, cy + 6 * k - haloR, haloR * 2, haloR * 2));
        }
        g.translate(cx, cy);
        g.rotate(shake);
        g.scale(k, k);
        if (shatter > 0) {
            shards(g, shatter);
            g.dispose();
            return;
        }
        Graphics2D shackle = (Graphics2D) g.create();
        shackle.translate(14, -10);
        shackle.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        shackle.setColor(new Color(10, 8, 14));
        shackle.drawArc(-28, -26, 28, 44, 0, 180);
        shackle.drawLine(-28, -4, -28, 4);
        shackle.drawLine(0, -4, 0, 10);
        shackle.setColor(new Color(84, 82, 100));
        shackle.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        shackle.drawArc(-28, -26, 28, 44, 0, 180);
        shackle.dispose();
        body(g);
        // 열쇠 구멍 빛: 보라 → 금
        Color key = mix(new Color(224, 160, 255), new Color(255, 236, 160), gold, 240 * Math.max(glow, gold));
        g.setPaint(new RadialGradientPaint(0, 7, 15, new float[]{0f, 1f}, new Color[]{key, new Color(key.getRed(), key.getGreen(), key.getBlue(), 0)}));
        g.fillOval(-15, -8, 30, 30);
        g.setColor(new Color(6, 2, 12));
        g.fillOval(-5, 0, 10, 10);
        g.fillRect(-2, 8, 4, 11);
        g.setColor(key);
        g.fillOval(-2, 3, 4, 4);
        if (crack > 0) {  // 열쇠 구멍에서 바깥으로 번지는 금빛 균열
            for (int i = 0; i < 7; i++) {
                double a = i * 2 * Math.PI / 7 + rnd(i, 9) * 0.6;
                Path2D p = new Path2D.Double();
                double x = 0, y = 7;
                p.moveTo(x, y);
                int segs = 4;
                for (int j = 1; j <= Math.ceil(segs * crack); j++) {
                    double len = 7 + 4 * rnd(i, j), bend = (rnd(i, j + 20) - 0.5) * 0.9;
                    x += Math.cos(a + bend) * len;
                    y += Math.sin(a + bend) * len * 0.8;
                    p.lineTo(Math.max(-25, Math.min(25, x)), Math.max(-11, Math.min(29, y)));
                }
                glowStroke(g, p, new Color(255, 226, 140, 240), 1.4f);
                g.setColor(new Color(255, 255, 240));
                g.setStroke(new BasicStroke(0.6f));
                g.draw(p);
            }
        }
        g.dispose();
    }

    private static void body(Graphics2D g) {
        g.setPaint(new GradientPaint(0, -12, new Color(74, 68, 88), 0, 30, new Color(22, 18, 30)));
        g.fillRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(6, 4, 10));
        g.setStroke(new BasicStroke(2.6f));
        g.drawRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(150, 146, 166));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(-22, -8, 22, -8);
        for (int[] rivet : new int[][]{{-20, -4}, {20, -4}, {-20, 24}, {20, 24}}) g.fillOval(rivet[0] - 2, rivet[1] - 2, 4, 4);
    }

    /** 자물쇠 몸통을 열쇠 구멍 중심의 부채꼴 조각으로 나눠 바깥으로 날리고, 고리는 위로 튕겨 돈다. */
    private static void shards(Graphics2D g, double f) {
        double move = easeOut(f);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1 - f)));
        double[][] rim = {{-26, -12}, {-9, -12}, {9, -12}, {26, -12}, {26, 9}, {26, 30}, {9, 30}, {-9, 30}, {-26, 30}, {-26, 9}};
        for (int i = 0; i < rim.length; i++) {
            double[] a = rim[i], b = rim[(i + 1) % rim.length];
            double mx = (a[0] + b[0]) / 3, my = (a[1] + b[1] + 7) / 3, d = Math.max(1, Math.hypot(mx, my - 7));
            Graphics2D piece = (Graphics2D) g.create();
            piece.translate(mx / d * (60 + 50 * rnd(i, 11)) * move, (my - 7) / d * (60 + 50 * rnd(i, 12)) * move + 70 * f * f);
            piece.rotate((rnd(i, 13) - 0.5) * 8 * move, mx, my);
            Path2D p = new Path2D.Double();
            p.moveTo(0, 7);
            p.lineTo(a[0], a[1]);
            p.lineTo(b[0], b[1]);
            p.closePath();
            piece.setPaint(new GradientPaint(0, -12, new Color(74, 68, 88), 0, 30, new Color(22, 18, 30)));
            piece.fill(p);
            piece.setColor(new Color(255, 220, 130, 220));  // 깨진 면이 금빛으로 빛난다
            piece.setStroke(new BasicStroke(1.4f));
            piece.draw(p);
            piece.dispose();
        }
        Graphics2D shackle = (Graphics2D) g.create();
        shackle.translate(14 + 30 * move, -10 - 70 * move + 60 * f * f);
        shackle.rotate(-5 * move);
        shackle.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        shackle.setColor(new Color(84, 82, 100));
        shackle.drawArc(-28, -26, 28, 44, 0, 180);
        shackle.dispose();
    }

    /** 섬광, 충격파, 빛줄기, 반짝이(0.38~1). */
    private static void burst(Graphics2D source, Rectangle b, int cx, int cy, float s, double t) {
        if (t < 0.36) return;
        Graphics2D g = (Graphics2D) source.create();
        double flash = t < 0.42 ? phase(t, 0.36, 0.42) : 1 - phase(t, 0.42, 0.6);
        if (flash > 0) {
            float r = 230 * s;
            g.setPaint(new RadialGradientPaint(cx, cy, r, new float[]{0f, 0.35f, 1f},
                new Color[]{new Color(255, 255, 245, (int) (250 * flash)), new Color(255, 236, 170, (int) (150 * flash)), new Color(255, 220, 120, 0)}));
            g.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }
        double rays = phase(t, 0.4, 0.9);
        if (rays > 0 && rays < 1) {  // 천천히 도는 빛줄기
            Graphics2D r = (Graphics2D) g.create();
            r.translate(cx, cy);
            r.rotate(t * 1.5);
            float len = (float) ((90 + 130 * easeOut(rays)) * s);
            int alpha = (int) (170 * Math.sin(Math.PI * rays));
            for (int i = 0; i < 12; i++) {
                r.rotate(Math.PI / 6);
                Path2D ray = new Path2D.Double();
                double wd = (i % 2 == 0 ? 0.09 : 0.05);
                ray.moveTo(0, 0);
                ray.lineTo(len * Math.cos(-wd), len * Math.sin(-wd));
                ray.lineTo(len * Math.cos(wd), len * Math.sin(wd));
                ray.closePath();
                r.setPaint(new GradientPaint(0, 0, new Color(255, 236, 170, alpha), len, 0, new Color(255, 220, 120, 0)));
                r.fill(ray);
            }
            r.dispose();
        }
        for (int k = 0; k < 3; k++) {  // 충격파 세 겹(마지막은 바닥에 눕는다)
            double f = phase(t, 0.4 + k * 0.07, 0.8 + k * 0.07);
            if (f <= 0 || f >= 1) continue;
            double r = (30 + 230 * easeOut(f)) * s;
            Graphics2D ring = (Graphics2D) g.create();
            ring.translate(cx, k == 2 ? b.y + b.height * 0.56 : cy);
            if (k == 2) ring.scale(1, 0.42);
            glowStroke(ring, new Ellipse2D.Double(-r, -r, r * 2, r * 2), new Color(255, 222, 130, (int) (230 * (1 - f))), (float) (5 * s * (1 - f) + 1));
            ring.dispose();
        }
        double sparkle = phase(t, 0.45, 1);
        for (int i = 0; sparkle > 0 && i < 36; i++) {  // 섬 위로 흩날리는 금빛 반짝이
            double start = rnd(i, 21) * 0.4;
            double life = phase(sparkle, start, start + 0.6);
            if (life <= 0 || life >= 1) continue;
            double x = b.x + b.width * (0.05 + 0.9 * rnd(i, 22)), y = b.y + b.height * (0.1 + 0.8 * rnd(i, 23)) - 30 * s * life;
            double size = (2 + 3 * rnd(i, 24)) * s * Math.sin(Math.PI * life);
            int alpha = (int) (255 * Math.sin(Math.PI * life));
            g.setColor(new Color(255, 236, 160, alpha));
            Path2D star = new Path2D.Double();
            star.moveTo(x, y - size * 2);
            star.lineTo(x + size * 0.4, y - size * 0.4);
            star.lineTo(x + size * 2, y);
            star.lineTo(x + size * 0.4, y + size * 0.4);
            star.lineTo(x, y + size * 2);
            star.lineTo(x - size * 0.4, y + size * 0.4);
            star.lineTo(x - size * 2, y);
            star.lineTo(x - size * 0.4, y - size * 0.4);
            star.closePath();
            g.fill(star);
        }
        g.dispose();
    }

    /** 섬 위쪽 가장자리의 여는 조건 안내. 아래쪽엔 경기대 로고 간판과 마을 이름표가 있다. */
    private static void hint(Graphics2D g, Rectangle b, int cx, float s, String hint) {
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 12f * s));
        int tw = g.getFontMetrics().stringWidth(hint) + Math.round(24 * s), th = Math.round(26 * s), ty = b.y - th / 3;
        UiStyle.round(g, cx - tw / 2, ty, tw, th, th, new Color(24, 10, 40, 230));
        g.setColor(new Color(190, 120, 255, 160));
        g.setStroke(new BasicStroke(1.2f * s));
        g.drawRoundRect(cx - tw / 2, ty, tw, th, th, th);
        g.setColor(new Color(236, 220, 255));
        UiStyle.center(g, hint, cx, ty + th / 2 + g.getFontMetrics().getAscent() / 2 - Math.round(2 * s));
    }
}
