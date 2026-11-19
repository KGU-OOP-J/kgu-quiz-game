package org.kgu.collection.view;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * 지도 그림에서 바다(파란 픽셀)만 골라 물결처럼 흔들고 밝은 물결 띠를 지나가게 그린다.
 */
final class WaveMap {
    private final int w, h;
    private final int[] src;
    private final boolean[] water;
    private final BufferedImage frame;
    private final int[] out;
    private final int[] rowShift, colShift, shine;

    WaveMap(Image image, int w, int h) {
        this.w = w;
        this.h = h;
        BufferedImage base = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = base.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, w, h, null);
        g.dispose();
        src = base.getRGB(0, 0, w, h, null, 0, w);
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
    }

    BufferedImage frame(double t) {
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
}
