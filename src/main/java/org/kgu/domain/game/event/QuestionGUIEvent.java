package org.kgu.domain.game.event;

import org.kgu.domain.game.feature.GameFeature;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.domain.game.view.QuestionView;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;

public final class QuestionGUIEvent {
    private final GameFeature gameFeature;

    public QuestionGUIEvent(GameFeature gameFeature) {
        this.gameFeature = gameFeature;
    }

    public GameSession getSession() {
        return gameFeature.getSession();
    }

    public Optional<BufferedImage> image(String key) {
        return gameFeature.image(key);
    }

    public void select(int answer) {
        gameFeature.selectAnswer(answer);
    }

    public void showHint(QuestionView view) {
        getSession().recordHint();
        view.showHint(String.join("\n", getSession().getCurrentPerson().getInformation()));
        org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.HINT);
    }

    public void submit(QuestionView view) {
        if (getSession().getSelectedAnswer() == 0) {
            JOptionPane.showMessageDialog(view, "정답을 선택해 주세요.");
            return;
        }
        try {
            boolean previouslyUnlocked = gameFeature.profile().professorUnlocked();
            boolean correct = gameFeature.submitAnswer();
            if (!previouslyUnlocked && gameFeature.profile().professorUnlocked()) org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.UNLOCK);
            org.kgu.core.window.GameResources.play(correct ? org.kgu.core.window.GameResources.Sound.CORRECT : org.kgu.core.window.GameResources.Sound.WRONG);
            if (correct && java.util.Set.of(3, 5, 10).contains(getSession().combo())) org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.COMBO);
            view.showPractice(correct);
        } catch (java.io.UncheckedIOException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "답안 기록 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void next(QuestionView view) {
        gameFeature.nextQuestion();
        if (getSession().getPhase() != GameSession.Phase.FINISHED) view.showPractice();
    }

    public void giveUp(QuestionView view) {
        JPanel message = org.kgu.core.window.GameTheme.clear(new BorderLayout());
        message.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        message.add(org.kgu.core.window.GameTheme.label("연습을 그만두고 메인 화면으로 돌아갈까요?", 16,
            org.kgu.core.ui.ComponentPainter.TEXT));
        String[] options = {"연습 계속", "메인 화면으로"};
        int answer = JOptionPane.showOptionDialog(view, message, "연습 종료", JOptionPane.DEFAULT_OPTION,
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (answer == 1) {
            org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.UI_CANCEL);
            if (getSession().getMode() == GameSession.Mode.PRACTICE) { gameFeature.returnToMenu(); return; }
            try {
                gameFeature.saveRewards();
                gameFeature.returnToMenu();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "보상을 저장하지 못해 연습 화면에 남습니다.\n" + e.getMessage(), "저장 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
