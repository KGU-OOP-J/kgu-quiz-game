package org.kgu.story.view;

import org.kgu.story.event.PrologueGUIEvent;
import org.kgu.story.feature.PrologueFeature;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class PrologueView extends JPanel implements View {
    private record Row(String text, boolean emphasis, boolean title) { }
    private final PrologueGUIEvent event;
    private final JButton skip = new UiButton("프롤로그 건너뛰기  →", new Color(36, 53, 73));
    private final Timer animation = new Timer(16, e -> update());

    public PrologueView(PrologueGUIEvent event) {
        this.event = event; setLayout(null); setBackground(Color.BLACK);
        skip.setName("prologueSkip"); skip.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f));
        skip.setForeground(GameTheme.PAPER); skip.setVisible(false); add(skip);
    }
    public JButton getSkipButton() { return skip; }
    public void startAnimation() { animation.start(); }
    public void stopAnimation() { animation.stop(); }
    private void update() { event.update(this); }
    @Override public void doLayout() { skip.setBounds(Math.max(18, getWidth() - 284), getHeight() - 78, 248, 44); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        GameTheme.backdrop(g, getWidth(), getHeight(), "prologue", 0.78f);
        float scale = Math.max(0.65f, Math.min(getWidth() / 1280f, getHeight() / 960f));
        int width = Math.min(getWidth() - 110, Math.round(940 * scale));
        Font body = UiStyle.FONT.deriveFont(25f * scale), strong = body.deriveFont(Font.BOLD), title = body.deriveFont(Font.BOLD, 36f * scale);
        List<Row> rows = new ArrayList<>();
        for (PrologueFeature.Passage passage : event.passages()) {
            Font font = passage.title() ? title : passage.emphasis() ? strong : body;
            g.setFont(font);
            if (passage.text().isBlank()) rows.add(new Row("", false, false));
            else for (String line : GameTheme.wrap(passage.text(), g.getFontMetrics(), width)) rows.add(new Row(line, passage.emphasis(), passage.title()));
        }
        double elapsed = event.elapsed(), speed = 34 * scale, lineHeight = 52 * scale;
        int center = getWidth() / 2; double initialY = getHeight() * 0.94;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i); double y = initialY + i * lineHeight - elapsed * speed;
            if (y < -70 || y > getHeight() + 80 || row.text().isEmpty()) continue;
            Font font = row.title() ? title : row.emphasis() ? strong : body; g.setFont(font);
            FontMetrics metrics = g.getFontMetrics(); int x = center - metrics.stringWidth(row.text()) / 2;
            int[] points = row.text().codePoints().toArray();
            double entered = (initialY + i * lineHeight - getHeight() * 0.93) / speed;
            for (int j = 0; j < points.length; j++) {
                String character = new String(Character.toChars(points[j]));
                double age = elapsed - entered - j * 0.024;
                float alpha = (float)Math.max(0, Math.min(1, age / 0.6));
                alpha *= Math.max(0, Math.min(1, (float)y / 100));
                alpha *= Math.max(0, Math.min(1, (getHeight() - (float)y) / 65));
                if (alpha > 0) {
                    g.setComposite(AlphaComposite.SrcOver.derive(alpha));
                    g.setColor(row.emphasis() || row.title() ? GameTheme.GOLD : GameTheme.PAPER);
                    g.drawString(character, x, (float)y + (1 - Math.min(1f, Math.max(0, (float)age / 0.65f))) * 26 * scale);
                }
                x += metrics.stringWidth(character);
            }
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setPaint(new GradientPaint(0, 0, new Color(3, 8, 17), 0, 110, new Color(3, 8, 17, 0))); g.fillRect(0, 0, getWidth(), 110);
        if (!rows.isEmpty() && initialY + (rows.size() - 1) * lineHeight - elapsed * speed < getHeight() * 0.25) event.reachedEnd();
        float fade = event.fade(); if (fade > 0) { g.setColor(new Color(0, 0, 0, fade)); g.fillRect(0, 0, getWidth(), getHeight()); }
        g.dispose();
    }
    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.PROLOGUE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
