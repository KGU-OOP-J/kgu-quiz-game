package org.kgu.ui.start;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 문제 풀이 전 난이도 선택 화면. */
public final class DifficultyScreen extends JPanel {
    private final GameNavigator navigator;
    public DifficultyScreen(GameNavigator navigator) {
        this.navigator = navigator; setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        add(level("하  천천히 연습", 55, new Color(38, 172, 113)));
        add(level("중  기본 실력 도전", 290, new Color(42, 133, 245)));
        add(level("상  제한 시간 도전", 525, new Color(236, 91, 70)));
    }
    private JButton level(String text, int x, Color color) { UiButton button = new UiButton(text, color); button.setBounds(x, 210, 200, 230); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 25f)); button.setForeground(Color.WHITE); button.addActionListener(e -> navigator.showQuestion()); return button; }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(240, 248, 255)); g.fillRect(0, 0, getWidth(), getHeight()); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f)); UiStyle.center(g, "난이도를 선택하세요", 390, 115); g.setFont(UiStyle.FONT.deriveFont(18f)); UiStyle.center(g, "선택 후 문제 풀이가 시작됩니다.", 390, 150); g.dispose(); }
}
