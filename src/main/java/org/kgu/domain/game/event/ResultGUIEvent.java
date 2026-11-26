package org.kgu.domain.game.event;

import org.kgu.domain.game.feature.BattleEngine;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.domain.game.view.ResultPresentation;
import org.kgu.domain.game.view.ResultPresentation.CardReward;
import org.kgu.domain.game.view.ResultPresentation.CardStatus;
import org.kgu.domain.game.view.ResultView;
import org.kgu.core.model.Person;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class ResultGUIEvent {
    private final GameFeature gameFeature;
    public ResultGUIEvent(GameFeature gameFeature){this.gameFeature=gameFeature;}
    public void refresh(ResultView view){
        try{
            gameFeature.saveRewards();
            if (gameFeature.beginRescueDialogue()) return;
            view.display(presentation(gameFeature.getSession(), gameFeature.getSavedRewards(),
                    "다음 목표: " + gameFeature.nextGoal(), false));
        }catch(IOException e){
            view.display(presentation(gameFeature.getSession(), gameFeature.getSavedRewards(),
                    "보상 저장 실패: " + e.getMessage(), true));
        }
    }

    public static ResultPresentation presentation(GameSession session, List<Person> savedRewards,
                                                   String statusMessage, boolean saveFailed) {
        Optional<ResultPresentation.BattleSummary> battleSummary = Optional.empty();
        if (session.getMode() == GameSession.Mode.BATTLE) {
            BattleEngine battle = session.getBattle();
            battleSummary = Optional.of(new ResultPresentation.BattleSummary(
                    battle.result() == BattleEngine.Result.WIN, battle.computer().getCategory().equals("교수님"),
                    battle.computerHp(), battle.maxComputerHp(), battle.bestCombo(), battle.retryCorrectCount()));
        }
        List<CardReward> cards = savedRewards.stream()
                .map(person -> new CardReward(person, session.isNewCard(person) ? CardStatus.NEW : CardStatus.ALREADY_OWNED))
                .toList();
        return new ResultPresentation(session.id(), session.getMode().toString(), battleSummary,
                session.getCorrectCount(), session.getAnsweredCount(), session.getQuestionCount(), cards,
                session.getDifficulty().getStage(), session.rewardSettlement(), statusMessage, saveFailed);
    }

    public Optional<BufferedImage> image(String key){return gameFeature.image(key);}
    public void returnToMenu(){gameFeature.returnToMenu();}
    public void openCollection(){gameFeature.openCollection();}
    public void openAdventure(){gameFeature.openAdventure();}
}
