package org.kgu.game.event;

import org.kgu.game.feature.GameFeature;
import org.kgu.game.feature.GameSession;
import org.kgu.game.view.QuestionView;

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
        if (view.isPracticeHintVisible()) { view.hidePracticeHint(); return; }
        if (getSession().getPhase() != GameSession.Phase.QUESTION) return;
        getSession().recordHint(); view.showPracticeHint();
    }

    public void submit(QuestionView view) {
        if (getSession().getSelectedAnswer() == 0) {
            JOptionPane.showMessageDialog(view, "정답을 선택해 주세요.");
            return;
        }
        try {
            boolean correct = gameFeature.submitAnswer();
            if (gameFeature.isSoundEnabled()) Toolkit.getDefaultToolkit().beep();
            view.showPractice(correct);
            if (correct) gameFeature.saveRewards();
        } catch (java.io.UncheckedIOException e) {
            JOptionPane.showMessageDialog(view, e.getMessage(), "답안 기록 실패", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "정답은 반영됐지만 보상 저장에 실패했습니다. 결과 화면에서 재시도할 수 있습니다.\n" + e.getMessage(), "보상 저장 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void next(QuestionView view) {
        gameFeature.nextQuestion();
        if (getSession().getPhase() != GameSession.Phase.FINISHED) view.showPractice();
    }

    public void giveUp(QuestionView view) {
        int answer = JOptionPane.showConfirmDialog(view, "연습을 그만두고 메인 화면으로 돌아갈까요?", "연습 종료", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                gameFeature.saveRewards();
                gameFeature.returnToMenu();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "보상을 저장하지 못해 연습 화면에 남습니다.\n" + e.getMessage(), "저장 실패", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
