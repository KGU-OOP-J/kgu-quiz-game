package org.kgu.integration.battle;

import java.util.Objects;
import org.kgu.domain.battle.feature.BattleEngine;
import org.kgu.integration.collection.CardSave;

/** Test adapter for the team's card store; production battle code does not depend on it. */
public final class BattleService {
    @FunctionalInterface
    public interface RewardRecorder {
        boolean record(String playerId, String field, String name, String difficulty);
    }

    private final RewardRecorder recorder;

    public BattleService() {
        this(CardSave::record);
    }

    public BattleService(RewardRecorder recorder) {
        this.recorder = Objects.requireNonNull(recorder);
    }

    public BattleEngine start(BattleRepository.Entry target) {
        return new BattleEngine(target.character(), target.questions(),
                BattleEngine.defaultRequiredCorrect(target.questions().size()));
    }

    /** CardSave deduplicates a repeated successful save for the same person and difficulty. */
    public boolean saveReward(String playerId, BattleEngine engine) {
        if (engine.phase() != BattleEngine.Phase.FINISHED || !engine.won()) {
            return false;
        }
        return recorder.record(playerId, engine.target().category(), engine.target().name(),
                engine.current().difficulty());
    }
}
