package org.kgu.integration.ui.collection;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import org.kgu.integration.collection.Card;
import org.kgu.integration.collection.CardRepository;
import org.kgu.integration.ui.common.UiButton;
import org.kgu.integration.ui.common.UiStyle;
import org.kgu.integration.ui.navigation.GameNavigator;

/** 인물 도감 화면. 분야 목록, 카드 격자, 카드 상세를 한 화면 안에서 전환한다. */
public final class CollectionScreen extends JPanel {
    private final JPanel listView = new JPanel(null);
    private final JPanel fieldList = new JPanel(null) {
        // 아이콘이 버튼 위에 겹쳐 있어서, 버튼만 다시 그려져 아이콘이 가려지는 일을 막는다.
        @Override
        public boolean isOptimizedDrawingEnabled() {
            return false;
        }
    };
    private final CardSliderPanel slider = new CardSliderPanel(this::openCard);
    private final CardDetailPanel detail = new CardDetailPanel(this::showList);
    private final List<Card> cards;
    private String field = CardRepository.ALL;

    /**
     * 도감 화면을 만든다.
     *
     * @param navigator 메뉴로 돌아갈 때 쓰는 화면 전환 도구
     */
    public CollectionScreen(GameNavigator navigator) {
        cards = CardRepository.cards(navigator.getSession().getPlayerId());
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT));
        setLayout(null);

        listView.setOpaque(false);
        listView.setBounds(0, 0, UiStyle.WIDTH, UiStyle.HEIGHT);
        add(listView);
        detail.setBounds(0, 0, UiStyle.WIDTH, UiStyle.HEIGHT);
        detail.setVisible(false);
        add(detail);

        fieldList.setOpaque(false);
        fieldList.setBounds(16, 92, 184, 270);
        listView.add(fieldList);
        slider.setBounds(221, 106, 530, 396);
        listView.add(slider);
        UiButton menu = CardArt.button("메인으로", Color.WHITE, 16f, navigator::showMenu);
        menu.setBounds(16, 506, 180, 46);
        listView.add(menu);

        showFields();
        slider.setCards(CardRepository.byField(cards, field));
    }

    private void selectField(String name) {
        field = name;
        showFields();
        fieldList.getComponent(CardRepository.FIELDS.indexOf(name) * 2 + 1).requestFocusInWindow();
        slider.setCards(CardRepository.byField(cards, name));
        listView.repaint();
    }

    // UiButton은 만든 뒤 색을 바꿀 수 없어서 선택이 바뀔 때마다 버튼을 새로 만든다.
    private void showFields() {
        fieldList.removeAll();
        for (int i = 0; i < CardRepository.FIELDS.size(); i++) {
            String name = CardRepository.FIELDS.get(i);
            List<Card> inField = CardRepository.byField(cards, name);
            // 14pt·괄호 없이 써야 왼쪽 분야 아이콘(42px)에 글자가 가리지 않는다.
            String label = name + " " + collected(inField) + "/" + inField.size() * Card.STAGE_COUNT;
            UiButton button = CardArt.button(label, name.equals(field) ? CardArt.BLUE : Color.WHITE, 14f, () -> selectField(name));
            button.setBounds(0, i * 54, 184, 44);
            FieldIcon icon = new FieldIcon(name);
            icon.setBounds(6, i * 54 + 4, 36, 36);
            fieldList.add(icon); // 먼저 넣은 쪽이 앞에 그려진다.
            fieldList.add(button);
        }
        fieldList.revalidate();
    }

    private void openCard(Card card) {
        List<Card> owned = CardRepository.byField(cards, field).stream().filter(Card::owned).toList();
        detail.show(card, owned);
        listView.setVisible(false);
        detail.setVisible(true);
        repaint();
    }

    private void showList() {
        detail.setVisible(false);
        listView.setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics source) {
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        CardArt.paintBackground(g, getWidth(), getHeight());
        if (listView.isVisible()) paintListFrame(g);
        g.dispose();
    }

    private void paintListFrame(Graphics2D g) {
        CardArt.shadow(g, 30, 22, 214, 54, 30);
        CardArt.gradientRound(g, 30, 22, 214, 54, 30, new Color(44, 80, 140), new Color(22, 46, 92));
        g.setColor(new Color(255, 255, 255, 60));
        g.drawRoundRect(31, 23, 211, 51, 28, 28);
        if (!CardArt.drawIcon(g, "ui/book", 40, 26, 46, 46)) CardArt.drawBook(g, 44, 34);
        g.setColor(Color.WHITE);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 28f));
        g.drawString("인물 도감", 98, 60);

        int owned = collected(cards), total = cards.size() * Card.STAGE_COUNT;
        int percent = owned * 100 / Math.max(1, total);
        CardArt.panel(g, 290, 18, 470, 62, 18, new Color(255, 255, 255, 235));
        g.setColor(UiStyle.NAVY);
        g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f));
        UiStyle.center(g, "수집한 카드 " + owned + " / " + total + " (" + percent + "%)", 525, 43);
        UiStyle.round(g, 320, 54, 410, 14, 14, new Color(222, 229, 240));
        UiStyle.round(g, 320, 54, 410 * percent / 100, 14, 14, CardArt.BLUE);

        CardArt.panel(g, 210, 90, 552, 432, 20, new Color(255, 255, 255, 200));
    }

    /** 모은 단계 카드 수. 인물 한 명이 하·중·상 카드를 따로 가진다. */
    private static int collected(List<Card> cards) {
        return cards.stream().mapToInt(card -> card.stages().size()).sum();
    }

    /** UiButton은 글자만 그려서 버튼 위에 겹쳐 놓는 분야 아이콘. 마우스 입력은 아래 버튼이 받는다. */
    private final class FieldIcon extends JComponent {
        private final String name;

        FieldIcon(String name) {
            this.name = name;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            CardArt.drawFieldBadge(g, name, 0, 0, getWidth());
            g.dispose();
        }
    }
}
