package org.kgu.collection.view;

import org.kgu.model.Person;
import org.kgu.window.UiStyle;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;

/**
 * 카드용 모델을 만들지 않고 원본 Person과 표시할 난이도만 사용한다.
 */
public final class CardView extends JComponent {
    public static final int MARGIN = 6;
    private final CardArt art;
    private Person person;
    private int stage;
    private boolean glow;

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

    @Override
    public AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) accessibleContext = new AccessibleJComponent() {};
        accessibleContext.setAccessibleName(person.getName() + (stage == 0 ? " (잠김)" : " " + stage + "성 카드"));
        return accessibleContext;
    }

    @Override
    protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        art.drawCard(g, person, stage, MARGIN, MARGIN, getWidth() - MARGIN * 2, getHeight() - MARGIN * 2, glow);
        g.dispose();
    }
}
