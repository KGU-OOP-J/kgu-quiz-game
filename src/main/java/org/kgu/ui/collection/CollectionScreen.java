package org.kgu.ui.collection;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 왼쪽 분야 선택과 오른쪽 카드 슬라이더로 구성된 카드 도감 화면. */
public final class CollectionScreen extends JPanel {
    private final CardSliderPanel slider = new CardSliderPanel();
    public CollectionScreen(GameNavigator navigator) {
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        slider.setBounds(255, 175, 465, 315); add(slider);
        String[] fields = { "과학자", "교수님", "한국사", "아이돌" }; int y = 190;
        for (String field : fields) { UiButton button = new UiButton(field, new Color(80, 123, 173)); button.setBounds(50, y, 150, 45); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f)); button.setForeground(Color.WHITE); button.addActionListener(e -> slider.setField(field)); add(button); y += 60; }
        UiButton back = new UiButton("메뉴로", new Color(112, 128, 144)); back.setBounds(50, 465, 150, 50); back.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f)); back.setForeground(Color.WHITE); back.addActionListener(e -> navigator.showMenu()); add(back);
    }
    @Override protected void paintComponent(Graphics s) { Graphics2D g = (Graphics2D) s.create(); UiStyle.prepare(g); g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight()); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f)); UiStyle.center(g, "카드 도감", 390, 90); g.setFont(UiStyle.FONT.deriveFont(17f)); UiStyle.center(g, "분야를 선택하고 카드를 넘겨보세요.", 390, 120); g.dispose(); }
}
