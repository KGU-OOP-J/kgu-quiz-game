package org.kgu.collection.view;

import org.kgu.window.UiStyle;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * 잠긴 마을(경기대) 봉인 연출. 게임 이펙트처럼 보이도록 빛나는 것은 지도와 함께 '빛 레이어'에도 그리고,
 * 빛 레이어를 두 크기로 흐리게 해 지도에 더한다(블룸). 입자는 시간만으로 위치가 정해져 상태가 없다.
 * <p>
 * 잠김: 어둠·보랏빛 연기·에너지 돔·땅의 봉인 고리·룬 사슬·번개·검은 자물쇠.
 * 풀림(t 0~1, {@link #DURATION}초): 에너지가 자물쇠로 빨려 듦(0~0.31) → 섬광·화면 흔들림과 함께 자물쇠와 돔이 깨짐,
 * 충격파·빛기둥·불꽃(0.31~0.8) → 섬이 금빛으로 빛나며 어둠이 걷힘(0.38~1).
 */
final class LockArt {
    static final double DURATION = 3.2;
    private static final double IMPACT = 0.31;
    private static final Color PURPLE = new Color(176, 92, 255), GOLD = new Color(255, 206, 96), WHITE_GOLD = new Color(255, 246, 214);

    private BufferedImage canvas, glow;
    private int[] canvasPx, glowPx, tmp, blurA, blurB;

    /** 지도 한 장(물결 반영)을 작업 화면에 복사하고 그 위에 그릴 Graphics를 준다. */
    Graphics2D begin(BufferedImage frame) {
        int w = frame.getWidth(), h = frame.getHeight();
        if (canvas == null || canvas.getWidth() != w || canvas.getHeight() != h) {
            canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            canvasPx = ((DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
            glow = new BufferedImage(w / 2, h / 2, BufferedImage.TYPE_INT_ARGB_PRE);
            glowPx = ((DataBufferInt) glow.getRaster().getDataBuffer()).getData();
            tmp = new int[glowPx.length];
            blurA = new int[glowPx.length];
            blurB = new int[glowPx.length];
        }
        System.arraycopy(((DataBufferInt) frame.getRaster().getDataBuffer()).getData(), 0, canvasPx, 0, canvasPx.length);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }

    /**
     * begin으로 받은 cg 위에 봉인을 그리고, 빛을 더한 뒤 결과를 out에 그린다. cg는 여기서 닫는다.
     *
     * @param t 0이면 잠김, 0~1이면 풀리는 중
     */
    void paint(Graphics2D out, Graphics2D cg, WaveMap map, int v, float s, double seconds, double t, String hint) {
        Arrays.fill(glowPx, 0);
        Graphics2D gg = glow.createGraphics();
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gg.scale(0.5, 0.5);
        Graphics2D[] both = {cg, gg};

        Rectangle b = map.villageBounds(v);
        Scene sc = new Scene(b, s, seconds, t);
        map.dimVillage(cg, v, (float) sc.dark);
        map.tintVillage(cg, v, new Color(58, 14, 92), (float) (sc.dark * (0.42 + 0.08 * Math.sin(seconds * 2.2))));
        smoke(cg, sc);
        for (Graphics2D g : both) groundSeal(g, sc);
        domeFill(cg, sc);
        chains(cg, gg, sc);
        for (Graphics2D g : both) {
            domeRim(g, sc);
            embers(g, sc);
            if (t <= 0) lightning(g, sc);
            charge(g, sc);
        }
        lock(cg, gg, sc);
        flash(cg, gg, sc);
        for (Graphics2D g : both) {
            shockwaves(g, sc);
            pillar(g, sc);
            domeShards(g, sc);
            sparks(g, sc);
            motes(g, sc);
        }
        double shine = phase(t, IMPACT, 0.9);
        if (shine > 0 && shine < 1) map.tintVillage(gg, v, GOLD, (float) (0.22 * Math.sin(Math.PI * shine)));
        gg.dispose();

        bloom(b, s);
        if (t <= 0) hint(cg, sc, hint);
        cg.dispose();

        // 깨지는 순간 화면이 짧게 흔들린다
        double quake = phase(t, IMPACT, IMPACT + 0.12);
        if (quake > 0 && quake < 1) {
            double amp = 7 * s * (1 - quake);
            int dx = (int) Math.round(Math.sin(t * 900) * amp), dy = (int) Math.round(Math.cos(t * 700) * amp);
            int m = Math.round(8 * s);
            out.drawImage(canvas, -m + dx, -m + dy, canvas.getWidth() + 2 * m, canvas.getHeight() + 2 * m, null);
        } else {
            out.drawImage(canvas, 0, 0, null);
        }
    }

    /** 한 프레임에서 여러 효과가 함께 쓰는 값. */
    private static final class Scene {
        final Rectangle b;
        final float s;
        final double seconds, t, dark, gold, cx, cy, gx, gy, rx, ry, domeH, tau;

        Scene(Rectangle b, float s, double seconds, double t) {
            this.b = b;
            this.s = s;
            this.seconds = seconds;
            this.t = t;
            dark = 1 - phase(t, 0.38, 0.9);
            gold = phase(t, 0.05, 0.28);
            cx = b.x + b.width / 2.0;
            cy = b.y + b.height * 0.42;
            gx = cx;
            gy = b.y + b.height * 0.6;
            rx = b.width * 0.5;
            ry = rx * 0.38;
            domeH = b.height * 0.74;
            tau = (t - IMPACT) * DURATION;  // 깨진 뒤 흐른 초
        }
    }

    private static double phase(double t, double from, double to) {
        return Math.max(0, Math.min(1, (t - from) / (to - from)));
    }

    private static double easeOut(double x) {
        return 1 - Math.pow(1 - x, 3);
    }

    /** 같은 (i, k)에는 늘 같은 0~1 값. */
    private static double rnd(int i, int k) {
        double x = Math.sin(i * 127.1 + k * 311.7) * 43758.5453;
        return x - Math.floor(x);
    }

    private static Color mix(Color a, Color b, double f, double alpha) {
        f = Math.max(0, Math.min(1, f));
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * f), (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * f),
            (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * f), (int) Math.max(0, Math.min(255, alpha)));
    }

    private static Color alpha(Color c, double a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) Math.max(0, Math.min(255, a)));
    }

    // ── 잠김 ─────────────────────────────────────────────

    /** 섬 위를 낮게 떠도는 검보랏빛 연기(빛나지 않는다). */
    private static void smoke(Graphics2D source, Scene sc) {
        if (sc.dark <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        for (int i = 0; i < 12; i++) {
            double life = (sc.seconds * 0.22 + rnd(i, 1)) % 1;
            double x = sc.b.x + sc.b.width * (0.08 + 0.84 * rnd(i, 2)) + Math.sin(sc.seconds * 0.9 + i) * 12 * sc.s;
            double y = sc.gy - sc.domeH * 0.8 * life;
            float r = (float) ((20 + 16 * rnd(i, 3)) * sc.s * (0.6 + life));
            int a = (int) (120 * Math.sin(Math.PI * life) * sc.dark);
            g.setPaint(new RadialGradientPaint((float) x, (float) y, r, new float[]{0f, 1f},
                new Color[]{new Color(30, 6, 48, a), new Color(20, 0, 36, 0)}));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
        g.dispose();
    }

    /** 땅에 비스듬히 누운 봉인 고리. 점선이 돌아간다. */
    private static void groundSeal(Graphics2D source, Scene sc) {
        double a = 1 - phase(sc.t, IMPACT, IMPACT + 0.15);
        if (a <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        g.translate(sc.gx, sc.gy);
        g.scale(1, sc.ry / sc.rx);
        double pulse = 0.7 + 0.3 * Math.sin(sc.seconds * 2.4) + sc.gold * 0.6;
        Color c = mix(PURPLE, GOLD, sc.gold, 200 * a * Math.min(1, pulse));
        for (double k : new double[]{0.82, 0.62}) {
            double r = sc.rx * k;
            float dashLen = (float) (10 * sc.s), gap = (float) (6 * sc.s);
            double period = dashLen + gap, shift = (k > 0.7 ? 1 : -1) * (sc.seconds * 30 + sc.t * 300) * sc.s;
            float phaseShift = (float) (((shift % period) + period) % period);  // 점선 위치는 음수면 안 된다
            g.setColor(c);
            g.setStroke(new BasicStroke(2f * sc.s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10, new float[]{dashLen, gap}, phaseShift));
            g.draw(new Ellipse2D.Double(-r, -r, r * 2, r * 2));
        }
        g.setColor(alpha(c, c.getAlpha() * 0.6));
        g.setStroke(new BasicStroke(1.2f * sc.s));
        g.draw(new Ellipse2D.Double(-sc.rx * 0.92, -sc.rx * 0.92, sc.rx * 1.84, sc.rx * 1.84));
        g.dispose();
    }

    private static double domeAlpha(Scene sc) {
        double a = 1 - phase(sc.t, IMPACT, IMPACT + 0.02);
        if (sc.t > 0 && sc.t < IMPACT) a *= 0.65 + 0.35 * Math.sin(sc.t * 140) * Math.sin(sc.t * 57);  // 충전 중 깜빡임
        return a;
    }

    private static Path2D domeShape(Scene sc) {
        Path2D p = new Path2D.Double();
        p.append(new Arc2D.Double(sc.gx - sc.rx, sc.gy - sc.domeH, sc.rx * 2, sc.domeH * 2, 0, 180, Arc2D.OPEN), false);
        p.append(new Arc2D.Double(sc.gx - sc.rx, sc.gy - sc.ry, sc.rx * 2, sc.ry * 2, 180, 180, Arc2D.OPEN), true);
        p.closePath();
        return p;
    }

    /** 에너지 돔 안쪽: 가장자리로 갈수록 짙어지는 보랏빛(빛나지 않는 면). */
    private static void domeFill(Graphics2D source, Scene sc) {
        double a = domeAlpha(sc);
        if (a <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        float r = (float) (sc.rx * 1.08);
        g.setPaint(new RadialGradientPaint((float) sc.gx, (float) (sc.gy - sc.domeH * 0.35), r, new float[]{0f, 0.7f, 1f},
            new Color[]{alpha(PURPLE, 8 * a), alpha(PURPLE, 34 * a), alpha(new Color(210, 150, 255), 90 * a)}));
        g.fill(domeShape(sc));
        g.dispose();
    }

    /** 에너지 돔 테두리와 위로 훑고 지나가는 스캔 띠. */
    private static void domeRim(Graphics2D source, Scene sc) {
        double a = domeAlpha(sc);
        if (a <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        Color c = mix(PURPLE, GOLD, sc.gold, 210 * a);
        g.setColor(c);
        g.setStroke(new BasicStroke(2.2f * sc.s));
        g.draw(new Arc2D.Double(sc.gx - sc.rx, sc.gy - sc.domeH, sc.rx * 2, sc.domeH * 2, 0, 180, Arc2D.OPEN));
        g.setColor(alpha(c, 90 * a));
        g.setStroke(new BasicStroke(1.4f * sc.s));
        g.draw(new Arc2D.Double(sc.gx - sc.rx, sc.gy - sc.ry, sc.rx * 2, sc.ry * 2, 180, 180, Arc2D.OPEN));
        g.clip(domeShape(sc));
        double band = (sc.seconds * 0.45) % 1.3 - 0.15;
        double y = sc.gy - sc.domeH * band;
        float hgt = 16 * sc.s;
        g.setPaint(new GradientPaint(0, (float) (y - hgt), alpha(c, 0), 0, (float) y, alpha(c, 70 * a), true));
        g.fill(new Rectangle2DD(sc.gx - sc.rx, y - hgt, sc.rx * 2, hgt * 2));
        g.dispose();
    }

    /** Rectangle2D.Double 줄임. */
    private static final class Rectangle2DD extends java.awt.geom.Rectangle2D.Double {
        Rectangle2DD(double x, double y, double w, double h) {
            super(x, y, w, h);
        }
    }

    /** 섬을 X자로 묶은 쇠사슬(쇠는 지도에만, 룬 빛은 빛 레이어에도). 깨질 때 고리들이 달궈진 채 튕겨 나간다. */
    private static void chains(Graphics2D cg, Graphics2D gg, Scene sc) {
        double snap = phase(sc.t, IMPACT, IMPACT + 0.25);
        if (snap >= 1) return;
        Rectangle b = sc.b;
        double rattle = sc.t > 0 && sc.t < IMPACT ? Math.sin(sc.t * 260) * 2.5 * sc.s * (sc.t / IMPACT) : 0;
        double[][] ends = {
            {b.x + b.width * 0.04, b.y + b.height * 0.2, b.x + b.width * 0.96, b.y + b.height * 0.76},
            {b.x + b.width * 0.96, b.y + b.height * 0.18, b.x + b.width * 0.06, b.y + b.height * 0.78}};
        float k = sc.s * 1.55f;
        double step = 7.6 * k;
        for (int c = 0; c < ends.length; c++) {
            double[] e = ends[c];
            double len = Math.hypot(e[2] - e[0], e[3] - e[1]), angle = Math.atan2(e[3] - e[1], e[2] - e[0]);
            int count = (int) (len / step);
            for (int i = 0; i <= count; i++) {
                double u = i / (double) count;
                double x = e[0] + (e[2] - e[0]) * u, y = e[1] + (e[3] - e[1]) * u + Math.sin(Math.PI * u) * 10 * sc.s + rattle;
                double spin = 0;
                if (snap > 0) {
                    double dx = x - sc.cx, dy = y - sc.cy, d = Math.max(1, Math.hypot(dx, dy));
                    double push = easeOut(snap) * (40 + 60 * rnd(i, c)) * sc.s;
                    x += dx / d * push;
                    y += dy / d * push + 120 * sc.s * snap * snap;
                    spin = (rnd(i, c + 7) - 0.5) * 9 * snap;
                }
                Graphics2D link = (Graphics2D) cg.create();
                link.setComposite(AlphaComposite.SrcOver.derive((float) (1 - snap)));
                link.translate(x, y);
                link.rotate(angle + spin);
                link.scale(k, k);
                if (i % 2 == 0) {
                    link.setStroke(new BasicStroke(4.2f));
                    link.setColor(new Color(10, 8, 16));
                    link.drawOval(-6, -3, 12, 6);
                    link.setStroke(new BasicStroke(2.2f));
                    link.setColor(new Color(108, 104, 128));
                    link.drawOval(-6, -3, 12, 6);
                    link.setColor(new Color(200, 196, 220, 150));
                    link.setStroke(new BasicStroke(0.8f));
                    link.drawArc(-5, -3, 10, 5, 30, 110);
                } else {
                    link.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(10, 8, 16));
                    link.drawLine(-6, 0, 6, 0);
                    link.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(80, 76, 98));
                    link.drawLine(-6, 0, 6, 0);
                }
                link.dispose();
                // 빛: 잠김 땐 룬이 자물쇠 쪽으로 흐르고, 충전 중 금빛으로 달궈지고, 깨지면 고리 전체가 불씨처럼 빛난다
                double toCenter = Math.abs(u - 0.5) * 2;
                double pulse = Math.pow(0.5 + 0.5 * Math.sin(sc.seconds * 5 + toCenter * 9 - sc.t * 60), 3);
                double heat = snap > 0 ? (1 - snap) : (i % 4 == 2 ? 0.3 + 0.7 * pulse : 0) + sc.gold * 0.5;
                if (heat <= 0.02) continue;
                Color hc = mix(PURPLE, GOLD, snap > 0 ? 1 : sc.gold, 255 * Math.min(1, heat));
                for (Graphics2D target : new Graphics2D[]{cg, gg}) {
                    target.setColor(hc);
                    double rr = (i % 4 == 2 || snap > 0 ? 3.2 : 2) * sc.s;
                    target.fill(new Ellipse2D.Double(x - rr, y - rr, rr * 2, rr * 2));
                }
            }
        }
    }

    /** 섬에서 올라오는 작은 불씨. 잠김엔 보라, 충전 중 금빛. */
    private static void embers(Graphics2D g, Scene sc) {
        if (sc.dark <= 0) return;
        for (int i = 0; i < 36; i++) {
            double life = (sc.seconds * 0.3 + rnd(i, 31)) % 1;
            double x = sc.b.x + sc.b.width * (0.08 + 0.84 * rnd(i, 32)) + Math.sin(sc.seconds * 1.7 + i) * 6 * sc.s;
            double y = sc.gy + sc.ry * (rnd(i, 33) - 0.5) - sc.domeH * 0.9 * life;
            double r = (0.7 + 1.4 * rnd(i, 34)) * sc.s * (1 - life * 0.6);
            g.setColor(mix(new Color(206, 130, 255), GOLD, sc.gold, 230 * Math.sin(Math.PI * life) * sc.dark));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
    }

    /** 잠겨 있을 때 몇 초마다 자물쇠에서 돔 가장자리로 튀는 번개. */
    private static void lightning(Graphics2D g, Scene sc) {
        double period = 2.2, local = sc.seconds % period;
        if (local > 0.22) return;
        int n = (int) (sc.seconds / period);
        double a = 1 - local / 0.22;
        double ang = Math.PI * (1.1 + 0.8 * rnd(n, 3));
        double tx = sc.gx + Math.cos(ang) * sc.rx * 0.95, ty = sc.gy + Math.sin(ang) * sc.domeH * 0.9;
        Path2D bolt = new Path2D.Double();
        bolt.moveTo(sc.cx, sc.cy);
        int steps = 10;
        for (int i = 1; i <= steps; i++) {
            double u = i / (double) steps;
            double nx = -(ty - sc.cy), ny = tx - sc.cx, nl = Math.hypot(nx, ny);
            double j = i == steps ? 0 : (rnd(n * 31 + i, 5) - 0.5) * 30 * sc.s;
            bolt.lineTo(sc.cx + (tx - sc.cx) * u + nx / nl * j, sc.cy + (ty - sc.cy) * u + ny / nl * j);
        }
        g.setColor(new Color(214, 160, 255, (int) (255 * a)));
        g.setStroke(new BasicStroke(1.6f * sc.s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(bolt);
    }

    /** 충전: 둘레의 에너지가 꼬리를 끌며 자물쇠로 빨려 든다. */
    private static void charge(Graphics2D g, Scene sc) {
        if (sc.t <= 0 || sc.t >= IMPACT) return;
        for (int i = 0; i < 54; i++) {
            double p = phase(sc.t, rnd(i, 41) * 0.14, IMPACT);
            if (p <= 0 || p >= 1) continue;
            double ang = rnd(i, 42) * Math.PI * 2, r0 = (110 + 170 * rnd(i, 43)) * sc.s;
            double r = r0 * (1 - p * p), rp = r0 * (1 - Math.max(0, p - 0.12) * Math.max(0, p - 0.12));
            double x = sc.cx + Math.cos(ang + p * 1.2) * r, y = sc.cy + Math.sin(ang + p * 1.2) * r * 0.75;
            double px = sc.cx + Math.cos(ang + (p - 0.12) * 1.2) * rp, py = sc.cy + Math.sin(ang + (p - 0.12) * 1.2) * rp * 0.75;
            g.setColor(mix(new Color(200, 120, 255), WHITE_GOLD, p, 255 * Math.min(1, p * 3)));
            g.setStroke(new BasicStroke((float) ((0.8 + 1.4 * p) * sc.s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new java.awt.geom.Line2D.Double(px, py, x, y));
        }
    }

    // ── 자물쇠 ─────────────────────────────────────────────

    private static void lock(Graphics2D cg, Graphics2D gg, Scene sc) {
        double crack = phase(sc.t, 0.1, IMPACT), shatter = phase(sc.t, IMPACT, IMPACT + 0.2);
        if (shatter >= 1) return;
        float k = sc.s * 1.35f;
        float breathe = (float) (0.55 + 0.45 * Math.sin(sc.seconds * 3));
        double shake = sc.t > 0 && sc.t < IMPACT ? Math.sin(sc.t * 300) * 0.035 * (1 + 5 * sc.t / IMPACT) : 0;
        // 자물쇠 뒤의 빛무리(빛 레이어에만): 충전될수록 커지고 금빛이 된다
        if (shatter <= 0) {
            float r = (float) ((30 + 40 * crack) * k);
            gg.setPaint(new RadialGradientPaint((float) sc.cx, (float) (sc.cy + 6 * k), r, new float[]{0f, 1f},
                new Color[]{mix(PURPLE, WHITE_GOLD, sc.gold, 150 * breathe + 105 * crack), alpha(PURPLE, 0)}));
            gg.fill(new Ellipse2D.Float((float) sc.cx - r, (float) (sc.cy + 6 * k) - r, r * 2, r * 2));
        }
        for (Graphics2D target : new Graphics2D[]{cg, gg}) {
            boolean light = target == gg;
            Graphics2D g = (Graphics2D) target.create();
            g.translate(sc.cx, sc.cy);
            g.rotate(shake);
            g.scale(k, k);
            if (shatter > 0) {
                shards(g, shatter, light);
                g.dispose();
                continue;
            }
            if (!light) {
                g.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(10, 8, 14));
                g.drawArc(-14, -36, 28, 44, 0, 180);
                g.drawLine(-14, -14, -14, -6);
                g.drawLine(14, -14, 14, 0);
                g.setColor(new Color(84, 82, 100));
                g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawArc(-14, -36, 28, 44, 0, 180);
                body(g);
            }
            Color key = mix(new Color(214, 150, 255), WHITE_GOLD, sc.gold, 230 * Math.max(breathe, sc.gold));
            g.setColor(new Color(6, 2, 12));
            if (!light) {
                g.fillOval(-5, 0, 10, 10);
                g.fillRect(-2, 8, 4, 11);
            }
            g.setColor(key);
            g.fillOval(-2, 3, 4, 4);
            g.fillRect(-1, 8, 2, 8);
            if (crack > 0) {  // 열쇠 구멍에서 번지는 빛 균열
                for (int i = 0; i < 8; i++) {
                    double a = i * 2 * Math.PI / 8 + rnd(i, 9) * 0.5;
                    Path2D p = new Path2D.Double();
                    double x = 0, y = 7;
                    p.moveTo(x, y);
                    for (int j = 1; j <= Math.ceil(4 * crack); j++) {
                        double len = 6 + 4 * rnd(i, j), bend = (rnd(i, j + 20) - 0.5) * 0.9;
                        x += Math.cos(a + bend) * len;
                        y += Math.sin(a + bend) * len * 0.8;
                        p.lineTo(Math.max(-25, Math.min(25, x)), Math.max(-11, Math.min(29, y)));
                    }
                    g.setColor(alpha(WHITE_GOLD, 255));
                    g.setStroke(new BasicStroke(light ? 2.2f : 1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(p);
                }
            }
            g.dispose();
        }
    }

    private static void body(Graphics2D g) {
        g.setPaint(new GradientPaint(0, -12, new Color(70, 64, 84), 0, 30, new Color(20, 16, 28)));
        g.fillRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(6, 4, 10));
        g.setStroke(new BasicStroke(2.6f));
        g.drawRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(140, 136, 158));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(-22, -8, 22, -8);
        for (int[] rivet : new int[][]{{-20, -4}, {20, -4}, {-20, 24}, {20, 24}}) g.fillOval(rivet[0] - 2, rivet[1] - 2, 4, 4);
    }

    /** 자물쇠가 열쇠 구멍 중심으로 갈라져 날아간다. 깨진 면은 달궈진 금빛. */
    private static void shards(Graphics2D g, double f, boolean light) {
        double move = easeOut(f);
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1 - f)));
        double[][] rim = {{-26, -12}, {-9, -12}, {9, -12}, {26, -12}, {26, 9}, {26, 30}, {9, 30}, {-9, 30}, {-26, 30}, {-26, 9}};
        for (int i = 0; i < rim.length; i++) {
            double[] a = rim[i], b = rim[(i + 1) % rim.length];
            double mx = (a[0] + b[0]) / 3, my = (a[1] + b[1] + 7) / 3, d = Math.max(1, Math.hypot(mx, my - 7));
            Graphics2D piece = (Graphics2D) g.create();
            piece.translate(mx / d * (36 + 36 * rnd(i, 11)) * move, (my - 7) / d * (36 + 36 * rnd(i, 12)) * move + 40 * f * f);
            piece.rotate((rnd(i, 13) - 0.5) * 9 * move, mx, my);
            Path2D p = new Path2D.Double();
            p.moveTo(0, 7);
            p.lineTo(a[0], a[1]);
            p.lineTo(b[0], b[1]);
            p.closePath();
            if (!light) {
                piece.setPaint(new GradientPaint(0, -12, new Color(70, 64, 84), 0, 30, new Color(20, 16, 28)));
                piece.fill(p);
            }
            piece.setColor(alpha(GOLD, 255 * (1 - f * 0.5)));
            piece.setStroke(new BasicStroke(light ? 2.6f : 1.3f));
            piece.draw(p);
            piece.dispose();
        }
    }

    // ── 깨짐 ─────────────────────────────────────────────

    /** 깨지는 순간의 흰 섬광. 빛 레이어에 크게 그려 블룸으로 화면이 하얗게 번진다. */
    private static void flash(Graphics2D cg, Graphics2D gg, Scene sc) {
        if (sc.t < IMPACT - 0.02) return;
        double f = phase(sc.t, IMPACT - 0.02, IMPACT + 0.1);
        if (f >= 1) return;
        double a = sc.t < IMPACT ? phase(sc.t, IMPACT - 0.02, IMPACT) : Math.pow(1 - f, 2);
        float r = (float) ((40 + 110 * easeOut(f)) * sc.s);
        for (Graphics2D g : new Graphics2D[]{cg, gg}) {
            float rr = g == gg ? r * 0.9f : r;
            g.setPaint(new RadialGradientPaint((float) sc.cx, (float) sc.cy, rr, new float[]{0f, 0.4f, 1f},
                new Color[]{alpha(Color.WHITE, 255 * a), alpha(WHITE_GOLD, 170 * a), alpha(GOLD, 0)}));
            g.fill(new Ellipse2D.Float((float) sc.cx - rr, (float) sc.cy - rr, rr * 2, rr * 2));
        }
    }

    /** 공중 충격파 두 겹과 땅을 훑는 충격파 하나. */
    private static void shockwaves(Graphics2D source, Scene sc) {
        for (int k = 0; k < 3; k++) {
            double f = phase(sc.t, IMPACT + k * 0.04, IMPACT + 0.3 + k * 0.06);
            if (f <= 0 || f >= 1) continue;
            Graphics2D g = (Graphics2D) source.create();
            double r = (24 + (k == 2 ? 380 : 300) * easeOut(f)) * sc.s;
            g.translate(sc.cx, k == 2 ? sc.gy : sc.cy);
            if (k == 2) g.scale(1, sc.ry / sc.rx);
            g.setColor(mix(WHITE_GOLD, GOLD, f, 240 * (1 - f)));
            g.setStroke(new BasicStroke((float) ((k == 0 ? 9 : 5) * sc.s * (1 - f) + 0.8)));
            g.draw(new Ellipse2D.Double(-r, -r, r * 2, r * 2));
            g.dispose();
        }
    }

    /** 섬에서 하늘로 솟구치는 빛기둥. 빠르게 뻗고 천천히 가늘어진다. */
    private static void pillar(Graphics2D g, Scene sc) {
        double f = phase(sc.t, IMPACT, 0.82);
        if (f <= 0 || f >= 1) return;
        double reach = easeOut(phase(sc.t, IMPACT, IMPACT + 0.08));
        double top = sc.cy - (sc.cy + 40 * sc.s) * reach, bottom = sc.gy;
        double half = (38 * sc.s) * Math.pow(1 - f, 0.6) + 1;
        double a = Math.pow(1 - f, 1.3);
        g.setPaint(new LinearGradientPaint((float) (sc.cx - half), 0, (float) (sc.cx + half), 0, new float[]{0f, 0.35f, 0.5f, 0.65f, 1f},
            new Color[]{alpha(GOLD, 0), alpha(GOLD, 170 * a), alpha(Color.WHITE, 255 * a), alpha(GOLD, 170 * a), alpha(GOLD, 0)}));
        g.fill(new Rectangle2DD(sc.cx - half, top, half * 2, bottom - top));
    }

    /** 돔 테두리가 조각나 바깥으로 흩어진다. */
    private static void domeShards(Graphics2D source, Scene sc) {
        double f = phase(sc.t, IMPACT, IMPACT + 0.32);
        if (f <= 0 || f >= 1) return;
        Graphics2D g = (Graphics2D) source.create();
        g.setColor(mix(WHITE_GOLD, new Color(200, 140, 255), f, 230 * (1 - f)));
        g.setStroke(new BasicStroke((float) (2.4 * sc.s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int pieces = 18;
        for (int i = 0; i < pieces; i++) {
            double a0 = Math.PI * i / pieces, a1 = Math.PI * (i + 0.8) / pieces, am = (a0 + a1) / 2;
            double push = easeOut(f) * (60 + 80 * rnd(i, 51)) * sc.s, fall = 90 * sc.s * f * f;
            double ox = Math.cos(am) * push, oy = -Math.sin(am) * push + fall;
            Path2D p = new Path2D.Double();
            for (int j = 0; j <= 4; j++) {
                double a = a0 + (a1 - a0) * j / 4;
                double x = sc.gx + Math.cos(a) * sc.rx + ox, y = sc.gy - Math.sin(a) * sc.domeH + oy;
                if (j == 0) p.moveTo(x, y);
                else p.lineTo(x, y);
            }
            g.draw(p);
        }
        g.dispose();
    }

    /** 깨질 때 튀는 불꽃. 공기 저항으로 느려지고 중력으로 떨어지며 꼬리를 남긴다. */
    private static void sparks(Graphics2D g, Scene sc) {
        if (sc.tau <= 0) return;
        double drag = 3.2, grav = 260 * sc.s;
        for (int i = 0; i < 80; i++) {
            double life = 0.5 + 0.9 * rnd(i, 61), u = sc.tau / life;
            if (u >= 1) continue;
            double ang = rnd(i, 62) * Math.PI * 2, speed = (260 + 620 * rnd(i, 63)) * sc.s;
            double[] now = sparkAt(sc, ang, speed, drag, grav, sc.tau), prev = sparkAt(sc, ang, speed, drag, grav, Math.max(0, sc.tau - 0.045));
            g.setColor(mix(WHITE_GOLD, new Color(255, 140, 40), u, 255 * (1 - u * u)));
            g.setStroke(new BasicStroke((float) ((2.2 * (1 - u) + 0.5) * sc.s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new java.awt.geom.Line2D.Double(prev[0], prev[1], now[0], now[1]));
        }
    }

    private static double[] sparkAt(Scene sc, double ang, double speed, double drag, double grav, double time) {
        double dist = speed / drag * (1 - Math.exp(-drag * time));
        return new double[]{sc.cx + Math.cos(ang) * dist, sc.cy + Math.sin(ang) * dist * 0.75 + 0.5 * grav * time * time};
    }

    /** 어둠이 걷힌 뒤 섬에서 천천히 떠오르는 금빛 입자. */
    private static void motes(Graphics2D g, Scene sc) {
        double f = phase(sc.t, 0.42, 1);
        if (f <= 0 || f >= 1) return;
        for (int i = 0; i < 40; i++) {
            double start = rnd(i, 71) * 0.5, life = phase(f, start, start + 0.5);
            if (life <= 0 || life >= 1) continue;
            double x = sc.b.x + sc.b.width * (0.06 + 0.88 * rnd(i, 72)) + Math.sin(life * 6 + i) * 5 * sc.s;
            double y = sc.gy + sc.ry * (rnd(i, 73) - 0.6) - 60 * sc.s * life;
            double r = (0.8 + 1.6 * rnd(i, 74)) * sc.s;
            g.setColor(alpha(WHITE_GOLD, 255 * Math.sin(Math.PI * life)));
            g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
    }

    // ── 블룸 ─────────────────────────────────────────────

    /** 빛 레이어를 작게·크게 두 번 흐려 지도에 더한다(가산 합성). 섬 주변만 계산한다. */
    private void bloom(Rectangle b, float s) {
        int gw = glow.getWidth(), gh = glow.getHeight();
        int pad = Math.round(360 * s / 2);
        int x0 = Math.max(0, b.x / 2 - pad), x1 = Math.min(gw, (b.x + b.width) / 2 + pad);
        int y0 = 0, y1 = Math.min(gh, (b.y + b.height) / 2 + pad);
        blur(glowPx, blurA, gw, x0, x1, y0, y1, Math.max(2, Math.round(3 * s)));
        blur(glowPx, blurB, gw, x0, x1, y0, y1, Math.max(6, Math.round(11 * s)));
        int cw = canvas.getWidth();
        for (int y = y0 * 2; y < Math.min(canvas.getHeight(), y1 * 2); y++) {
            int row = (y >> 1) * gw, crow = y * cw;
            for (int x = x0 * 2; x < Math.min(cw, x1 * 2); x++) {
                int a = blurA[row + (x >> 1)], bb = blurB[row + (x >> 1)];
                if ((a | bb) == 0) continue;
                int c = canvasPx[crow + x];
                int r = (c >> 16 & 255) + ((a >> 16 & 255) * 5 >> 2) + (bb >> 16 & 255);
                int gr = (c >> 8 & 255) + ((a >> 8 & 255) * 5 >> 2) + (bb >> 8 & 255);
                int bl = (c & 255) + ((a & 255) * 5 >> 2) + (bb & 255);
                canvasPx[crow + x] = Math.min(255, r) << 16 | Math.min(255, gr) << 8 | Math.min(255, bl);
            }
        }
    }

    /** 상자 흐림을 가로·세로로 두 번씩(가우스 흐림에 가깝다). 알파는 버리고 RGB(미리 곱한 값)만 쓴다. */
    private void blur(int[] src, int[] dst, int w, int x0, int x1, int y0, int y1, int r) {
        boxH(src, tmp, w, x0, x1, y0, y1, r);
        boxV(tmp, dst, w, x0, x1, y0, y1, r);
        boxH(dst, tmp, w, x0, x1, y0, y1, r);
        boxV(tmp, dst, w, x0, x1, y0, y1, r);
    }

    private static void boxH(int[] in, int[] out, int w, int x0, int x1, int y0, int y1, int r) {
        int n = 2 * r + 1;
        for (int y = y0; y < y1; y++) {
            int row = y * w, sr = 0, sg = 0, sb = 0;
            for (int i = -r; i <= r; i++) {
                int p = in[row + Math.min(x1 - 1, Math.max(x0, x0 + i))];
                sr += p >> 16 & 255;
                sg += p >> 8 & 255;
                sb += p & 255;
            }
            for (int x = x0; x < x1; x++) {
                out[row + x] = (sr / n) << 16 | (sg / n) << 8 | sb / n;
                int pOut = in[row + Math.max(x0, x - r)], pIn = in[row + Math.min(x1 - 1, x + r + 1)];
                sr += (pIn >> 16 & 255) - (pOut >> 16 & 255);
                sg += (pIn >> 8 & 255) - (pOut >> 8 & 255);
                sb += (pIn & 255) - (pOut & 255);
            }
        }
    }

    private static void boxV(int[] in, int[] out, int w, int x0, int x1, int y0, int y1, int r) {
        int n = 2 * r + 1;
        for (int x = x0; x < x1; x++) {
            int sr = 0, sg = 0, sb = 0;
            for (int i = -r; i <= r; i++) {
                int p = in[Math.min(y1 - 1, Math.max(y0, y0 + i)) * w + x];
                sr += p >> 16 & 255;
                sg += p >> 8 & 255;
                sb += p & 255;
            }
            for (int y = y0; y < y1; y++) {
                out[y * w + x] = (sr / n) << 16 | (sg / n) << 8 | sb / n;
                int pOut = in[Math.max(y0, y - r) * w + x], pIn = in[Math.min(y1 - 1, y + r + 1) * w + x];
                sr += (pIn >> 16 & 255) - (pOut >> 16 & 255);
                sg += (pIn >> 8 & 255) - (pOut >> 8 & 255);
                sb += (pIn & 255) - (pOut & 255);
            }
        }
    }

    /** 섬 위쪽 가장자리의 여는 조건 안내(블룸 뒤에 그려 또렷하게). */
    private static void hint(Graphics2D g, Scene sc, String hint) {
        float s = sc.s;
        int cx = (int) sc.cx;
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 12f * s));
        int tw = g.getFontMetrics().stringWidth(hint) + Math.round(24 * s), th = Math.round(26 * s);
        int ty = Math.max(Math.round(6 * s), (int) (sc.gy - sc.domeH) - th - Math.round(4 * s));
        UiStyle.round(g, cx - tw / 2, ty, tw, th, th, new Color(22, 8, 38, 235));
        g.setColor(new Color(190, 120, 255, 170));
        g.setStroke(new BasicStroke(1.2f * s));
        g.drawRoundRect(cx - tw / 2, ty, tw, th, th, th);
        g.setColor(new Color(236, 220, 255));
        UiStyle.center(g, hint, cx, ty + th / 2 + g.getFontMetrics().getAscent() / 2 - Math.round(2 * s));
    }
}
