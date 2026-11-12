package org.kgu.integration.battle;

import org.kgu.battle.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.kgu.integration.collection.CardSave;

/** Owns battle creation and the collection adapter; screens never write save files. */
public final class BattleService {
    @FunctionalInterface
    public interface RewardRecorder {
        boolean record(String playerId, String field, String name, String difficulty);
    }
    private final RewardRecorder recorder;
    public BattleService() { this(CardSave::record); }
    public BattleService(RewardRecorder recorder) { this.recorder = recorder; }

    public BattleEngine start(BattleRepository.Entry player, List<BattleRepository.Entry> entries, Random random) {
        List<BattleRepository.Entry> opponents = entries.stream()
                .filter(e -> !e.character().id().equals(player.character().id())).toList();
        if (opponents.isEmpty()) throw new IllegalArgumentException("같은 난이도의 문제를 가진 인물이 2명 이상 필요합니다.");
        BattleRepository.Entry opponent = opponents.get(random.nextInt(opponents.size()));
        List<BattleQuestion> attack = new ArrayList<>(player.questions());
        List<BattleQuestion> defense = new ArrayList<>(opponent.questions());
        Collections.shuffle(attack, random);
        Collections.shuffle(defense, random);
        return new BattleEngine(player.character(), opponent.character(), attack, defense);
    }

    /** Repeating after partial failure is safe: CardSave deduplicates each person/difficulty. */
    public void saveRewards(String playerId, BattleEngine engine) {
        if (engine.result() != BattleEngine.Result.WIN) return;
        String difficulty = engine.current().difficulty();
        for (BattleCharacter person : List.of(engine.player(), engine.computer())) {
            recorder.record(playerId, person.category(), person.name(), difficulty);
        }
    }
}
