package org.kgu.domain.game.event;

import org.kgu.domain.game.feature.BattleEngine;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.core.model.Person;

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

    public org.kgu.domain.game.feature.GameSession session() { return gameFeature.getSession(); }
    public void useItem(org.kgu.core.progression.ShopItem item) throws java.io.IOException { gameFeature.useItem(item); }
    public org.kgu.core.progression.PlayerProgress profile() { return gameFeature.profile(); }

    public void select(int choice) {
        gameFeature.selectBattleAnswer(choice);
    }

    public void submit() {
        boolean previouslyUnlocked = gameFeature.profile().professorUnlocked();
        gameFeature.submitBattleAnswer();
        if (!previouslyUnlocked && gameFeature.profile().professorUnlocked()) org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.UNLOCK);
        org.kgu.core.window.GameResources.play(getBattle().feedback().correct()
            ? org.kgu.core.window.GameResources.Sound.CORRECT : org.kgu.core.window.GameResources.Sound.WRONG);
        if (getBattle().feedback().correct()) org.kgu.core.window.GameResources.play(org.kgu.core.window.GameResources.Sound.ATTACK);
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
