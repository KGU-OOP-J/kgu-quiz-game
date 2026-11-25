package org.kgu.collection.view;

import org.kgu.model.Person;
import org.kgu.window.UiStyle;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

/**
 * 카드용 모델을 만들지 않고 원본 Person과 표시할 난이도만 사용한다.
 */
public final class CardView extends JComponent {
    public static final int MARGIN = 6;
    private final CardArt art;
    private Person person;
    private int stage;
    private boolean glow;
    private BufferedImage hitSnapshot;
    private long hitStartedAt;
    private double hitProgress = -1;
    private boolean positiveHit;

    public CardView(Person person, int stage, CardArt art) {
        this.art = art;
        setOpaque(false);
        show(person, stage);
    }

    public void show(Person person, int stage) {
        if (stage < 0 || stage > 3) throw new IllegalArgumentException("카드 단계는 0~3입니다.");
        this.person = java.util.Objects.requireNonNull(person);
        this.stage = stage;
        repaint();
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
        repaint();
    }

    /** Plays a short portrait-sized hit effect without changing the card or its model. */
    public void startHit(long now) { startHit(false, now); }
    public void startCorrectHit(long now) { startHit(true, now); }
    private void startHit(boolean positive, long now) {
        positiveHit = positive; hitStartedAt = now; hitProgress = 0; hitSnapshot = null; repaint();
    }
    public void updateHit(long now) {
        if (!isHitAnimating()) return;
        hitProgress = Math.min(1, Math.max(0, (now - hitStartedAt) / 850_000_000.0));
        repaint();
    }
    public boolean isHitAnimating() { return hitProgress >= 0 && hitProgress < 1; }

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) accessibleContext = new AccessibleJComponent() {};
        accessibleContext.setAccessibleName(person.getName() + (stage == 0 ? " (잠김)" : " " + stage + "성 카드"));
        return accessibleContext;
    }

    @Override
    protected void paintComponent(Graphics source) {
        if (hitProgress >= 0) {
            if (hitSnapshot == null || hitSnapshot.getWidth() != getWidth() || hitSnapshot.getHeight() != getHeight()) {
                hitSnapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D capture = hitSnapshot.createGraphics();
                drawCard(capture); capture.dispose();
            }
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (positiveHit) paintAcquiredCard(g);
            else paintHorizontalTear(g);
            g.dispose(); return;
        }
        drawCard((Graphics2D) source);
    }

    private void drawCard(Graphics2D source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        art.drawCard(g, person, stage, MARGIN, MARGIN, getWidth() - MARGIN * 2, getHeight() - MARGIN * 2, glow, 1.16f);
        g.dispose();
    }

    private void paintHorizontalTear(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        Path2D tear = new Path2D.Double();
        tear.moveTo(0, h * .49);
        tear.lineTo(w * .16, h * .46); tear.lineTo(w * .27, h * .52);
        tear.lineTo(w * .39, h * .47); tear.lineTo(w * .53, h * .53);
        tear.lineTo(w * .67, h * .46); tear.lineTo(w * .81, h * .52);
        tear.lineTo(w, h * .48);
        Path2D upperPath = new Path2D.Double(tear);
        upperPath.lineTo(w, 0); upperPath.lineTo(0, 0); upperPath.closePath();
        Area upper = new Area(upperPath);
        Area lower = new Area(new Rectangle(0, 0, w, h)); lower.subtract(upper);
        int separation = (int)(h * .095 * Math.min(1, hitProgress));
        float darken = .12f + .5f * (float)Math.min(1, hitProgress);
        drawTornHalf(g, upper, 0, -separation, darken);
        drawTornHalf(g, lower, 0, separation, darken);
    }

    private void paintAcquiredCard(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        boolean held = hitProgress >= 1;
        double pulse = held ? 0 : Math.sin(Math.PI * hitProgress);
        double scale = 1 + .13 * pulse;
        int drawW = (int)(w * scale), drawH = (int)(h * scale);
        int lift = (int)(h * .11 * hitProgress);
        g.drawImage(hitSnapshot, (w - drawW) / 2, (h - drawH) / 2 - lift, drawW, drawH, null);

        float alpha = held ? .7f : (float)Math.min(1, 1.15 * (1 - hitProgress));
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setColor(new Color(255, 239, 176));
        if (!held) {
            g.setStroke(new BasicStroke(Math.max(2f, w / 95f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = w / 2, cy = h / 2 - lift;
            for (int i = 0; i < 16; i++) {
                double angle = i * Math.PI / 8 + hitProgress * .35;
                int inner = (int)(w * (.29 + .04 * Math.sin(i + hitProgress * 6)));
                int outer = inner + (int)(w * (.1 + .05 * (1 - hitProgress)));
                g.drawLine(cx + (int)(Math.cos(angle) * inner), cy + (int)(Math.sin(angle) * inner),
                        cx + (int)(Math.cos(angle) * outer), cy + (int)(Math.sin(angle) * outer));
            }
        }
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12 + (held ? 0 : hitProgress * .85);
            double distance = w * (held ? .44 : .12 + .35 * hitProgress);
            int x = w / 2 + (int)(Math.cos(angle) * distance);
            int y = h / 2 - lift + (int)(Math.sin(angle) * distance);
            int size = Math.max(6, w / (i % 3 == 0 ? 15 : 23));
            paintSpark(g, x, y, size);
        }
    }

    private void paintSpark(Graphics2D g, int x, int y, int size) {
        Polygon star = new Polygon();
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int radius = i % 2 == 0 ? size : Math.max(1, size / 4);
            star.addPoint(x + (int)(Math.cos(angle) * radius), y + (int)(Math.sin(angle) * radius));
        }
        g.fillPolygon(star);
    }

    private void drawTornHalf(Graphics2D source, Shape clip, int dx, int dy, float darken) {
        Graphics2D g = (Graphics2D)source.create();
        g.translate(dx, dy); g.clip(clip); g.drawImage(hitSnapshot, 0, 0, null);
        g.setComposite(AlphaComposite.SrcOver.derive(darken));
        g.setColor(new Color(24, 30, 48)); g.fillRect(-dx, -dy, getWidth(), getHeight());
        g.dispose();
    }

}
