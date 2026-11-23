package org.kgu.ui.collection;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;

/** 한 분야의 20장 카드를 4장씩 넘겨 보는 카드 슬라이더. */
final class CardSliderPanel extends JPanel {
    private static final int CARDS_PER_PAGE = 4;
    private static final int TOTAL_CARDS = 20;
    private String field = "과학자";
    private int page = 0;
    private final JPanel cards = new JPanel(new GridLayout(2, 2, 12, 12));
    private final JLabel pageLabel = new JLabel("", SwingConstants.CENTER);

    CardSliderPanel() {
        setLayout(null); setOpaque(false);
        cards.setBounds(0, 0, 465, 250); cards.setOpaque(false); add(cards);
        UiButton previous = new UiButton("◀ 이전", new Color(102, 124, 149)); previous.setBounds(40, 270, 110, 40); previous.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 14f)); previous.setForeground(Color.WHITE); previous.addActionListener(e -> move(-1)); add(previous);
        pageLabel.setBounds(160, 270, 145, 40); pageLabel.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 15f)); add(pageLabel);
        UiButton next = new UiButton("다음 ▶", new Color(36, 126, 238)); next.setBounds(315, 270, 110, 40); next.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 14f)); next.setForeground(Color.WHITE); next.addActionListener(e -> move(1)); add(next);
        refresh();
    }

    void setField(String field) { this.field = field; page = 0; refresh(); }
    private void move(int offset) { int lastPage = TOTAL_CARDS / CARDS_PER_PAGE - 1; page = Math.max(0, Math.min(lastPage, page + offset)); refresh(); }
    private void refresh() {
        cards.removeAll(); int first = page * CARDS_PER_PAGE + 1;
        for (int number = first; number < first + CARDS_PER_PAGE; number++) {
            JButton card = new JButton(field + " 카드 " + number); card.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f)); card.setBackground(Color.WHITE); card.addActionListener(e -> JOptionPane.showMessageDialog(this, card.getText() + " 상세 화면으로 이동합니다.")); cards.add(card);
        }
        pageLabel.setText((page + 1) + " / " + (TOTAL_CARDS / CARDS_PER_PAGE)); cards.revalidate(); cards.repaint();
    }
}
