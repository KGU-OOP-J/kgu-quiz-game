package org.kgu.domain.game.view.component;

import org.kgu.domain.game.feature.GameSession.AnswerRecord;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** Read-only learning recap. Only submitted answers are shown, never unattempted questions. */
public final class LearningReviewPanel extends JPanel {
    public LearningReviewPanel(List<AnswerRecord> answers) {
        super(new BorderLayout(0, 16));
        setBackground(GameTheme.INK);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(760, 520));
        long correct = answers.stream().filter(AnswerRecord::correct).count();
        JLabel heading = GameTheme.label("푼 문제 " + answers.size() + "개 · 정답 " + correct + "개 · 오답 " + (answers.size() - correct) + "개", 23, GameTheme.PAPER);
        add(heading, BorderLayout.NORTH);
        JPanel rows = GameTheme.clear(null);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        // Show mistakes first while retaining the original question numbers.
        java.util.List<Integer> order = java.util.stream.IntStream.range(0, answers.size()).boxed()
            .sorted(java.util.Comparator.comparing(i -> answers.get(i).correct())).toList();
        for (int i : order) {
            AnswerRecord answer = answers.get(i);
            JPanel card = GameTheme.panel(new BorderLayout(0, 10));
            card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
            card.setAlignmentX(LEFT_ALIGNMENT);
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
            card.add(GameTheme.label((i + 1) + "  " + (answer.correct() ? "정답" : "다시 살펴봐요")
                    + " · " + answer.entry().person().getName(), 18, answer.correct() ? GameTheme.GOLD : new Color(255, 174, 155)), BorderLayout.NORTH);
            JTextArea question = text(answer.entry().quiz().getQuestion(), 21, GameTheme.PAPER);
            question.setRows(2); card.add(question, BorderLayout.CENTER);
            JPanel feedback = GameTheme.clear(new GridLayout(0, 1, 0, 6));
            if (!answer.correct()) feedback.add(text("내 답: " + answer.selectedText(), 17, GameTheme.MUTED));
            feedback.add(text("정답: " + answer.correctText(), 18, GameTheme.GOLD));
            card.add(feedback, BorderLayout.SOUTH);
            rows.add(card); rows.add(Box.createVerticalStrut(12));
        }
        JScrollPane scroll = GameTheme.scroll(rows);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
        add(GameTheme.label("틀린 문제는 결과 화면의 ‘이번 오답 복습’에서 다시 풀 수 있어요.", 16, GameTheme.MUTED), BorderLayout.SOUTH);
    }
    private static JTextArea text(String value, int size, Color color) {
        JTextArea area = new JTextArea(value);
        area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true);
        area.setOpaque(false); area.setForeground(color); area.setFont(UiStyle.FONT.deriveFont((float) size));
        return area;
    }
}
