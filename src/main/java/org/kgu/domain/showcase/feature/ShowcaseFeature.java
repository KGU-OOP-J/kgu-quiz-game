package org.kgu.domain.showcase.feature;

import org.kgu.core.application.Difficulty;
import org.kgu.core.factory.SaveFactory;
import org.kgu.core.progression.PlayerProgress;
import org.kgu.core.window.ViewId;
import org.kgu.domain.game.feature.BattleEngine;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.domain.game.feature.QuestionBank;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public final class ShowcaseFeature {
    @FunctionalInterface
    public interface AnswerPlayback {
        void play(boolean correct, boolean continuous);
    }

    private final GameFeature game;
    private final SaveFactory save;
    private final List<QuestionBank.Entry> questions;
    private final Consumer<ViewId> show;
    private final Consumer<String> selectAdventureTab;
    private final AnswerPlayback playAnswers;

    public ShowcaseFeature(GameFeature game, SaveFactory save, List<QuestionBank.Entry> questions,
                           Consumer<ViewId> show, Consumer<String> selectAdventureTab, AnswerPlayback playAnswers) {
        this.game = game;
        this.save = save;
        this.questions = questions;
        this.show = show;
        this.selectAdventureTab = selectAdventureTab;
        this.playAnswers = playAnswers;
    }

    public void open(ShowcaseScene scene) throws IOException {
        switch (scene) {
            case MENU -> show.accept(ViewId.MENU);
            case TUTORIAL -> show.accept(ViewId.TUTORIAL);
            case MAP, SHOP, ACHIEVEMENTS, DECK, REVIEW -> openAdventure(scene);
            case COLLECTION -> {
                seedCard();
                show.accept(ViewId.COLLECTION);
            }
            case SETTINGS -> show.accept(ViewId.SETTING);
            case PRACTICE -> game.startPractice("과학자", Difficulty.LOW);
            case BATTLE, HIT, MISS, VICTORY, DEFEAT, RESCUE -> openBattle(scene);
            case RESULT -> openResult();
        }
    }

    private void openAdventure(ShowcaseScene scene) throws IOException {
        if (scene == ShowcaseScene.DECK) seedCard();
        if (scene == ShowcaseScene.REVIEW) {
            var entry = questions.getFirst();
            game.progress().answer(entry.id(), entry.person().getCategory(), false);
        }
        selectAdventureTab.accept(scene.tab());
        show.accept(ViewId.ADVENTURE);
    }

    private void seedCard() throws IOException {
        save.record(questions.getFirst().person(), Difficulty.LOW);
        game.progress().syncCollected(save.create().getCollectedCount());
    }

    private void openBattle(ShowcaseScene scene) throws IOException {
        boolean professor = scene == ShowcaseScene.RESCUE || scene == ShowcaseScene.DEFEAT;
        String category = professor ? "교수님" : "과학자";
        if (professor) {
            var solved = questions.stream().filter(entry -> entry.person().getCategory().equals("과학자"))
                .limit(PlayerProgress.PROFESSOR_REQUIRED).toList();
            for (var entry : solved) {
                game.progress().answer(entry.id(), entry.person().getCategory(), true);
            }
        }
        var target = game.getChallengeCandidates(category).getFirst();
        game.startBattle(category, Difficulty.LOW, target);
        if (scene == ShowcaseScene.VICTORY) game.getSession().markDialoguePlayed();
        switch (scene) {
            case HIT -> playAnswers.play(true, false);
            case MISS -> playAnswers.play(false, false);
            case VICTORY, RESCUE -> playAnswers.play(true, true);
            case DEFEAT -> playAnswers.play(false, true);
            default -> { }
        }
    }

    private void openResult() {
        String category = "과학자";
        game.startBattle(category, Difficulty.LOW, game.getChallengeCandidates(category).getFirst());
        BattleEngine battle = game.getSession().getBattle();
        while (battle.phase() == BattleEngine.Phase.QUESTION) {
            game.selectBattleAnswer(battle.current().getAnswer() - 1);
            game.submitBattleAnswer();
            game.advanceBattle();
        }
        game.getSession().markDialoguePlayed();
        show.accept(ViewId.RESULT);
    }
}
