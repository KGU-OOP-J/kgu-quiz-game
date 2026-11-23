package org.kgu.collection.view;

import org.kgu.model.Person;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public final class CardArt {
    static final Color GOLD = new Color(246, 186, 40);
    static final Color BLUE = new Color(30, 116, 236);

    private final Function<String, Optional<BufferedImage>> images;
    private final Map<String, Image> scaledImages = new HashMap<>();

    public CardArt(Function<String, Optional<BufferedImage>> images) {
        this.images = images;
    }

    static UiButton button(String text, Color color, float fontSize, Runnable action) {
        UiButton button = new UiButton(text, color);
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, fontSize));
        button.setForeground(color.equals(Color.WHITE) ? UiStyle.NAVY : Color.WHITE);
        button.addActionListener(e -> action.run());
        return button;
    }

    /**
     * 그림을 비율을 유지한 채 영역을 꽉 채우도록 가운데를 잘라 그린다.
     */
    private static void drawCover(Graphics2D g, Image img, int x, int y, int w, int h) {
        int iw = img.getWidth(null), ih = img.getHeight(null);
        double scale = Math.max((double) w / iw, (double) h / ih);
        int sw = (int) (w / scale), sh = (int) (h / scale);
        int sx = (iw - sw) / 2, sy = (ih - sh) / 2;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, x + w, y + h, sx, sy, sx + sw, sy + sh, null);
    }

    static void shadow(Graphics2D g, int x, int y, int w, int h, int arc) {
        for (int i = 4; i >= 1; i--) {
            g.setColor(new Color(40, 70, 120, 10));
            g.fillRoundRect(x - i, y - i + 3, w + i * 2, h + i * 2, arc + i * 2, arc + i * 2);
        }
    }

    static void panel(Graphics2D g, int x, int y, int w, int h, int arc, Color fill) {
        shadow(g, x, y, w, h, arc);
        UiStyle.round(g, x, y, w, h, arc, fill);
    }

    static void gradientRound(Graphics2D g, int x, int y, int w, int h, int arc, Color top, Color bottom) {
        g.setPaint(new GradientPaint(0, y, top, 0, y + h, bottom));
        g.fillRoundRect(x, y, w, h, arc, arc);
    }

    static Color fieldColor(String field) {
        return switch (field) {
            case "과학자" -> new Color(46, 112, 226);
            case "교수님" -> new Color(20, 140, 110);
            case "한국사" -> new Color(226, 112, 40);
            case "아이돌" -> new Color(140, 84, 204);
            default -> UiStyle.NAVY;
        };
    }

    /**
     * 이름을 가운데 정렬로 쓴다. 긴 이름(니콜라우스 코페르니쿠스 등)은 maxW 안에 들어가게 글자를 줄인다.
     */
    private static void drawName(Graphics2D g, String name, int cx, int baseline, int maxW, float s) {
        Font font = UiStyle.FONT.deriveFont(Font.BOLD, 12.5f * s);
        int width = g.getFontMetrics(font).stringWidth(name);
        if (width > maxW) font = font.deriveFont(font.getSize2D() * maxW / width);
        g.setFont(font);
        UiStyle.center(g, name, cx, baseline);
    }

    private static void drawLockedCard(Graphics2D g, String name, int x, int y, int w, int h) {
        float s = w / 100f;
        int arc = Math.round(14 * s), pad = Math.round(4 * s);
        gradientRound(g, x, y, w, h, arc, new Color(222, 229, 240), new Color(196, 206, 222));
        g.setColor(new Color(240, 244, 250));
        g.setStroke(new BasicStroke(2f * s));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        Shape oldClip = g.getClip();
        g.clip(new RoundRectangle2D.Float(x + pad, y + pad, w - pad * 2, h - pad * 2, arc - pad, arc - pad));
        drawSilhouette(g, x + pad, y + pad, w - pad * 2, h - pad * 2, new Color(168, 180, 200));
        g.setClip(oldClip);
        drawLock(g, x + w / 2, y + h / 2, s);
        // 잠긴 카드도 이름은 보여 준다. 위치는 획득 카드의 이름 띠와 같다.
        g.setColor(new Color(90, 104, 128));
        drawName(g, name, x + w / 2, y + h - Math.round(20 * s) + Math.round(4.5f * s), w - Math.round(18 * s), s);
    }

    private static void drawSilhouette(Graphics2D g, int x, int y, int w, int h, Color color) {
        g.setColor(color);
        int head = w * 2 / 5;
        g.fillOval(x + (w - head) / 2, y + h / 6, head, head);
        g.fillRoundRect(x + w / 6, y + h / 6 + head + Math.max(3, w / 30), w * 2 / 3, h, w / 2, w / 2);
    }

    private static void drawLock(Graphics2D g, int cx, int cy, float s) {
        int bw = Math.round(24 * s), bh = Math.round(19 * s);
        g.setColor(new Color(248, 250, 253));
        g.setStroke(new BasicStroke(4f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - bw / 3, cy - bh, bw * 2 / 3, bh * 3 / 2, 0, 180);
        g.fillRoundRect(cx - bw / 2, cy - bh / 4, bw, bh, Math.round(6 * s), Math.round(6 * s));
        g.setColor(new Color(150, 162, 182));
        g.fillOval(cx - Math.round(2.5f * s), cy + bh / 6, Math.round(5 * s), Math.round(5 * s));
        g.fillRect(cx - Math.round(s), cy + bh / 6 + Math.round(3 * s), Math.round(2 * s), Math.round(5 * s));
    }

    static void drawStar(Graphics2D g, float cx, float cy, float r, Color color) {
        Path2D star = new Path2D.Float();
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 5 * i - Math.PI / 2;
            double radius = i % 2 == 0 ? r : r * 0.45;
            float px = (float) (cx + Math.cos(angle) * radius), py = (float) (cy + Math.sin(angle) * radius);
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        g.setColor(color);
        g.fill(star);
        g.setColor(color.equals(GOLD) ? new Color(176, 112, 20) : new Color(190, 198, 210));
        g.setStroke(new BasicStroke(Math.max(1f, r / 7)));
        g.draw(star);
    }

    /**
     * 정보 상자 왼쪽 위에 걸치는 남색 이름표.
     */
    static void drawTab(Graphics2D g, String text, int x, int y) {
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f));
        int w = g.getFontMetrics().stringWidth(text) + 36;
        gradientRound(g, x, y, w, 28, 28, new Color(44, 80, 140), new Color(24, 48, 92));
        g.setColor(Color.WHITE);
        g.drawString(text, x + 18, y + 19);
    }

    /**
     * 맑은 고딕에는 ✓ 글자가 없어서 체크 표시는 선으로 그린다.
     */
    static void drawCheck(Graphics2D g, int x, int y, float size) {
        g.setStroke(new BasicStroke(size / 5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawPolyline(new int[]{x, x + Math.round(size * 0.36f), x + Math.round(size)},
            new int[]{y + Math.round(size * 0.43f), y + Math.round(size * 0.79f), y}, 3);
    }

    static void drawBook(Graphics2D g, int x, int y) {
        int w = 19, h = 30, r = 6;
        g.setColor(new Color(250, 214, 120));
        g.fillRoundRect(x, y, w, h, r, r);
        g.fillRoundRect(x + w, y, w, h, r, r);
        g.setColor(new Color(255, 246, 222));
        g.fillRoundRect(x + 3, y + 3, w - 4, h - 6, 3, 3);
        g.fillRoundRect(x + w + 1, y + 3, w - 4, h - 6, 3, 3);
        g.setColor(new Color(214, 160, 70));
        g.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < 3; i++) {
            int lineY = y + 9 + i * 6;
            g.drawLine(x + 6, lineY, x + 15, lineY);
            g.drawLine(x + w + 4, lineY, x + w + 13, lineY);
        }
    }

    private Optional<BufferedImage> image(String key) {
        return images.apply(key);
    }

    boolean drawIcon(Graphics2D g, String key, int x, int y, int w, int h) {
        Optional<BufferedImage> found = image(key);
        if (found.isEmpty()) return false;
        Image icon = found.get();
        // 큰 그림을 바로 줄이면 가장자리가 거칠어져서, 화면 배율(1.8)을 감당할 크기로 미리 줄여 둔다.
        Image scaled = scaledImages.computeIfAbsent(key + "@" + w + "x" + h,
            k -> new ImageIcon(icon.getScaledInstance(w * 2, h * 2, Image.SCALE_AREA_AVERAGING)).getImage());
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(scaled, x, y, w, h, null);
        return true;
    }

    private WaveMap waveMap;

    private Optional<WaveMap> waveMap(int w, int h) {
        if (waveMap == null) {
            image("ui/map").ifPresent(map -> waveMap = new WaveMap(map, w, h));
        }
        return Optional.ofNullable(waveMap);
    }

    /** 지도 그림이 없으면 마을을 고를 수 없으니 -1. */
    int villageAt(int x, int y, int w, int h) {
        return waveMap(w, h).map(map -> map.villageAt(x, y)).orElse(-1);
    }

    /**
     * @param lockedVillage 자물쇠를 그릴 마을 번호(없으면 -1)
     * @param unlock 0이면 잠김, 0~1이면 자물쇠가 풀리는 중, 1이면 다 열림
     * @param hint 잠겨 있을 때 섬 위쪽에 보여 줄 여는 조건
     */
    void paintMap(Graphics2D g, int w, int h, double seconds, double[] villageScales, int lockedVillage, double unlock, String hint) {
        Optional<WaveMap> map = waveMap(w, h);
        if (map.isEmpty()) {
            paintBackground(g, w, h);
            return;
        }
        g.drawImage(map.get().frame(seconds), 0, 0, null);
        for (int v = 0; v < villageScales.length; v++) {
            map.get().paintVillage(g, v, villageScales[v]);
        }
        if (lockedVillage < 0 || unlock >= 1) return;
        WaveMap m = map.get();
        Rectangle b = m.villageBounds(lockedVillage);
        float s = w / 780f;
        double dark = 1 - phase(unlock, 0.45, 1);
        m.dimVillage(g, lockedVillage, (float) dark);
        m.auraVillage(g, lockedVillage, (float) (dark * (0.38 + 0.08 * Math.sin(seconds * 2.2))));
        drawWisps(g, b, s, seconds, dark);
        int cx = b.x + b.width / 2, cy = b.y + b.height * 9 / 20;
        drawChains(g, b, s, unlock);
        if (unlock <= 0) {  // 섬 위쪽 가장자리: 아래쪽엔 경기대 로고 간판과 마을 이름표가 있다
            g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 12f * s));
            int tw = g.getFontMetrics().stringWidth(hint) + Math.round(24 * s), th = Math.round(26 * s), ty = b.y - th / 3;
            UiStyle.round(g, cx - tw / 2, ty, tw, th, th, new Color(24, 10, 40, 225));
            g.setColor(new Color(236, 220, 255));
            UiStyle.center(g, hint, cx, ty + th / 2 + g.getFontMetrics().getAscent() / 2 - Math.round(2 * s));
        }
        drawBigLock(g, cx, cy, s, seconds, unlock);
    }

    /** t가 from~to 구간에서 0→1로 가는 값(구간 밖은 0 또는 1). */
    private static double phase(double t, double from, double to) {
        return Math.max(0, Math.min(1, (t - from) / (to - from)));
    }

    /** 잠긴 섬 위로 천천히 피어오르는 보랏빛 기운. */
    private static void drawWisps(Graphics2D source, Rectangle b, float s, double seconds, double dark) {
        if (dark <= 0) return;
        Graphics2D g = (Graphics2D) source.create();
        for (int i = 0; i < 7; i++) {
            double life = (seconds * 0.35 + i / 7.0) % 1;  // 0→1 동안 아래에서 위로 떠오르며 사라진다
            double x = b.x + b.width * (0.15 + 0.7 * ((i * 0.618) % 1)) + Math.sin(seconds * 1.3 + i) * 8 * s;
            double y = b.y + b.height * (0.85 - 0.75 * life);
            float r = (float) ((14 + 10 * Math.sin(i * 1.7)) * s * (0.6 + life));
            int alpha = (int) (110 * Math.sin(Math.PI * life) * dark);
            g.setPaint(new RadialGradientPaint((float) x, (float) y, r, new float[]{0f, 1f},
                new Color[]{new Color(150, 70, 220, alpha), new Color(40, 0, 70, 0)}));
            g.fill(new java.awt.geom.Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        }
        g.dispose();
    }

    /** 섬을 X자로 묶은 쇠사슬. 풀릴 때(0.2~0.55) 고리들이 가운데에서 바깥으로 튕겨 나가며 떨어지고 사라진다. */
    private static void drawChains(Graphics2D source, Rectangle b, float s, double t) {
        double snap = phase(t, 0.2, 0.55);
        if (snap >= 1) return;
        Graphics2D g = (Graphics2D) source.create();
        double rattle = t > 0 && t < 0.2 ? Math.sin(t * 160) * 2 * s : 0;
        double[][] ends = {
            {b.x + b.width * 0.06, b.y + b.height * 0.22, b.x + b.width * 0.94, b.y + b.height * 0.74},
            {b.x + b.width * 0.94, b.y + b.height * 0.2, b.x + b.width * 0.08, b.y + b.height * 0.76}};
        double step = 10.5 * s;
        for (double[] e : ends) {
            double len = Math.hypot(e[2] - e[0], e[3] - e[1]), angle = Math.atan2(e[3] - e[1], e[2] - e[0]);
            int count = (int) (len / step);
            for (int i = 0; i <= count; i++) {
                double u = i / (double) count;
                double sag = Math.sin(Math.PI * u) * 10 * s;  // 사슬이 살짝 처진다
                double x = e[0] + (e[2] - e[0]) * u, y = e[1] + (e[3] - e[1]) * u + sag + rattle;
                if (snap > 0) {  // 가운데에서 멀어지는 방향으로 날아가고 중력으로 떨어진다
                    double away = u - 0.5;
                    x += Math.cos(angle) * away * 120 * s * snap;
                    y += Math.sin(angle) * away * 120 * s * snap + 160 * s * snap * snap;
                }
                Graphics2D link = (Graphics2D) g.create();
                link.setComposite(AlphaComposite.SrcOver.derive((float) (1 - snap)));
                link.translate(x, y);
                link.rotate(angle + (snap > 0 ? (i % 3 - 1) * snap * 2 : 0));
                link.scale(s * 1.25, s * 1.25);
                if (i % 2 == 0) {  // 정면 고리(타원)
                    link.setStroke(new BasicStroke(4.2f));
                    link.setColor(new Color(18, 16, 26));
                    link.drawOval(-6, -3, 12, 6);
                    link.setStroke(new BasicStroke(2.2f));
                    link.setColor(new Color(128, 128, 146));
                    link.drawOval(-6, -3, 12, 6);
                    link.setColor(new Color(210, 210, 226, 170));
                    link.setStroke(new BasicStroke(0.9f));
                    link.drawArc(-5, -3, 10, 5, 30, 110);
                } else {  // 옆으로 선 고리(막대)
                    link.setStroke(new BasicStroke(4.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(18, 16, 26));
                    link.drawLine(-6, 0, 6, 0);
                    link.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    link.setColor(new Color(96, 96, 114));
                    link.drawLine(-6, 0, 6, 0);
                }
                link.dispose();
            }
        }
        g.dispose();
    }

    /** 사슬이 만나는 곳의 검은 쇠 자물쇠. 풀릴 때: 덜컹거림(0~0.2) → 고리가 튀어 올라 열림(0.35~0.6) → 금빛으로 터지며 사라짐(0.6~1). */
    private static void drawBigLock(Graphics2D source, int cx, int cy, float s, double seconds, double t) {
        Graphics2D g = (Graphics2D) source.create();
        double shake = t > 0 && t < 0.2 ? Math.sin(t * 90) * 0.16 * (1 - t / 0.2) : 0;
        double open = phase(t, 0.35, 0.6), fade = phase(t, 0.6, 1);
        float glow = (float) (0.55 + 0.45 * Math.sin(seconds * 3));
        if (fade <= 0) {  // 자물쇠 뒤의 어두운 후광
            float r = 46 * s;
            g.setPaint(new RadialGradientPaint(cx, cy + 6 * s, r, new float[]{0f, 1f},
                new Color[]{new Color(120, 40, 190, (int) (120 * glow)), new Color(30, 0, 60, 0)}));
            g.fill(new java.awt.geom.Ellipse2D.Float(cx - r, cy + 6 * s - r, r * 2, r * 2));
        } else {
            for (int k = 0; k < 2; k++) {
                double f = Math.max(0, fade - k * 0.15);
                int r = Math.round((36 + 170 * (float) f) * s);
                g.setColor(new Color(255, 226, 130, (int) (220 * (1 - f))));
                g.setStroke(new BasicStroke(7 * s * (float) (1 - f) + 1));
                g.drawOval(cx - r, cy - r, r * 2, r * 2);
            }
        }
        g.setComposite(AlphaComposite.SrcOver.derive((float) (1 - fade)));
        g.translate(cx, cy);
        g.rotate(shake);
        g.scale(s * (1 + 0.5 * fade), s * (1 + 0.5 * fade));
        // 고리: 오른쪽 다리를 축으로 위로 튀어 오르며 바깥으로 돈다
        Graphics2D shackle = (Graphics2D) g.create();
        shackle.translate(14, -10 - 12 * open);
        shackle.rotate(-0.6 * open);
        shackle.setStroke(new BasicStroke(9, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        shackle.setColor(new Color(14, 12, 20));
        shackle.drawArc(-28, -26, 28, 44, 0, 180);
        shackle.drawLine(-28, -4, -28, 4);
        shackle.drawLine(0, -4, 0, 10);
        shackle.setColor(new Color(92, 90, 108));
        shackle.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        shackle.drawArc(-28, -26, 28, 44, 0, 180);
        shackle.dispose();
        // 몸통: 검은 쇠, 리벳, 보랏빛으로 빛나는 열쇠 구멍
        g.setPaint(new GradientPaint(0, -12, new Color(78, 72, 92), 0, 30, new Color(26, 22, 34)));
        g.fillRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(8, 6, 12));
        g.setStroke(new BasicStroke(2.6f));
        g.drawRoundRect(-26, -12, 52, 42, 10, 10);
        g.setColor(new Color(150, 146, 166));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(-22, -8, 22, -8);
        for (int[] rivet : new int[][]{{-20, -4}, {20, -4}, {-20, 24}, {20, 24}}) g.fillOval(rivet[0] - 2, rivet[1] - 2, 4, 4);
        g.setPaint(new RadialGradientPaint(0, 7, 14, new float[]{0f, 1f},
            new Color[]{new Color(224, 160, 255, (int) (240 * glow)), new Color(120, 40, 200, 0)}));
        g.fillOval(-14, -7, 28, 28);
        g.setColor(new Color(6, 2, 12));
        g.fillOval(-5, 0, 10, 10);
        g.fillRect(-2, 8, 4, 11);
        g.setColor(new Color(200, 130, 255, (int) (230 * glow)));
        g.fillOval(-2, 3, 4, 4);
        g.dispose();
    }

    void paintBackground(Graphics2D g, int w, int h) {
        Optional<BufferedImage> background = image("ui/background");
        if (background.isPresent()) {
            drawCover(g, background.get(), 0, 0, w, h);
            return;
        }
        g.setPaint(new GradientPaint(0, 0, new Color(205, 226, 250), 0, h, new Color(240, 246, 253)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 110));
        g.fillOval(-60, h - 106, 300, 140);
        g.fillOval(150, h - 76, 260, 120);
        g.fillOval(w - 260, h - 96, 320, 150);
        g.fillOval(w - 180, -50, 260, 110);
    }

    /**
     * 카드 한 장을 그린다. 크기에 맞춰 테두리, 이름 띠, 배지가 함께 커진다.
     */
    public void drawCard(Graphics2D g, Person person, int stage, int x, int y, int w, int h, boolean glow) {
        if (stage == 0) {
            drawLockedCard(g, person.getName(), x, y, w, h);
            return;
        }
        float s = w / 100f;
        int arc = Math.round(14 * s), pad = Math.round(4 * s);
        if (glow) {
            for (int i = 6; i >= 1; i--) {
                g.setColor(new Color(60, 150, 255, 24));
                g.fillRoundRect(x - i, y - i, w + i * 2, h + i * 2, arc + i * 2, arc + i * 2);
            }
        }
        gradientRound(g, x, y, w, h, arc,
            glow ? new Color(70, 150, 250) : new Color(238, 196, 112),
            glow ? new Color(24, 92, 210) : new Color(190, 132, 52));

        Shape inner = new RoundRectangle2D.Float(x + pad, y + pad, w - pad * 2, h - pad * 2, arc - pad, arc - pad);
        Shape oldClip = g.getClip();
        g.clip(inner);
        // 클리어한 난이도(별 수)마다 그림이 다르다. 단계 그림이 없으면 이름만 붙은 그림을 쓴다.
        Optional<BufferedImage> portrait = image("cards/" + person.getName() + "_" + stage);
        if (portrait.isPresent()) {
            drawCover(g, portrait.get(), x + pad, y + pad, w - pad * 2, h - pad * 2);
        } else {
            g.setPaint(new GradientPaint(0, y, new Color(232, 238, 247), 0, y + h, new Color(204, 216, 234)));
            g.fill(inner);
            drawSilhouette(g, x + pad, y + pad, w - pad * 2, h - pad * 2, new Color(160, 176, 200));
        }
        g.setClip(oldClip);

        int ribbonH = Math.round(26 * s), ribbonX = x + Math.round(5 * s), ribbonW = w - Math.round(10 * s);
        int ribbonY = y + h - ribbonH - Math.round(7 * s), ribbonArc = Math.round(6 * s);
        g.setPaint(new GradientPaint(0, ribbonY, new Color(255, 245, 214), 0, ribbonY + ribbonH, new Color(246, 222, 160)));
        g.fillRoundRect(ribbonX, ribbonY, ribbonW, ribbonH, ribbonArc, ribbonArc);
        g.setColor(new Color(196, 146, 60));
        g.setStroke(new BasicStroke(1.5f * s));
        g.drawRoundRect(ribbonX, ribbonY, ribbonW, ribbonH, ribbonArc, ribbonArc);
        g.setColor(new Color(58, 40, 16));
        drawName(g, person.getName(), x + w / 2, ribbonY + ribbonH / 2 + Math.round(4.5f * s), ribbonW - Math.round(8 * s), s);

        drawFieldBadge(g, person.getCategory(), x + Math.round(3 * s), y + Math.round(3 * s), Math.round(24 * s));
        for (int i = 0; i < stage; i++) {
            drawStar(g, x + w - Math.round((11 + i * 14) * s), y + Math.round(12 * s), 7 * s, GOLD);
        }
    }

    /**
     * 분야 아이콘(images/ui/{분야}.png)을 그린다. 그림이 없으면 색 원과 첫 글자로 대신한다.
     */
    void drawFieldBadge(Graphics2D g, String field, int x, int y, int size) {
        if (drawIcon(g, "ui/" + field, x, y, size, size)) return;
        g.setColor(fieldColor(field));
        g.fillOval(x, y, size, size);
        g.setColor(Color.WHITE);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, size * 0.46f));
        UiStyle.center(g, field.substring(0, 1), x + size / 2, y + size / 2 + size / 6);
    }
}
