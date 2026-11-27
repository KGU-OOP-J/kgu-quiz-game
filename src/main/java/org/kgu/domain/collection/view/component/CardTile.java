package org.kgu.domain.collection.view.component;

import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.BooleanSupplier;

public final class CardTile extends JButton {
    private final CardView card;
    private final BooleanSupplier isNew;
    private final boolean equipped;
    private final Timer motion = new Timer(16, e -> tick());
    private long changed, appeared;
    private double amount, from, target;
    private boolean badgesVisible = true;
    public void setBadgesVisible(boolean visible) { badgesVisible = visible; repaint(); }
    public CardTile(CardView card, BooleanSupplier isNew, boolean equipped) {
        this.card = card; this.isNew = isNew; this.equipped = equipped;
        setContentAreaFilled(false); setBorderPainted(false); setRolloverEnabled(true); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { moveTo(isEnabled() ? 1 : 0); }
            @Override public void mouseExited(MouseEvent e) { moveTo(0); }
        });
        addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { moveTo(1); }
            @Override public void focusLost(FocusEvent e) { moveTo(0); }
        });
    }
    private void moveTo(double target) { from = amount; this.target = target; changed = System.nanoTime(); motion.start(); }
    private void tick() {
        long now = System.nanoTime(); double t = Math.clamp((now - changed) / 150_000_000.0, 0, 1);
        amount = from + (target - from) * (1 - Math.pow(1 - t, 3)); repaint();
        if (t >= 1 && now - appeared >= 250_000_000) motion.stop();
    }
    @Override public void addNotify() { super.addNotify(); appeared = System.nanoTime(); motion.start(); }
    @Override public void removeNotify() { motion.stop(); super.removeNotify(); }
    @Override protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
        g.translate(getWidth() / 2.0, getHeight() / 2.0); g.scale(1 + .025 * amount, 1 + .025 * amount);
        g.shear(.015 * amount, 0); g.translate(-getWidth() / 2.0, -getHeight() / 2.0);
        int inset = badgesVisible ? 25 : 0;
        int height = Math.max(0, getHeight() - inset * 2);
        int width = Math.min(getWidth(), Math.max(0, (int)(height * .72)));
        g.translate((getWidth() - width) / 2, inset);
        card.setGlow(amount > .1 || hasFocus()); card.setSize(width, height); card.paint(g);
        g.translate(-(getWidth() - width) / 2, -inset);
        if (badgesVisible && equipped) badge(g, "장착", getHeight() - 24, 1);
        if (badgesVisible && isNew.getAsBoolean()) badge(g, "NEW", 1, (float)Math.clamp((System.nanoTime() - appeared) / 250_000_000.0, 0, 1));
        g.dispose();
    }
    private void badge(Graphics2D g, String label, int y, float alpha) {
        g.setComposite(AlphaComposite.SrcOver.derive(alpha)); g.setColor(GameTheme.BLUE_TEXT);
        int x = (getWidth() - 50) / 2;
        g.fillRoundRect(x, y, 50, 23, 8, 8); g.setColor(Color.WHITE);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 12f)); g.drawString(label, x + 10, y + 17);
    }
}
