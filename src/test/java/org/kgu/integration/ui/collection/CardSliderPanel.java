package org.kgu.integration.ui.collection;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import org.kgu.integration.collection.Card;
import org.kgu.integration.ui.common.UiButton;

/** 카드를 쪽마다 10장씩 보여 주는 격자와 쪽 번호. */
final class CardSliderPanel extends JPanel {
    private static final int CARDS_PER_PAGE = 10;
    private static final int COLUMNS = 5;
    private static final int TILE_WIDTH = 106;
    private static final int TILE_HEIGHT = 168;

    private final Consumer<Card> onOpen;
    private final JPanel grid = new JPanel(null);
    private final JPanel pageNumbers = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final UiButton previous = CardArt.button("◀", Color.WHITE, 14f, () -> turnPage(-1));
    private final UiButton next = CardArt.button("▶", Color.WHITE, 14f, () -> turnPage(1));
    private List<Card> cards = List.of();
    private int page = 0;

    CardSliderPanel(Consumer<Card> onOpen) {
        this.onOpen = onOpen;
        setLayout(null);
        setOpaque(false);
        grid.setOpaque(false);
        grid.setBounds(0, 0, 530, 336);
        add(grid);

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        pager.setOpaque(false);
        pager.setBounds(0, 352, 530, 44);
        pageNumbers.setOpaque(false);
        previous.setPreferredSize(new Dimension(40, 36));
        next.setPreferredSize(new Dimension(40, 36));
        pager.add(previous);
        pager.add(pageNumbers);
        pager.add(next);
        add(pager);
    }

    void setCards(List<Card> cards) {
        this.cards = cards;
        page = 0;
        boolean paged = pageCount(cards.size()) > 1;
        previous.setVisible(paged);
        next.setVisible(paged);
        showPage(0);
    }

    static int pageCount(int cardCount) {
        return Math.max(1, (cardCount + CARDS_PER_PAGE - 1) / CARDS_PER_PAGE);
    }

    private void turnPage(int offset) {
        showPage(Math.floorMod(page + offset, pageCount(cards.size())));
    }

    private void showPage(int target) {
        page = target;
        grid.removeAll();
        int first = page * CARDS_PER_PAGE;
        for (int i = 0; i < CARDS_PER_PAGE && first + i < cards.size(); i++) {
            CardTile tile = new CardTile(cards.get(first + i));
            tile.setBounds(i % COLUMNS * TILE_WIDTH, i / COLUMNS * TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT - 4);
            grid.add(tile);
        }
        grid.revalidate();
        showPageNumbers();
        repaint();
    }

    // UiButton은 만든 뒤 색을 바꿀 수 없어서 쪽이 바뀔 때마다 번호 버튼을 새로 만든다.
    private void showPageNumbers() {
        pageNumbers.removeAll();
        for (int i = 0; i < pageCount(cards.size()); i++) {
            int target = i;
            UiButton number = CardArt.button(String.valueOf(i + 1), i == page ? CardArt.BLUE : Color.WHITE, 16f, () -> {
                showPage(target);
                pageNumbers.getComponent(target).requestFocusInWindow();
            });
            number.setPreferredSize(new Dimension(36, 36));
            pageNumbers.add(number);
        }
        pageNumbers.revalidate();
    }

    /** 격자의 한 칸. 버튼 동작은 JButton이, 그림은 CardView가 맡는다. 획득한 카드만 눌러서 상세를 볼 수 있다. */
    private final class CardTile extends JButton {
        private final CardView view;

        CardTile(Card card) {
            super(card.owned() ? card.name() : card.name() + " (잠김)");
            view = new CardView(card, card.stars());
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setEnabled(card.owned());
            if (card.owned()) setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addActionListener(e -> onOpen.accept(card));
        }

        @Override
        protected void paintComponent(Graphics source) {
            view.setGlow(getModel().isRollover() || hasFocus());
            view.setSize(getSize());
            view.paint(source);
        }
    }
}
