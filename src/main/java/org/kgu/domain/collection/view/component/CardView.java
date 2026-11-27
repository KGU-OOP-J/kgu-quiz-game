package org.kgu.domain.collection.view.component;

import org.kgu.core.model.Person;
import org.kgu.core.window.UiStyle;

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
    private boolean reserveEffectSpace;
    private boolean practiceFeedback;
    private boolean practiceHidden;

    public CardView(Person person, int stage, CardArt art) {
        this.art = art;
        setOpaque(false);
        show(person, stage);
    }

    public void show(Person person, int stage) {
        if (stage < 0 || stage > 3) throw new IllegalArgumentException("카드 단계는 0~3입니다.");
        this.person = java.util.Objects.requireNonNull(person);
        this.stage = stage;
        practiceHidden = false; practiceFeedback = false; hitProgress = -1; hitSnapshot = null;
        repaint();
    }

    public void reservePracticeEffectSpace() {
        reserveEffectSpace = true;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
        repaint();
    }

    /** Plays a short portrait-sized hit effect without changing the card or its model. */
    public void startHit(long now) { startHit(false, now); }
    public void startCorrectHit(long now) { startHit(true, now); }
    private void startHit(boolean positive, long now) {
        practiceHidden = false; practiceFeedback = false; positiveHit = positive; hitStartedAt = now; hitProgress = 0; hitSnapshot = null; repaint();
    }
    /** Sparkles settle back; torn practice cards disappear until the next question. */
    public void startPracticeFeedback(boolean correct, long now) {
        startHit(correct, now); practiceFeedback = true;
    }
    public void updateHit(long now) {
        if (!isHitAnimating()) return;
        hitProgress = Math.min(1, Math.max(0, (now - hitStartedAt) / (practiceFeedback ? 1_200_000_000.0 : 850_000_000.0)));
        if (practiceFeedback && hitProgress >= 1) { practiceHidden = !positiveHit; hitProgress = -1; hitSnapshot = null; practiceFeedback = false; }
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
        if (getWidth() <= MARGIN * 2 || getHeight() <= MARGIN * 2) return;
        if (practiceHidden) return;
        if (hitProgress >= 0) {
            if (hitSnapshot == null || hitSnapshot.getWidth() != getWidth() || hitSnapshot.getHeight() != getHeight()) {
                hitSnapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D capture = hitSnapshot.createGraphics();
                drawCard(capture); capture.dispose();
            }
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (positiveHit) paintAcquiredCard(g);
            else if (practiceFeedback) {
                // Both pieces move apart and fade out; never restore a wrong-answer card.
                float fade = (float) smooth((hitProgress - .4) / .6);
                g.setComposite(AlphaComposite.SrcOver.derive(1 - fade));
                paintHorizontalTear(g);
            } else paintHorizontalTear(g);
            g.dispose(); return;
        }
        drawCard((Graphics2D) source);
    }

    private void drawCard(Graphics2D source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        Rectangle bounds = cardBounds();
        art.drawCard(g, person, stage, bounds.x, bounds.y, bounds.width, bounds.height, glow, 1.16f);
        g.dispose();
    }

    private Rectangle cardBounds() {
        int horizontal = reserveEffectSpace ? Math.max(MARGIN, (int)(getWidth() * .085)) : MARGIN;
        int vertical = reserveEffectSpace ? Math.max(MARGIN, (int)(getHeight() * .135)) : MARGIN;
        return new Rectangle(horizontal, vertical, getWidth() - horizontal * 2, getHeight() - vertical * 2);
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
        int separation = (int)(h * (practiceFeedback ? .18 * smooth(hitProgress) : .095 * hitProgress));
        double scale = 1 - .06 * hitProgress;
        float darken = .12f + .5f * (float)hitProgress;
        drawTornHalf(g, upper, -separation, scale, darken);
        drawTornHalf(g, lower, separation, scale, darken);
    }

    private void paintAcquiredCard(Graphics2D g) {
        int w = getWidth(), h = getHeight();
        boolean held = hitProgress >= 1;
        double pulse = held ? 0 : practiceFeedback ? Math.pow(Math.sin(Math.PI * hitProgress), 2) : Math.sin(Math.PI * hitProgress);
        double scale = practiceFeedback ? 1 - .06 * pulse : 1 + .13 * pulse;
        int drawW = (int)(w * scale), drawH = (int)(h * scale);
        int lift = practiceFeedback ? (int)(h * .03 * pulse) : (int)(h * .11 * hitProgress);
        g.drawImage(hitSnapshot, (w - drawW) / 2, (h - drawH) / 2 - lift, drawW, drawH, null);

        float alpha = practiceFeedback ? (float) Math.pow(Math.sin(Math.PI * hitProgress), .7) : held ? .7f : (float)Math.min(1, 1.15 * (1 - hitProgress));
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setColor(new Color(255, 239, 176));
        g.setStroke(new BasicStroke(Math.max(2f, w / 95f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = w / 2, cy = h / 2 - lift;
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8 + hitProgress * .35;
            int inner = (int)(w * (.29 + .04 * Math.sin(i + hitProgress * 6)));
            int outer = inner + (int)(w * (.1 + .05 * (1 - hitProgress)));
            g.drawLine(cx + (int)(Math.cos(angle) * inner), cy + (int)(Math.sin(angle) * inner),
                    cx + (int)(Math.cos(angle) * outer), cy + (int)(Math.sin(angle) * outer));
        }
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI * 2 / 12 + hitProgress * .85;
            double distance = w * (.12 + .35 * hitProgress);
            int x = w / 2 + (int)(Math.cos(angle) * distance);
            int y = h / 2 - lift + (int)(Math.sin(angle) * distance);
            int size = Math.max(6, w / (i % 3 == 0 ? 15 : 23));
            paintSpark(g, Math.clamp(x, size, w - size), Math.clamp(y, size, h - size), size);
        }
    }

    private static double smooth(double value) {
        double t = Math.max(0, Math.min(1, value)); return t * t * (3 - 2 * t);
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

    private void drawTornHalf(Graphics2D source, Shape clip, int dy, double scale, float darken) {
        Graphics2D g = (Graphics2D)source.create();
        double x = getWidth() / 2.0, y = getHeight() / 2.0;
        g.translate(x, y + dy);
        g.scale(scale, scale);
        g.translate(-x, -y);
        g.clip(clip);
        g.drawImage(hitSnapshot, 0, 0, null);
        g.setComposite(AlphaComposite.SrcOver.derive(darken * ((AlphaComposite)g.getComposite()).getAlpha()));
        g.setColor(new Color(24, 30, 48));
        Rectangle bounds = cardBounds();
        int radius = Math.round(bounds.width * .14f);
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius, radius);
        g.dispose();
    }
}
