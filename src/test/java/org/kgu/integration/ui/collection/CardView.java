package org.kgu.integration.ui.collection;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import org.kgu.integration.collection.Card;
import org.kgu.integration.ui.common.UiStyle;

/**
 * 카드 한 장. 도감 격자·상세 화면과 다른 화면(문제·결과 등)이 모두 이 부품으로 카드를 그린다.
 * 크기는 {@code setBounds}로 정하고, 테두리·이름·별이 그 크기에 맞춰 같이 커진다.
 * 사방 {@value #MARGIN}px은 빛나는 테두리 자리로 비워 둔다. 카드 모양이 2:3이 되려면 예를 들어 132×192로 잡는다.
 *
 * <pre>{@code
 * CardView view = new CardView(card, 3); // 이 인물의 3성 카드
 * view.setBounds(40, 120, 132, 192);
 * add(view);
 * }</pre>
 */
public final class CardView extends JComponent {
    /** 빛나는 테두리를 그릴 사방 여백(px). */
    public static final int MARGIN = 6;

    private Card shown;
    private boolean glow;

    /**
     * 카드 한 장을 만든다. 사용자가 그 단계를 얻었는지와 상관없이 고른 단계 그림을 보여 준다.
     *
     * @param card {@link org.kgu.integration.collection.CardRepository#cards(String)}에서 받은 카드
     * @param stage 보여 줄 단계(1~3). 0이면 잠긴 카드로 보여 준다
     * @throws IllegalArgumentException 단계가 0~3이 아닐 때
     */
    public CardView(Card card, int stage) {
        setOpaque(false);
        show(card, stage);
    }

    /**
     * 보여 줄 카드나 단계를 바꾼다.
     *
     * @param card 보여 줄 카드
     * @param stage 보여 줄 단계(1~3). 0이면 잠긴 카드로 보여 준다
     * @throws IllegalArgumentException 단계가 0~3이 아닐 때
     */
    public void show(Card card, int stage) {
        if (stage < 0 || stage > Card.STAGE_COUNT) throw new IllegalArgumentException("카드 단계는 0~3이어야 합니다: " + stage);
        shown = new Card(card.name(), card.intro(), card.field(), stage == 0 ? List.of() : List.of(stage), card.achievements());
        repaint();
    }

    /**
     * 파란 빛 테두리를 켜거나 끈다. 고른 카드나 마우스가 올라간 카드를 표시할 때 쓴다.
     *
     * @param glow 켜려면 true
     */
    public void setGlow(boolean glow) {
        this.glow = glow;
        repaint();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        AccessibleContext context = super.getAccessibleContext();
        context.setAccessibleName(shown.owned() ? shown.name() + " " + shown.stars() + "성 카드" : shown.name() + " (잠김)");
        return context;
    }

    @Override
    protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        CardArt.drawCard(g, shown, MARGIN, MARGIN, getWidth() - MARGIN * 2, getHeight() - MARGIN * 2, glow);
        g.dispose();
    }
}
