package org.kgu.ui.wrongnote;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 오답 노트 담당자가 이 클래스만 확장해 구현하는 화면 모듈. */
public final class WrongNoteScreen extends JPanel {
    public WrongNoteScreen(GameNavigator navigator) {
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        UiButton back = new UiButton("← 메뉴로", new Color(112, 128, 144)); back.setBounds(45, 485, 160, 55); back.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); back.setForeground(Color.WHITE); back.addActionListener(e -> navigator.showMenu()); add(back);
    }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(250, 246, 238)); g.fillRect(0, 0, getWidth(), getHeight()); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 35f)); UiStyle.center(g, "오답 노트", 390, 125); g.setFont(UiStyle.FONT.deriveFont(18f)); UiStyle.center(g, "오답 노트 화면은 담당 팀원이 구현합니다.", 390, 170); g.dispose(); }
}
