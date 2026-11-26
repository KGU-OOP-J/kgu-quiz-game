package org.kgu.collection.view;

import org.kgu.model.Person;
import org.kgu.window.GameTheme;
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
        button.setForeground(GameTheme.PAPER);
        // 메뉴와 같은 색: 강조 버튼은 파랑, 나머지는 차분한 남색
        button.putClientProperty("selectionTint", color.equals(BLUE) ? new Color(43, 104, 165) : new Color(35, 52, 73));
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
        drawName(g, name, cx, baseline, maxW, s, 1f);
    }

    private static void drawName(Graphics2D g, String name, int cx, int baseline, int maxW, float s, float nameScale) {
        Font font = UiStyle.FONT.deriveFont(Font.BOLD, 12.5f * s * nameScale);
        int width = g.getFontMetrics(font).stringWidth(name);
        if (width > maxW) font = font.deriveFont(font.getSize2D() * maxW / width);
        g.setFont(font);
        UiStyle.center(g, name, cx, baseline);
    }

    private static void drawLockedCard(Graphics2D g, String name, int x, int y, int w, int h) {
        float s = w / 100f;
        int arc = Math.round(14 * s), pad = Math.round(4 * s);
        gradientRound(g, x, y, w, h, arc, new Color(32, 48, 66), new Color(18, 30, 45));
        g.setColor(new Color(113, 136, 148, 120));
        g.setStroke(new BasicStroke(1.5f * s));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, arc, arc);
        Shape oldClip = g.getClip();
        g.clip(new RoundRectangle2D.Float(x + pad, y + pad, w - pad * 2, h - pad * 2, arc - pad, arc - pad));
        drawSilhouette(g, x + pad, y + pad, w - pad * 2, h - pad * 2, new Color(52, 70, 90));
        g.setClip(oldClip);
        drawLock(g, x + w / 2, y + h / 2, s);
        // 잠긴 카드도 이름은 보여 준다. 위치는 획득 카드의 이름 띠와 같다.
        g.setColor(GameTheme.MUTED);
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
        g.setColor(GameTheme.GOLD);
        g.setStroke(new BasicStroke(4f * s, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawArc(cx - bw / 3, cy - bh, bw * 2 / 3, bh * 3 / 2, 0, 180);
        g.fillRoundRect(cx - bw / 2, cy - bh / 4, bw, bh, Math.round(6 * s), Math.round(6 * s));
        g.setColor(GameTheme.DARK);
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
            image("ui/map").ifPresent(map -> waveMap = new WaveMap(map, image("ui/map_ruin").orElse(null), w, h));
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
     * @param reveal 섬마다 폐허에서 복구된 정도(0~1)
     * @param world 지도 전체가 복구된 정도(0~1)
     */
    void paintMap(Graphics2D g, int w, int h, double seconds, double[] villageScales, int lockedVillage, double unlock, String hint,
                  double[] reveal, double world) {
        Optional<WaveMap> map = waveMap(w, h);
        if (map.isEmpty()) {
            paintBackground(g, w, h);
            return;
        }
        BufferedImage frame = map.get().frame(seconds, reveal, world);
        boolean sealed = lockedVillage >= 0 && unlock < 1;
        // 봉인이 있으면 빛 번짐을 더하려고 작업 화면에 그린 뒤 한 번에 옮긴다
        Graphics2D target = sealed ? lockArt.begin(frame) : g;
        if (!sealed) g.drawImage(frame, 0, 0, null);
        for (int v = 0; v < villageScales.length; v++) {
            map.get().paintVillage(target, v, villageScales[v]);
        }
        if (sealed) lockArt.paint(g, target, map.get(), lockedVillage, w / 780f, seconds, unlock, hint);
    }

    private final LockArt lockArt = new LockArt();

    void paintBackground(Graphics2D g, int w, int h) {
        // 도전 준비 화면과 같은 어두운 캠퍼스 배경
        GameTheme.backdrop(g, w, h, "collection", 0.82f);
    }

    /**
     * 카드 한 장을 그린다. 크기에 맞춰 테두리, 이름 띠, 배지가 함께 커진다.
     */
    public void drawCard(Graphics2D g, Person person, int stage, int x, int y, int w, int h, boolean glow) {
        drawCard(g, person, stage, x, y, w, h, glow, 1f);
    }

    public void drawCard(Graphics2D g, Person person, int stage, int x, int y, int w, int h, boolean glow, float nameScale) {
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
        drawName(g, person.getName(), x + w / 2, ribbonY + ribbonH / 2 + Math.round(4.5f * s), ribbonW - Math.round(8 * s), s, nameScale);

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
