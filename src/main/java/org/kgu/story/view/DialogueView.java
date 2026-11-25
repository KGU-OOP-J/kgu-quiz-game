package org.kgu.story.view;

import org.kgu.story.event.DialogueGUIEvent;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.List;

public final class DialogueView extends JPanel implements View {
    private final DialogueGUIEvent event;
    private final Timer animation = new Timer(16, e -> update());
    private BufferedImage portrait;

    public DialogueView(DialogueGUIEvent event) {
        this.event = event; setFocusable(true); setBackground(Color.BLACK);
    }
    public void setPortrait(BufferedImage portrait) { this.portrait = portrait; }
    public void startAnimation() { animation.start(); }
    public void stopAnimation() { animation.stop(); }
    private void update() { event.update(this); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        int w = getWidth(), h = getHeight();
        GameTheme.backdrop(g, w, h, "dialogue", 0.65f);
        double elapsed = event.elapsed(); float entrance = Math.min(1f, (float)(elapsed / 0.8));
        float scale = Math.max(0.6f, Math.min(w / 1280f, h / 960f));
        int boxX = Math.round(65 * scale), boxHeight = Math.max(170, Math.round(225 * scale)), boxY = h - boxHeight - Math.round(75 * scale);
        int boxW = w - boxX * 2;
        g.setComposite(AlphaComposite.SrcOver.derive(entrance));
        int portraitH = Math.min(Math.round(550 * scale), boxY - Math.round(35 * scale));
        if (portrait != null && portraitH > 0) {
            int portraitW = (int)(portraitH * portrait.getWidth() / (double)portrait.getHeight());
            int x = (w - portraitW) / 2, y = boxY - portraitH + 20;
            g.setPaint(new RadialGradientPaint(w / 2f, y + portraitH / 2f, Math.max(1, portraitH * 0.7f), new float[]{0, 1}, new Color[]{new Color(106, 164, 180, 70), new Color(106, 164, 180, 0)}));
            g.fillOval(w / 2 - portraitH, y - 45, portraitH * 2, portraitH + 120);
            Shape clip = g.getClip(); g.clip(new RoundRectangle2D.Float(x, y, portraitW, portraitH, 26, 26));
            g.drawImage(portrait, x, y, portraitW, portraitH, null); g.setClip(clip);
        }
        GameTheme.paintSurface(g, boxX, boxY, boxW, boxHeight, 22, false);
        GameTheme.asset("dialogue-frame").ifPresent(image -> GameTheme.ninePatch(g, image, boxX, boxY, boxW, boxHeight, 28));
        g.setColor(GameTheme.GOLD); g.fillRoundRect(boxX + 28, boxY - 22, Math.round(255 * scale), 40, 12, 12);
        g.setColor(GameTheme.INK); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f * scale));
        g.drawString(event.speaker(), boxX + 44, boxY + 5);
        Font font = UiStyle.FONT.deriveFont(27f * scale); g.setFont(font); g.setColor(GameTheme.PAPER);
        FontMetrics metrics = g.getFontMetrics(); int lineHeight = metrics.getHeight() + 8;
        List<String> lines = GameTheme.wrap(event.visibleText(), metrics, boxW - 80);
        int visible = Math.max(1, (boxHeight - 60) / lineHeight), start = Math.max(0, lines.size() - visible);
        Shape clip = g.getClip(); g.clipRect(boxX + 30, boxY + 28, boxW - 60, boxHeight - 48);
        for (int i = start; i < lines.size(); i++) g.drawString(lines.get(i), boxX + 40, boxY + 54 + (i - start) * lineHeight);
        g.setClip(clip);
        g.setComposite(AlphaComposite.SrcOver);
        g.setFont(UiStyle.FONT.deriveFont(16f * scale)); String skip = "3초 동안 Ctrl 눌러 스킵하기";
        int skipW = g.getFontMetrics().stringWidth(skip), skipX = w - boxX - skipW, skipY = h - Math.round(32 * scale);
        g.setColor(GameTheme.MUTED); g.drawString(skip, skipX, skipY);
        g.setColor(new Color(255, 255, 255, 35)); g.fillRoundRect(skipX, skipY + 10, skipW, 3, 3, 3);
        g.setColor(GameTheme.GOLD); g.fillRoundRect(skipX, skipY + 10, Math.round(skipW * event.skipProgress()), 3, 3, 3);
        g.dispose();
    }
    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.DIALOGUE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
