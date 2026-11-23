package org.kgu.ui.answer;

import java.awt.*;
import javax.swing.*;
import org.kgu.game.GameOutcome;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;

/** 정답 여부와 카드 획득 결과를 표시하는 독립 화면 모듈. */
public final class AnswerResultScreen extends JPanel {
    private final GameOutcome outcome;
    public AnswerResultScreen(GameNavigator navigator, GameOutcome outcome) {
        this.outcome = outcome; setPreferredSize(new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT)); setLayout(null);
        UiButton end = new UiButton("메인으로", new Color(36, 126, 238)); end.setBounds(290, 455, 200, 58); end.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f)); end.setForeground(Color.WHITE); end.addActionListener(e -> navigator.showMenu()); add(end);
    }
    @Override protected void paintComponent(Graphics s) { Graphics2D g = (Graphics2D) s.create(); UiStyle.prepare(g); boolean correct = outcome == GameOutcome.CORRECT; g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight()); UiStyle.round(g, 180, 120, 420, 390, 16, Color.WHITE); g.setColor(correct ? new Color(20, 154, 98) : new Color(218, 82, 70)); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 38f)); UiStyle.center(g, correct ? "정답!" : "오답", 390, 200); g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 21f)); UiStyle.center(g, correct ? "획득 카드 1장" : "획득 카드 없음", 390, 270); UiStyle.round(g, 310, 300, 160, 105, 10, new Color(234, 239, 246)); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); UiStyle.center(g, correct ? "새 카드" : "다음 기회에!", 390, 360); g.dispose(); }
}
