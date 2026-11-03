package org.kgu.ui.start;

import java.awt.*;
import javax.swing.*;
import org.kgu.game.PlayerSession;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 게임 시작 전 분야·난이도·모드를 고르는 화면. */
public final class GameSetupScreen extends JPanel {
    private final GameNavigator navigator;
    private final JComboBox<String> field = new JComboBox<>(new String[] { "과학자", "교수님", "한국사", "아이돌" });
    private final JComboBox<String> difficulty = new JComboBox<>(new String[] { "하", "중", "상" });
    private final JComboBox<String> mode = new JComboBox<>(new String[] { "연습", "대진" });
    public GameSetupScreen(GameNavigator navigator) {
        this.navigator = navigator; setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        addChoice("분야 선택", field, 190); addChoice("난이도 선택", difficulty, 285); addChoice("게임 모드", mode, 380);
        UiButton start = new UiButton("게임 시작", new Color(33, 127, 239)); start.setBounds(245, 475, 290, 58); start.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); start.setForeground(Color.WHITE); start.addActionListener(e -> begin()); add(start);
    }
    private void addChoice(String label, JComboBox<String> box, int y) { JLabel title = new JLabel(label); title.setBounds(205, y, 160, 42); title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); add(title); box.setBounds(370, y, 205, 42); box.setFont(UiStyle.FONT.deriveFont(17f)); add(box); }
    private void begin() { PlayerSession s = navigator.getSession(); s.setField((String) field.getSelectedItem()); s.setDifficulty((String) difficulty.getSelectedItem()); s.setMode((String) mode.getSelectedItem()); if (s.getMode().equals("대진")) navigator.showBattle(); else navigator.showQuestion(); }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight()); UiStyle.round(g, 150, 100, 480, 465, 16, Color.WHITE); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f)); UiStyle.center(g, "게임 준비", 390, 155); g.setFont(UiStyle.FONT.deriveFont(17f)); UiStyle.center(g, "분야, 난이도, 게임 모드를 선택하세요.", 390, 180); g.dispose(); }
}
