package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.event.QuestionGUIEvent;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Quiz;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

public final class QuestionView extends JPanel implements View {
    private final QuestionGUIEvent questionEvent;
    private final BattleGUIEvent battleEvent;
    private final CardArt art;
    private Optional<BattlePanel> battlePanel = Optional.empty();

    public QuestionView(QuestionGUIEvent questionEvent, BattleGUIEvent battleEvent) {
        this.questionEvent = questionEvent;
        this.battleEvent = battleEvent;
        art = new CardArt(questionEvent::image);
    }

    private static JTextArea text(String value, boolean bold) {
        JTextArea text = new JTextArea(value);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        text.setFocusable(false);
        text.setOpaque(false);
        text.setFont(UiStyle.FONT.deriveFont(bold ? Font.BOLD : Font.PLAIN, bold ? 17f : 16f));
        text.setForeground(UiStyle.NAVY);
        return text;
    }

    private static JScrollPane scroll(JTextArea text) {
        JScrollPane pane = new JScrollPane(text);
        pane.setBorder(null);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        return pane;
    }

    public void display() {
        if (questionEvent.getSession().getMode() == GameSession.Mode.PRACTICE) {
            showPractice();
        } else {
            removeAll();
            setLayout(new BorderLayout());
            BattlePanel panel = new BattlePanel(battleEvent.getBattle(), battleEvent, battleEvent::finish, battleEvent::returnToMenu);
            battlePanel = Optional.of(panel);
            add(panel, BorderLayout.CENTER);
            revalidate();
            repaint();
        }
    }

    public void showPractice() {
        GameSession session = questionEvent.getSession();
        Quiz quiz = session.getCurrentQuiz();
        boolean asking = session.getPhase() == GameSession.Phase.QUESTION;
        removeAll();
        setLayout(null);
        setBackground(new Color(239, 246, 255));
        JLabel header = new JLabel(session.getCategory() + " · " + session.getDifficulty() + " · 연습 모드");
        header.setBounds(35, 20, 500, 40);
        header.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f));
        add(header);
        JLabel progress = new JLabel("문제 " + (session.getQuestionIndex() + 1) + " / " + session.getQuestionCount(), SwingConstants.RIGHT);
        progress.setBounds(570, 20, 160, 40);
        progress.setFont(UiStyle.FONT);
        add(progress);
        JProgressBar bar = new JProgressBar(0, session.getQuestionCount());
        bar.setValue(session.getQuestionIndex() + 1);
        bar.setBounds(35, 78, 690, 10);
        add(bar);
        CardView card = new CardView(session.getCurrentPerson(), session.getDifficulty().getStage(), art);
        card.setBounds(60, 115, 210, 310);
        add(card);
        JTextArea question = text(quiz.getQuestion(), true);
        JScrollPane questionScroll = scroll(question);
        questionScroll.setBounds(315, 110, 430, 90);
        add(questionScroll);
        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBackground(Color.WHITE);
        ButtonGroup group = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            int answer = i + 1;
            JRadioButton option = new JRadioButton();
            option.setOpaque(false);
            option.setEnabled(asking);
            option.setSelected(session.getSelectedAnswer() == answer);
            group.add(option);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setBorder(new EmptyBorder(8, 8, 8, 8));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setBackground(!asking && quiz.getAnswer() == answer ? new Color(210, 255, 220) : Color.WHITE);
            JTextArea label = text(answer + ". " + quiz.getChoices().get(i), false);
            label.setSize(340, Short.MAX_VALUE);
            row.add(option, BorderLayout.WEST);
            row.add(label, BorderLayout.CENTER);
            row.setMaximumSize(new Dimension(410, Math.max(54, label.getPreferredSize().height + 20)));
            option.addActionListener(e -> questionEvent.select(answer));
            label.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (asking) option.doClick();
                }
            });
            options.add(row);
            options.add(Box.createVerticalStrut(4));
        }
        JScrollPane optionsScroll = new JScrollPane(options);
        optionsScroll.setBounds(315, 205, 430, 220);
        optionsScroll.setBorder(null);
        add(optionsScroll);
        JButton hint = button("힌트 보기", 35, new Color(75, 139, 224));
        hint.addActionListener(e -> questionEvent.showHint(this));
        JButton action = button(asking ? "정답 제출" : "다음 문제", 285, new Color(22, 164, 119));
        action.addActionListener(e -> {
            if (asking) questionEvent.submit(this);
            else questionEvent.next(this);
        });
        JButton quit = button("연습 그만", 535, new Color(135, 148, 166));
        quit.addActionListener(e -> questionEvent.giveUp(this));
        if (!asking) {
            boolean correct = session.getSelectedAnswer() == quiz.getAnswer();
            JLabel feedback = new JLabel((correct ? "정답입니다!" : "오답입니다.") + " 정답: " + quiz.getAnswer() + "번", SwingConstants.CENTER);
            feedback.setBounds(280, 430, 470, 30);
            feedback.setFont(UiStyle.FONT);
            add(feedback);
        }
        revalidate();
        repaint();
    }

    private JButton button(String title, int x, Color color) {
        JButton b = new UiButton(title, color);
        b.setBounds(x, 475, 180, 48);
        b.setForeground(Color.WHITE);
        b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f));
        add(b);
        return b;
    }

    public void stopAnimations() {
        battlePanel.ifPresent(BattlePanel::stopAnimation);
    }

    @Override
    public ViewId getViewId() {
        return ViewId.QUESTION;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return questionEvent.getSession().getMode() == GameSession.Mode.BATTLE ? new Dimension(1280, 960) : new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT);
    }
}
