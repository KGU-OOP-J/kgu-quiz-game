package org.kgu.game.event;

import org.kgu.game.feature.BattleEngine;
import org.kgu.game.feature.GameFeature;
import org.kgu.model.Person;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * 배틀 입력만 Feature로 전달한다. BattlePanel은 진행 상태를 출력한다.
 */
public final class BattleGUIEvent {

    private final GameFeature gameFeature;

    public BattleGUIEvent(GameFeature gameFeature) {
        this.gameFeature = gameFeature;
    }

    public BattleEngine getBattle() {
        return gameFeature.getSession().getBattle();
    }

    public void select(int choice) {
        gameFeature.selectBattleAnswer(choice);
    }

    public void submit() {
        gameFeature.submitBattleAnswer();
        if (gameFeature.isSoundEnabled())
            Toolkit.getDefaultToolkit().beep();
    }

    public void advance() {
        gameFeature.advanceBattle();
    }

    public void finish() {
        gameFeature.finishBattle();
    }

    public void returnToMenu() {
        gameFeature.returnToMenu();
    }

    public Optional<BufferedImage> image(String key) {
        return gameFeature.image(key);
    }

    public BufferedImage portrait(Person person) {
        String key = "cards/" + person.getName() + "_" + gameFeature.getSession().getDifficulty().getStage();
        return image(key).orElseThrow(() -> new IllegalStateException("인물 이미지가 없습니다: " + key));
    }
}
