package org.kgu.ui.battle;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/** Portrait-only hit animation. It never changes HP, rewards or the source image file. */
public final class ImpactCard extends JPanel {
    private final BattleComponents.CardPanel card;
    private final String victoryLabel;
    private BufferedImage snapshot;
    private long startedAt;
    private double progress = -1;
    private int damage;
    private boolean defeated;
    private boolean externalHitEffect;

    public void setExternalHitEffect(boolean enabled) { externalHitEffect = enabled; }

    public ImpactCard(String name, String imagePath, Color color, String victoryLabel) {
        this.victoryLabel = victoryLabel;
        setOpaque(false);
        setLayout(new BorderLayout());
        card = new BattleComponents.CardPanel(name, imagePath, color);
        add(card, BorderLayout.CENTER);
    }

    public ImpactCard(String name, BufferedImage image, Color color, String victoryLabel) {
        this.victoryLabel = victoryLabel;
        setOpaque(false); setLayout(new BorderLayout());
        card = new BattleComponents.CardPanel(name, image, color); add(card, BorderLayout.CENTER);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        card.doLayout();
    }

    public void startHit(int damage, boolean defeated, long now) {
        this.damage = damage;
        this.defeated = defeated;
        startedAt = now;
        progress = 0;
        snapshot = null;
    }

    public void updateHit(long now) {
        if (!isAnimating()) return;
        double duration = defeated ? 1_250_000_000.0 : 850_000_000.0;
        progress = Math.min(1, Math.max(0, (now - startedAt) / duration));
        repaint();
    }

    public boolean isAnimating() {
        return progress >= 0 && progress < 1;
    }

    public int shakeOffset() {
        if (!isAnimating() || progress > 0.5) return 0;
        return (int) (Math.sin(progress * 95) * 9 * (1 - progress * 2));
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        if (!isAnimating()) {
            if (!(externalHitEffect && defeated)) super.paintChildren(graphics);
            if (defeated && !externalHitEffect) paintReleased((Graphics2D) graphics);
            return;
        }
        if (snapshot == null || snapshot.getWidth() != getWidth() || snapshot.getHeight() != getHeight()) {
            snapshot = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D capture = snapshot.createGraphics();
            super.paintChildren(capture);
            capture.dispose();
        }
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (externalHitEffect) {
            int offset = shakeOffset();
            float opacity = defeated ? (float) Math.max(0, 1 - progress) : 1;
            g.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g.drawImage(snapshot, offset, 0, null);
            if (progress < 0.18) {
                g.setColor(new Color(255, 255, 255, (int) (120 * (1 - progress / 0.18))));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setComposite(AlphaComposite.SrcOver);
        } else {
            paintSplitPortrait(g);
            paintSlashAndSparks(g);
        }
        paintDamage(g);
        g.dispose();
    }

    private Path2D splitLine() {
        double w = getWidth(), h = getHeight();
        Path2D path = new Path2D.Double();
        path.moveTo(w * 0.7, 0);
        path.lineTo(w * 0.51, h * 0.24);
        path.lineTo(w * 0.61, h * 0.34);
        path.lineTo(w * 0.39, h * 0.57);
        path.lineTo(w * 0.47, h * 0.68);
        path.lineTo(w * 0.25, h);
        return path;
    }

    private void paintSplitPortrait(Graphics2D g) {
        // Two clipped copies move apart along a jagged cut, then settle back into place.
        double strength = Math.sin(Math.PI * progress);
        int offset = (int) (getWidth() * (defeated ? 0.16 : 0.065) * strength);
        Path2D leftPath = splitLine();
        leftPath.lineTo(0, getHeight());
        leftPath.lineTo(0, 0);
        leftPath.closePath();
        Area left = new Area(leftPath);
        Area right = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
        right.subtract(left);
        drawPiece(g, left, -offset, -offset / 3);
        drawPiece(g, right, offset, offset / 3);
    }

    private void drawPiece(Graphics2D source, Shape clip, int x, int y) {
        Graphics2D g = (Graphics2D) source.create();
        g.translate(x, y);
        g.clip(clip);
        g.drawImage(snapshot, 0, 0, null);
        if (progress < 0.2) {
            g.setColor(new Color(255, 255, 255, (int) (100 * (1 - progress / 0.2))));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.dispose();
    }

    private void paintSlashAndSparks(Graphics2D g) {
        float alpha = (float) Math.max(0, 1 - progress);
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setStroke(new BasicStroke(Math.max(2f, getWidth() / 35f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(66, 213, 255));
        g.draw(splitLine());
        g.setStroke(new BasicStroke(Math.max(1f, getWidth() / 110f)));
        g.setColor(Color.WHITE);
        g.draw(splitLine());
        double distance = getWidth() * (0.08 + progress * 0.42);
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            int x = (int) (getWidth() / 2.0 + Math.cos(angle) * distance);
            int y = (int) (getHeight() / 2.0 + Math.sin(angle) * distance);
            g.setColor(i % 2 == 0 ? new Color(255, 222, 83) : Color.WHITE);
            g.fillOval(x, y, 6, 6);
        }
        g.setComposite(AlphaComposite.SrcOver);
    }

    private void paintDamage(Graphics2D g) {
        if (damage <= 0) return;
        float alpha = (float) Math.min(1, (1 - progress) * 3);
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setFont(new Font("Malgun Gothic", Font.BOLD, Math.max(24, getWidth() / 6)));
        String text = "-" + damage;
        int x = (getWidth() - g.getFontMetrics().stringWidth(text)) / 2;
        int y = (int) (getHeight() * (0.4 - progress * 0.18));
        g.setColor(new Color(92, 31, 31));
        g.drawString(text, x + 3, y + 3);
        g.setColor(new Color(255, 226, 83));
        g.drawString(text, x, y);
    }

    private void paintReleased(Graphics2D source) {
        Graphics2D g = (Graphics2D) source.create();
        int height = Math.max(40, getHeight() / 7);
        g.setColor(new Color(17, 129, 128, 235));
        g.fillRoundRect(10, getHeight() / 2 - height / 2, getWidth() - 20, height, 18, 18);
        g.setFont(new Font("Malgun Gothic", Font.BOLD, Math.max(18, getWidth() / 12)));
        g.setColor(Color.WHITE);
        String text = victoryLabel;
        g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2,
                getHeight() / 2 + g.getFontMetrics().getAscent() / 3);
        g.dispose();
    }
}
