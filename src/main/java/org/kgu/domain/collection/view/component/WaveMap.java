package org.kgu.domain.collection.view.component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * 지도 그림에서 바다(파란 픽셀)만 골라 물결처럼 흔들고 밝은 물결 띠를 지나가게 그린다.
 */
public final class WaveMap {
    private final int w, h;
    private final int[] src;
    private final boolean[] water;
    private final BufferedImage frame;
    private final int[] out;
    private final int[] rowShift, colShift, shine;

    // 폐허 지도(없으면 null). 마을을 복구하면 그 섬만, 교수님 마을이 열리면 전체가 원래 지도로 돌아온다.
    private final int[] ruin;
    private final int[] region;          // 픽셀이 속한 섬 번호(-1: 바다·광장)
    private final float[] islandDist;    // 섬 가운데에서 떨어진 정도(섬 가장자리 쪽이 1)
    private final float[] worldDist;     // 경기대 섬 가운데에서 떨어진 정도(지도 끝이 1)
    private final BufferedImage[] ruinSprites = new BufferedImage[4];
    private double[] lastReveal = {1, 1, 1, 1};
    private double lastWorld = 1;

    public WaveMap(Image image, int w, int h) { this(image, null, w, h); }

    public WaveMap(Image image, Image ruinImage, int w, int h) {
        this.w = w;
        this.h = h;
        src = pixels(image, w, h);
        ruin = ruinImage == null ? null : pixels(ruinImage, w, h);
        water = new boolean[src.length];
        for (int i = 0; i < src.length; i++) {
            int r = src[i] >> 16 & 255, gr = src[i] >> 8 & 255, b = src[i] & 255;
            water[i] = b > 150 && b > r + 60 && b > gr + 20;
        }
        frame = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        rowShift = new int[h];
        colShift = new int[w];
        shine = new int[w * 2 + h * 3];
        region = new int[w * h];
        islandDist = new float[w * h];
        worldDist = new float[w * h];
        cutVillages();
        if (ruin != null) measureDistances();
    }

    private static int[] pixels(Image image, int w, int h) {
        BufferedImage base = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = base.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        return base.getRGB(0, 0, w, h, null, 0, w);
    }

    private void measureDistances() {
        double ox = bounds[1].getCenterX(), oy = bounds[1].getCenterY();  // 빛은 경기대 섬에서 퍼진다
        double far = 0;
        for (int cx : new int[]{0, w}) for (int cy : new int[]{0, h}) far = Math.max(far, Math.hypot(cx - ox, cy - oy));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                worldDist[i] = (float) (Math.hypot(x - ox, y - oy) / far);
                int v = region[i];
                if (v < 0) continue;
                Rectangle b = bounds[v];
                double rx = b.width / 2.0, ry = b.height / 2.0;
                islandDist[i] = (float) Math.hypot((x - b.getCenterX()) / rx, (y - b.getCenterY()) / ry) / 1.42f;
            }
        }
    }

    // 마을 섬 = 섬을 감싼 타원 안의 바다 아닌 픽셀(780×560 기준, images/ui/map.png에 맞춘 값).
    // 마우스를 올리면 이 조각을 키워 그린다. 지도 그림을 바꾸면 이 값도 다시 맞춰야 한다.
    private static final double[][] ISLANDS = {
        {225, 155, 150, 105}, {562, 155, 158, 110}, {192, 395, 152, 125}, {578, 405, 162, 120}};
    private final BufferedImage[] sprites = new BufferedImage[4], shadows = new BufferedImage[4];
    private final java.util.Map<String, BufferedImage> tints = new java.util.HashMap<>();
    private final Rectangle[] bounds = new Rectangle[4];

    public int villageAt(int x, int y) {
        if (x < 0 || y < 0 || x >= w || y >= h || water[y * w + x]) return -1;
        return islandAt(x, y);
    }

    /** 바다 여부는 보지 않고 섬 타원만 본다. */
    private int islandAt(int x, int y) {
        double fx = x * 780.0 / w, fy = y * 560.0 / h;
        for (int v = 0; v < ISLANDS.length; v++) {
            double dx = (fx - ISLANDS[v][0]) / ISLANDS[v][2], dy = (fy - ISLANDS[v][1]) / ISLANDS[v][3];
            if (dx * dx + dy * dy <= 1) return v;
        }
        return -1;
    }

    /** 지도 가장자리에서 이어진 파란 픽셀만 바다로 본다(가장자리부터 채워 나가기). */
    private boolean[] ocean() {
        boolean[] seen = new boolean[w * h];
        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
        for (int x = 0; x < w; x++) { queue.add(x); queue.add((h - 1) * w + x); }
        for (int y = 0; y < h; y++) { queue.add(y * w); queue.add(y * w + w - 1); }
        while (!queue.isEmpty()) {
            int i = queue.poll();
            if (seen[i] || !water[i]) continue;
            seen[i] = true;
            int x = i % w, y = i / w;
            if (x > 0) queue.add(i - 1);
            if (x < w - 1) queue.add(i + 1);
            if (y > 0) queue.add(i - w);
            if (y < h - 1) queue.add(i + w);
        }
        return seen;
    }

    private void cutVillages() {
        BufferedImage[] full = new BufferedImage[4], dark = new BufferedImage[4], ruined = new BufferedImage[4];
        for (int v = 0; v < 4; v++) {
            full[v] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            dark[v] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            ruined[v] = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        boolean[] ocean = ocean();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // 유리 돔·수영장처럼 섬 안에 갇힌 파란 곳은 바다가 아니라 섬에 넣는다
                int v = ocean[y * w + x] ? -1 : islandAt(x, y);
                region[y * w + x] = v;
                if (v < 0) continue;
                full[v].setRGB(x, y, 0xFF000000 | src[y * w + x]);
                if (ruin != null) ruined[v].setRGB(x, y, 0xFF000000 | ruin[y * w + x]);
                dark[v].setRGB(x, y, 0x55000020);
                bounds[v] = bounds[v] == null ? new Rectangle(x, y, 1, 1) : bounds[v].union(new Rectangle(x, y, 1, 1));
            }
        }
        for (int v = 0; v < 4; v++) {
            Rectangle b = bounds[v];
            sprites[v] = full[v].getSubimage(b.x, b.y, b.width, b.height);
            shadows[v] = dark[v].getSubimage(b.x, b.y, b.width, b.height);
            ruinSprites[v] = ruined[v].getSubimage(b.x, b.y, b.width, b.height);
        }
    }

    /** scale 1이면 아무것도 안 그린다. 커질수록 위로 살짝 떠오르고 그림자가 생긴다. */
    public void paintVillage(Graphics2D g, int v, double scale) {
        if (scale <= 1.001 || (lastReveal[v] > 0 && lastReveal[v] < 1) || (lastWorld > 0 && lastWorld < 1)) return;
        Rectangle b = bounds[v];
        double lift = (scale - 1) / 0.12;  // 0~1, CollectionView의 최대 배율 1.12 기준
        int nw = (int) (b.width * scale), nh = (int) (b.height * scale);
        int nx = b.x + b.width / 2 - nw / 2, ny = b.y + b.height / 2 - nh / 2;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(shadows[v], nx, ny + (int) (10 * lift), nw, nh, null);
        boolean ruined = ruin != null && lastReveal[v] < 0.5 && lastWorld < 0.5;
        g.drawImage(ruined ? ruinSprites[v] : sprites[v], nx, ny - (int) (6 * lift), nw, nh, null);
    }

    /** 잠긴 마을 섬을 섬 모양대로 어둡게 덮는다. dim 0이면 그대로, 1이면 가장 어둡다. */
    public void dimVillage(Graphics2D g, int v, float dim) {
        if (dim <= 0) return;
        Graphics2D d = (Graphics2D) g.create();
        d.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, dim)));
        Rectangle b = bounds[v];
        for (int i = 0; i < 2; i++) d.drawImage(shadows[v], b.x, b.y, null);  // 그림자 한 장은 옅어서 겹쳐 그린다
        d.dispose();
    }

    /** 마을 섬을 섬 모양대로 한 가지 색으로 덮는다(잠김의 보랏빛 어둠, 풀릴 때의 금빛). 색마다 한 번 만들어 둔다. */
    public void tintVillage(Graphics2D g, int v, Color color, float alpha) {
        if (alpha <= 0) return;
        BufferedImage tint = tints.computeIfAbsent(v + ":" + color.getRGB(), key -> {
            BufferedImage img = new BufferedImage(sprites[v].getWidth(), sprites[v].getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    if ((sprites[v].getRGB(x, y) >>> 24) != 0) img.setRGB(x, y, color.getRGB() | 0xFF000000);
                }
            }
            return img;
        });
        Graphics2D d = (Graphics2D) g.create();
        d.setComposite(AlphaComposite.SrcOver.derive(Math.min(1f, alpha)));
        d.drawImage(tint, bounds[v].x, bounds[v].y, null);
        d.dispose();
    }

    public Rectangle villageBounds(int v) {
        return bounds[v];
    }

    public BufferedImage frame(double t) {
        for (int y = 0; y < h; y++) rowShift[y] = (int) Math.round(2.5 * Math.sin(y * 0.09 + t * 2.0));
        for (int x = 0; x < w; x++) colShift[x] = (int) Math.round(1.5 * Math.cos(x * 0.07 + t * 1.6));
        for (int k = 0; k < shine.length; k++) shine[k] = (int) Math.max(0, 22 * Math.sin(k * 0.045 - t * 2.4) - 8);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int i = y * w + x;
                if (!water[i]) {
                    out[i] = src[i];
                    continue;
                }
                int sx = Math.min(w - 1, Math.max(0, x + rowShift[y])), sy = Math.min(h - 1, Math.max(0, y + colShift[x]));
                int j = sy * w + sx;
                int c = water[j] ? src[j] : src[i];  // 섬 가장자리 색이 바다로 번지지 않게
                int add = shine[x * 2 + y * 3];
                out[i] = Math.min(255, (c >> 16 & 255) + add) << 16 | Math.min(255, (c >> 8 & 255) + add) << 8 | Math.min(255, (c & 255) + add);
            }
        }
        return frame;
    }

    /**
     * 물결 지도 위에 폐허를 덮는다. 복구 중인 곳은 가운데에서 금빛 고리가 퍼지며 원래 지도가 드러난다.
     *
     * @param reveal 섬마다 0(폐허)~1(복구 끝)
     * @param world 0(폐허)~1(지도 전체 복구 끝), 경기대 섬에서 퍼진다
     */
    public BufferedImage frame(double t, double[] reveal, double world) {
        frame(t);
        lastReveal = reveal.clone();
        lastWorld = world;
        if (ruin == null || world >= 1) return frame;
        for (int i = 0; i < out.length; i++) {
            int v = region[i];
            double island = v < 0 ? 0 : edge(reveal[v], islandDist[i]), all = edge(world, worldDist[i]);
            double a = Math.max(island, all);
            double light = Math.max(v < 0 ? 0 : ring(reveal[v], islandDist[i]), ring(world, worldDist[i]));
            int o = out[i], r = ruin[i];
            if (a <= 0 && light <= 0) { out[i] = r; continue; }
            if (a >= 1 && light <= 0) continue;
            int add = (int) (light * 150);
            out[i] = blend(r >> 16 & 255, o >> 16 & 255, a, add) << 16
                | blend(r >> 8 & 255, o >> 8 & 255, a, add * 4 / 5) << 8
                | blend(r & 255, o & 255, a, add * 2 / 5);
        }
        return frame;
    }

    private static final double SOFT = 0.08;

    /** 빛 고리 안쪽이면 1(복구됨), 바깥이면 0, 고리 가장자리는 부드럽게. */
    private static double edge(double p, double d) {
        if (p <= 0) return 0;
        if (p >= 1) return 1;
        return Math.min(1, Math.max(0, (p * (1 + SOFT) - d) / SOFT));
    }

    /** 퍼지는 빛 고리의 밝기. 끝에 다가갈수록 옅어진다. */
    private static double ring(double p, double d) {
        if (p <= 0 || p >= 1) return 0;
        double k = (d - p * (1 + SOFT)) / 0.05;
        return Math.exp(-k * k) * (1 - p * 0.6);
    }

    private static int blend(int ruined, int original, double a, int add) {
        return Math.min(255, (int) (ruined + (original - ruined) * a) + add);
    }
}
