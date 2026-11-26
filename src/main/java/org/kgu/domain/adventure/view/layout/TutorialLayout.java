package org.kgu.domain.adventure.view.layout;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiButton;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;

public final class TutorialLayout extends JPanel {
    public TutorialLayout(JLabel title, JLabel indicator, JTextArea body, JPanel demo, JLabel demoResult,
                          JButton previous, JButton next, Runnable goBack, Runnable goNext, Runnable leave) {
        super(new BorderLayout(20, 24));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(56, 68, 44, 68));
        add(headerSection(title, indicator), BorderLayout.NORTH);
        add(pageContainer(body), BorderLayout.CENTER);

        JPanel footer = GameTheme.clear(new GridLayout(3, 1, 0, 12));
        buildDemoSection(demo, demoResult);
        footer.add(demo);
        footer.add(demoResult);
        footer.add(navigationSection(previous, next, goBack, goNext, leave));
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel headerSection(JLabel title, JLabel indicator) {
        JPanel header = GameTheme.clear(new GridLayout(0, 1, 0, 8));
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 32f));
        title.setForeground(ComponentPainter.TEXT);
        indicator.setForeground(GameTheme.BLUE_TEXT);
        indicator.setFont(UiStyle.FONT.deriveFont(16f));
        header.add(indicator);
        header.add(title);
        return header;
    }

    private JPanel pageContainer(JTextArea body) {
        body.setFont(UiStyle.FONT.deriveFont(22f));
        body.setEditable(false);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setOpaque(false);
        body.setForeground(ComponentPainter.TEXT);
        body.setMargin(new Insets(28, 32, 28, 32));
        JPanel page = GameTheme.lightPanel(new BorderLayout());
        page.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        page.add(GameTheme.scroll(body));
        return page;
    }

    private void buildDemoSection(JPanel demo, JLabel result) {
        demo.setOpaque(false);
        String[] actions = {"정답", "오답", "3개 정답"};
        String[] results = {"정답! 상대 HP -20 · 설명 확인 후 자동 진행", "오답 · 정답을 확인하고 다음 문제에 도전해요",
            "성공 조건 달성! 남은 문제까지 마치면 구출 확정"};
        for (int i = 0; i < actions.length; i++) {
            String feedback = results[i];
            JButton button = new UiButton(actions[i], GameTheme.TEAL);
            button.setName("tutorialDemo" + i);
            button.setFont(UiStyle.FONT.deriveFont(16f));
            button.addActionListener(event -> result.setText(feedback));
            demo.add(button);
        }
        result.setFont(UiStyle.FONT.deriveFont(19f));
        result.setForeground(GameTheme.BLUE_TEXT);
    }

    private JPanel navigationSection(JButton previous, JButton next, Runnable goBack, Runnable goNext, Runnable leave) {
        JPanel navigation = GameTheme.clear(new GridLayout(1, 3, 16, 0));
        JButton later = new UiButton("다음 로그인에 다시 보기", new Color(39, 53, 72));
        later.setName("tutorialLater");
        later.putClientProperty("secondaryAction", true);
        previous.putClientProperty("secondaryAction", true);
        later.addActionListener(event -> leave.run());
        previous.setName("tutorialPrevious");
        previous.addActionListener(event -> goBack.run());
        next.setName("tutorialNext");
        next.addActionListener(event -> goNext.run());
        for (JButton button : new JButton[]{later, previous, next}) {
            button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            navigation.add(button);
        }
        return navigation;
    }
}
