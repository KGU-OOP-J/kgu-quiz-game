package org.kgu.domain.game.view.layout;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiButton;
import org.kgu.core.window.UiStyle;
import org.kgu.domain.collection.view.component.CardView;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.domain.game.view.component.BattleButton;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

public final class PracticeLayout extends JPanel {
    public PracticeLayout(GameSession session, CardView card, IntConsumer select,
                          Runnable hint, Runnable submit, Runnable next, Runnable giveUp) {
        super(new BorderLayout(24, 20));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(32, 45, 32, 45));
        boolean asking = session.getPhase() == GameSession.Phase.QUESTION;

        add(progressSection(session), BorderLayout.NORTH);
        add(questionContainer(session, card, select, asking), BorderLayout.CENTER);
        add(actionSection(session, asking, hint, asking ? submit : next, giveUp), BorderLayout.SOUTH);
    }

    private JPanel progressSection(GameSession session) {
        JPanel panel = GameTheme.clear(new GridLayout(2, 1, 0, 8));
        panel.add(label(session.getCategory() + " · " + session.currentEntry().difficulty() + " · " + session.getMode(), 26));
        JProgressBar bar = new JProgressBar(0, session.getQuestionCount());
        bar.setValue(session.getQuestionIndex() + 1);
        bar.setStringPainted(true);
        bar.setString("문제 " + (session.getQuestionIndex() + 1) + "/" + session.getQuestionCount());
        bar.setFont(UiStyle.FONT.deriveFont(18f));
        panel.add(bar);
        return panel;
    }

    private JPanel questionContainer(GameSession session, CardView card, IntConsumer select, boolean asking) {
        JPanel main = GameTheme.clear(new BorderLayout(30, 0));
        JPanel portrait = new JPanel(null) {
            @Override public void doLayout() {
                int height = Math.min(448, getHeight() - 16);
                int width = Math.min(getWidth() - 16, (int)(height * .72));
                card.setBounds((getWidth() - width) / 2, (getHeight() - height) / 2,
                    Math.max(0, width), Math.max(0, height));
            }
        };
        portrait.setOpaque(false);
        portrait.setPreferredSize(new Dimension(320, 448));
        portrait.add(card);
        main.add(portrait, BorderLayout.WEST);

        JPanel questionArea = GameTheme.clear(new BorderLayout(0, 16));
        JTextArea question = new JTextArea(session.getCurrentQuiz().getQuestion());
        question.setEditable(false);
        question.setLineWrap(true);
        question.setWrapStyleWord(true);
        question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 26f));
        question.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        question.setForeground(ComponentPainter.TEXT);
        question.setBackground(Color.WHITE);
        JScrollPane scroll = GameTheme.scroll(question);
        scroll.setPreferredSize(new Dimension(600, 155));
        questionArea.add(scroll, BorderLayout.NORTH);
        questionArea.add(answerSection(session, select, asking), BorderLayout.CENTER);
        main.add(questionArea, BorderLayout.CENTER);
        return main;
    }

    private JPanel answerSection(GameSession session, IntConsumer select, boolean asking) {
        JPanel options = GameTheme.clear(new GridLayout(4, 1, 0, 12));
        for (int i = 0; i < 4; i++) {
            int answer = i + 1;
            var button = new BattleButton(answer + ". " + session.getCurrentQuiz().getChoices().get(i), true, false);
            button.setName("practiceChoice" + i);
            button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            button.setEnabled(asking);
            boolean selected = answer == session.getSelectedAnswer();
            boolean correct = answer == session.getCurrentQuiz().getAnswer();
            button.putClientProperty("selected", selected);
            button.putClientProperty("correct", !asking && correct);
            button.putClientProperty("wrong", !asking && selected && !correct);
            button.addActionListener(e -> select.accept(answer));
            options.add(button);
            bind("answer" + i, KeyStroke.getKeyStroke((char)('1' + i)), button);
        }
        return options;
    }

    private JPanel actionSection(GameSession session, boolean asking, Runnable hint, Runnable submit, Runnable giveUp) {
        JPanel bottom = GameTheme.clear(new GridLayout(2, 1, 0, 12));
        int answer = session.getCurrentQuiz().getAnswer();
        String message = asking ? "연습에서는 힌트를 자유롭게 볼 수 있습니다. 정답을 맞히면 단계 카드를 얻습니다."
            : (session.getSelectedAnswer() == answer ? "정답! " : "오답 — ") + "정답: " + session.getCurrentQuiz().getChoices().get(answer - 1);
        bottom.add(label(message, 18));
        JPanel buttons = GameTheme.clear(new GridLayout(1, 3, 20, 0));
        JButton hintButton = button("힌트 보기", hint);
        hintButton.setEnabled(asking);
        JButton action = button(asking ? "정답 제출" : "다음 문제", submit);
        action.setName("practiceSubmit");
        buttons.add(hintButton);
        buttons.add(action);
        buttons.add(button("연습 그만", giveUp));
        bottom.add(buttons);
        bind("submit", KeyStroke.getKeyStroke("ENTER"), action);
        return bottom;
    }

    private void bind(String name, KeyStroke key, JButton button) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                button.doClick();
            }
        });
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new UiButton(text, GameTheme.TEAL);
        button.putClientProperty("secondaryAction", !text.equals("정답 제출") && !text.equals("다음 문제"));
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f));
        button.addActionListener(event -> action.run());
        return button;
    }

    private static JLabel label(String text, int size) {
        JLabel label = GameTheme.label(text, size, ComponentPainter.TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }
}
