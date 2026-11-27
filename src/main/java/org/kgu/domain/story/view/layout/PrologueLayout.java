package org.kgu.domain.story.view.layout;

import org.kgu.domain.story.event.PrologueGUIEvent;
import org.kgu.domain.story.feature.PrologueFeature;
import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class PrologueLayout {
    private PrologueLayout() { }

    private record Row(String text, boolean emphasis, boolean title) { }

    public static void paint(Graphics source, int width, int height, PrologueGUIEvent event) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        GameTheme.backdrop(g, width, height, "prologue", 0f);
        Shape originalClip = g.getClip();
        int inset = Math.max(20, width / 16);
        int readingTop = Math.max(20, height / 16);
        int readingHeight = (int)(height * .81);
        var reading = new java.awt.geom.RoundRectangle2D.Double(inset, readingTop,
            width - 2.0 * inset, readingHeight, 32, 32);
        g.setColor(new Color(255, 255, 255, 231));
        g.fill(reading);
        g.clip(reading);
        float scale = Math.max(0.65f, Math.min(width / 1280f, height / 960f));
        int lineWidth = Math.max(1, Math.min(width - 2 * inset - 56, Math.round(940 * scale)));
        Font body = UiStyle.FONT.deriveFont(25f * scale), strong = body.deriveFont(Font.BOLD), title = body.deriveFont(Font.BOLD, 36f * scale);
        List<Row> rows = new ArrayList<>();
        for (PrologueFeature.Passage passage : event.passages()) {
            Font font = passage.title() ? title : passage.emphasis() ? strong : body;
            g.setFont(font);
            if (passage.text().isBlank()) rows.add(new Row("", false, false));
            else for (String line : GameTheme.wrap(passage.text(), g.getFontMetrics(), lineWidth)) rows.add(new Row(line, passage.emphasis(), passage.title()));
        }
        double elapsed = event.elapsed(), speed = 34 * scale, lineHeight = 52 * scale;
        int center = width / 2; double initialY = height * 0.48;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i); double y = initialY + i * lineHeight - elapsed * speed;
            if (y < -70 || y > height + 80 || row.text().isEmpty()) continue;
            Font font = row.title() ? title : row.emphasis() ? strong : body; g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(); int x = center - metrics.stringWidth(row.text()) / 2;
            int[] points = row.text().codePoints().toArray();
            double entered = (initialY + i * lineHeight - height * 0.78) / speed;
            for (int j = 0; j < points.length; j++) {
                String character = new String(Character.toChars(points[j]));
                double age = elapsed - entered - j * 0.024;
                float alpha = (float)Math.max(0, Math.min(1, age / 0.6));
                alpha *= Math.max(0, Math.clamp((y - readingTop) / 85, 0.0, 1.0));
                alpha *= Math.max(0, Math.clamp((readingTop + readingHeight - y) / 65, 0.0, 1.0));
                if (alpha > 0) {
                    g.setComposite(AlphaComposite.SrcOver.derive(alpha));
                    g.setColor(row.emphasis() || row.title() ? GameTheme.BLUE_TEXT : ComponentPainter.TEXT);
                    g.drawString(character, x, (float)y + (1 - Math.min(1f, Math.max(0, (float)age / 0.65f))) * 26 * scale);
                }
                x += metrics.stringWidth(character);
            }
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setClip(originalClip);
        if (!rows.isEmpty() && initialY + (rows.size() - 1) * lineHeight - elapsed * speed < height * 0.25) event.reachedEnd();
        float fade = event.fade(); if (fade > 0) { g.setColor(new Color(GameTheme.PAGE.getRed(), GameTheme.PAGE.getGreen(), GameTheme.PAGE.getBlue(), Math.round(fade * 255))); g.fillRect(0, 0, width, height); }
        g.dispose();
    }
}
