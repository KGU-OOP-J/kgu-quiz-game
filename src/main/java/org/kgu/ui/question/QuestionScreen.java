package org.kgu.ui.question;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;
import org.kgu.game.GameOutcome;

/** 현재 문제와 힌트를 표시하는 최소 문제 풀이 화면. */
public final class QuestionScreen extends JPanel {
    public QuestionScreen(GameNavigator navigator) {
        setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        JTextField answer = new JTextField("교수님 이름을 입력하세요"); answer.setBounds(330, 372, 390, 48); answer.setFont(UiStyle.FONT.deriveFont(17f)); answer.setForeground(new Color(150, 158, 168)); answer.setBorder(new CompoundBorder(new LineBorder(new Color(165, 176, 190)), new EmptyBorder(1, 12, 1, 8))); add(answer);
        add(button("힌트 보기", 35, new Color(27, 125, 247), e -> JOptionPane.showMessageDialog(this, "힌트: 소프트웨어 관련 수업을 담당합니다.")));
        add(button("정답 제출", 285, new Color(16, 174, 112), e -> navigator.showAnswerResult(GameOutcome.CORRECT)));
        add(button("게임 포기", 535, new Color(126, 137, 151), e -> navigator.showAnswerResult(GameOutcome.INCORRECT)));
    }
    private JButton button(String text, int x, Color color, java.awt.event.ActionListener action) { UiButton button = new UiButton(text, color); button.setBounds(x, 455, 210, 56); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); button.setForeground(Color.WHITE); button.addActionListener(action); return button; }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D) source.create(); UiStyle.prepare(g); g.setColor(new Color(240, 248, 255)); g.fillRect(0, 0, getWidth(), getHeight()); UiStyle.round(g, 20, 110, 740, 415, 12, Color.WHITE); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); g.drawString("문제 3 / 10", 35, 55); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); g.drawString("남은 시간  00:37", 565, 55); UiStyle.round(g, 40, 135, 250, 285, 9, new Color(109, 148, 132)); UiStyle.round(g, 320, 135, 145, 38, 19, new Color(255, 233, 164)); g.setColor(new Color(42, 46, 51)); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); g.drawString("힌트 1 / 3", 345, 160); String[] hint = { "이 교수님은 소프트웨어 관련 수업을", "담당하고 계십니다.", "알고리즘과 자료구조를 가르치십니다." }; g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); int y = 210; for (String line : hint) { g.drawString(line, 330, y); y += 38; } g.dispose(); }
}
