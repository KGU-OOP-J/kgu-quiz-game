package org.kgu.domain.game.view.component;

import org.kgu.core.window.*;
import org.kgu.core.ui.ComponentPainter;
import javax.swing.*;
import java.awt.*;

public final class PracticeResultPanel extends JPanel {
    public PracticeResultPanel() {
        super(new GridBagLayout()); setOpaque(false);
        JPanel content = GameTheme.lightPanel(new BorderLayout(0, 24));
        content.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
        JLabel title = GameTheme.label("보상 없이 문제를 익히는 연습", 27, ComponentPainter.TEXT);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 27f));
        content.add(title, BorderLayout.NORTH);
        JTextArea message = new JTextArea("연습에서는 카드·XP·코인·숙련도를 얻지 않습니다.\n업적·수집률·해금 진행도에도 반영되지 않습니다.\n\n정답과 콤보는 이번 연습의 학습 결과입니다.\n오답 노트와 최근 출제 기록만 갱신합니다.\n\n카드는 배틀에서 승리했을 때만 획득합니다.");
        message.setEditable(false); message.setFocusable(false); message.setOpaque(false);
        message.setFont(UiStyle.FONT.deriveFont(19f)); message.setForeground(ComponentPainter.TEXT);
        message.setLineWrap(true); message.setWrapStyleWord(true);
        content.add(GameTheme.scroll(message), BorderLayout.CENTER);
        GridBagConstraints bounds = new GridBagConstraints();
        bounds.weightx = 1; bounds.weighty = 1; bounds.fill = GridBagConstraints.BOTH;
        add(content, bounds);
    }
}
