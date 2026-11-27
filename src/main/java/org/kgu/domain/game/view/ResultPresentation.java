package org.kgu.domain.game.view;

import org.kgu.domain.game.feature.GameSession.RewardSettlement;
import org.kgu.core.model.Person;

import java.util.List;
import java.util.Optional;

/** Data prepared for a single result screen. */
public record ResultPresentation(String sessionId, String modeLabel, Optional<BattleSummary> battle,
                                 int correctCount, int answeredCount, int questionCount,
                                 List<CardReward> cardRewards, int cardStage, Optional<RewardSettlement> settlement,
                                 String statusMessage, boolean saveFailed, boolean practice) {
    public ResultPresentation {
        cardRewards = List.copyOf(cardRewards);
        if (practice) { cardRewards = List.of(); settlement = Optional.empty(); saveFailed = false; }
    }

    public enum CardStatus { NEW, ALREADY_OWNED }

    public record CardReward(Person person, CardStatus status) { }

    public record BattleSummary(boolean victory, boolean professor, int opponentHp, int maxOpponentHp,
                                int bestCombo, int retryCorrectCount) { }
}
