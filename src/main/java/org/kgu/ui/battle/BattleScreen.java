package org.kgu.ui.battle;

import java.awt.*;
import javax.swing.*;
import org.kgu.ui.common.UiButton;
import org.kgu.ui.common.UiStyle;
import org.kgu.ui.navigation.GameNavigator;
import org.kgu.game.GameOutcome;

/** 대진 모드: 상대 카드의 퀴즈를 맞혀 카드를 획득하는 화면. */
public final class BattleScreen extends JPanel {
    private final GameNavigator navigator;
    private final JLabel question = new JLabel("<html>상대 카드의 인물은 상대성이론으로 유명한<br>과학자입니다. 누구일까요?</html>");

    public BattleScreen(GameNavigator navigator) {
        this.navigator = navigator; setPreferredSize(new Dimension(780, 576)); setLayout(null); setBackground(new Color(245, 248, 252));
        question.setBounds(45, 315, 690, 125); question.setForeground(UiStyle.NAVY); question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 21f)); question.setBorder(BorderFactory.createLineBorder(new Color(109, 142, 177), 2)); question.setVerticalAlignment(SwingConstants.CENTER); question.setHorizontalAlignment(SwingConstants.CENTER); question.setBackground(Color.WHITE); question.setOpaque(true); add(question);
        add(answer("1. 뉴턴", 35, false));
        add(answer("2. 아인슈타인", 215, true));
        add(answer("3. 갈릴레오", 395, false));
        add(answer("4. 다윈", 575, false));
    }

    private JButton answer(String text, int x, boolean correct) {
        UiButton button = new UiButton(text, new Color(36, 126, 238)); button.setBounds(x, 465, 165, 52); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f)); button.setForeground(Color.WHITE); button.addActionListener(e -> submit(correct)); return button;
    }
    private void submit(boolean correct) { if (correct) navigator.showAnswerResult(GameOutcome.CORRECT); else question.setText("<html>오답입니다. 카드를 얻으려면 정답을 선택하세요.</html>"); }

    @Override protected void paintComponent(Graphics source) {
        super.paintComponent(source); Graphics2D g = (Graphics2D) source.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(245, 248, 252)); g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); g.drawString("상대 카드 퀴즈", 325, 55);
        // 상대 카드만 중앙에 배치한다. 내 카드 및 양옆 카드 목록은 표시하지 않는다.
        g.setColor(Color.WHITE); g.fillRoundRect(300, 72, 180, 205, 12, 12); g.setColor(new Color(36, 126, 238)); g.drawRoundRect(300, 72, 180, 205, 12, 12);
        g.setColor(UiStyle.NAVY); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 52f)); g.drawString("?", 370, 175); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f)); g.drawString("상대 카드", 328, 235);
        g.dispose();
    }
}
