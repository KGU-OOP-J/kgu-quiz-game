package org.kgu.integration.ui.collection;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import org.kgu.integration.collection.Card;
import org.kgu.integration.ui.common.UiButton;
import org.kgu.integration.ui.common.UiStyle;

/** 카드 한 장의 그림, 정보, 업적을 보여 주는 상세 화면. */
final class CardDetailPanel extends JPanel {
    private static final int INFO_X = 344;
    private static final int INFO_WIDTH = 360;

    private final JTextArea achievements = new JTextArea();
    private final UiButton back;
    private final UiButton previous = CardArt.button("◀", Color.WHITE, 16f, () -> step(-1));
    private final UiButton next = CardArt.button("▶", Color.WHITE, 16f, () -> step(1));
    private int stage;
    // 카드 그림 양옆 화살표로 얻은 단계의 그림만 넘겨 본다(상만 깼으면 3성만).
    private final UiButton lowerStage = CardArt.button("‹", Color.WHITE, 18f, () -> showStage(stageIndex() - 1));
    private final UiButton higherStage = CardArt.button("›", Color.WHITE, 18f, () -> showStage(stageIndex() + 1));
    private List<Card> owned = List.of();
    private Card card;
    private CardView view; // 첫 카드를 열 때 만든다.

    CardDetailPanel(Runnable onBack) {
        setLayout(null);
        setOpaque(false);

        achievements.setBounds(INFO_X + 22, 292, INFO_WIDTH - 36, 124);
        achievements.setFont(UiStyle.FONT.deriveFont(14f));
        achievements.setForeground(new Color(36, 44, 56));
        achievements.setEditable(false);
        achievements.setFocusable(false);
        achievements.setOpaque(false);
        achievements.setLineWrap(true);
        achievements.setWrapStyleWord(true);
        add(achievements);

        back = CardArt.button("도감으로 돌아가기", Color.WHITE, 19f, onBack);
        back.setBounds(240, 462, 300, 50);
        add(back);
        previous.setBounds(10, 206, 36, 48);
        add(previous);
        next.setBounds(734, 206, 36, 48);
        add(next);
        lowerStage.setBounds(86, 210, 30, 44);
        lowerStage.getAccessibleContext().setAccessibleName("이전 단계 카드");
        add(lowerStage);
        higherStage.setBounds(280, 210, 30, 44);
        higherStage.getAccessibleContext().setAccessibleName("다음 단계 카드");
        add(higherStage);
    }

    /**
     * 카드를 보여 준다.
     *
     * @param owned 좌우 화살표로 넘겨 볼 획득 카드
     */
    void show(Card card, List<Card> owned) {
        this.owned = owned;
        previous.setVisible(owned.size() > 1);
        next.setVisible(owned.size() > 1);
        showCard(card);
        back.requestFocusInWindow();
    }

    private void step(int offset) {
        showCard(owned.get(Math.floorMod(owned.indexOf(card) + offset, owned.size())));
    }

    private void showCard(Card shown) {
        card = shown;
        showStage(shown.stages().size() - 1);
        achievements.setText(shown.achievements().stream().map(line -> "•  " + line).collect(Collectors.joining("\n")));
        repaint();
    }

    private int stageIndex() {
        return card.stages().indexOf(stage);
    }

    private void showStage(int index) {
        stage = card.stages().get(index);
        if (view == null) {
            view = new CardView(card, stage);
            view.setGlow(true);
            view.setBounds(80 - CardView.MARGIN, 56 - CardView.MARGIN, 236 + CardView.MARGIN * 2, 352 + CardView.MARGIN * 2);
            add(view); // 나중에 넣어서 단계 화살표 아래에 그려진다.
        } else {
            view.show(card, stage);
        }
        lowerStage.setVisible(index > 0);
        higherStage.setVisible(index < card.stages().size() - 1);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics source) {
        if (card == null) return;
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        CardArt.panel(g, 54, 30, 672, 404, 24, new Color(255, 255, 255, 240));
        paintCollectedRibbon(g);

        g.setColor(UiStyle.NAVY);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 28f));
        g.drawString(card.name(), INFO_X, 96);
        int introX = INFO_X + g.getFontMetrics().stringWidth(card.name()) + 14;
        g.setFont(UiStyle.FONT.deriveFont(16f));
        if (introX + g.getFontMetrics().stringWidth(card.intro()) <= INFO_X + INFO_WIDTH) {
            g.setColor(new Color(70, 80, 96));
            g.drawString(card.intro(), introX, 96);
        }
        paintFieldPill(g);

        paintBox(g, 176, 62);
        CardArt.drawTab(g, "카드 정보", INFO_X + 12, 160);
        g.setColor(new Color(40, 48, 60));
        g.setFont(UiStyle.FONT.deriveFont(16f));
        g.drawString("공격력", 368, 220);
        g.drawString("난이도", 534, 220);
        g.setColor(UiStyle.NAVY);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 26f));
        g.drawString(String.valueOf(Card.ATTACK), 444, 223);
        for (int i = 0; i < 3; i++) {
            CardArt.drawStar(g, 606 + i * 26, 214, 11, card.stages().contains(i + 1) ? CardArt.GOLD : new Color(232, 236, 242));
        }

        paintBox(g, 270, 154);
        CardArt.drawTab(g, "주요 업적", INFO_X + 12, 254);
        g.dispose();
    }

    private void paintCollectedRibbon(Graphics2D g) {
        g.setColor(new Color(20, 120, 76));
        g.fillPolygon(new int[] { 722, 734, 722 }, new int[] { 58, 58, 66 }, 3);
        CardArt.gradientRound(g, 598, 24, 136, 34, 10, new Color(52, 196, 124), new Color(28, 160, 98));
        g.setColor(Color.WHITE);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f));
        g.drawString("수집 완료", 616, 47);
        CardArt.drawCheck(g, 694, 35, 14);
    }

    private void paintFieldPill(Graphics2D g) {
        Color color = CardArt.fieldColor(card.field());
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f));
        int width = g.getFontMetrics().stringWidth(card.field()) + 50;
        UiStyle.round(g, INFO_X, 110, width, 32, 12, new Color(236, 243, 254));
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(INFO_X, 110, width, 32, 12, 12);
        g.drawString(card.field(), INFO_X + 36, 132);
        CardArt.drawFieldBadge(g, card.field(), INFO_X + 7, 113, 26);
    }

    private void paintBox(Graphics2D g, int y, int height) {
        CardArt.panel(g, INFO_X, y, INFO_WIDTH, height, 14, new Color(248, 250, 254));
        g.setColor(new Color(224, 230, 240));
        g.drawRoundRect(INFO_X, y, INFO_WIDTH, height, 14, 14);
    }
}
