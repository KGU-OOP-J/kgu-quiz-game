package org.kgu.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.integration.collection.CardRepository;
import org.kgu.integration.collection.CardSave;
import org.kgu.integration.battle.*;

class BattleIntegrationTest {
    @TempDir Path temporary;

    private BattleEngine engine() {
        return new BattleService().start(new BattleRepository().load("교수님", "중").get(0));
    }

    private void finish(BattleEngine battle, boolean correct) {
        while (battle.phase() != BattleEngine.Phase.FINISHED) {
            battle.select((battle.current().answerIndex() + (correct ? 0 : 1)) % 4);
            battle.submit();
            battle.advance();
        }
    }

    @Test void allTeamFieldsAndDifficultiesHavePlayableQuestionsAndArtwork() throws Exception {
        try (var archive = new java.util.zip.ZipFile("images/cards.zip", java.nio.charset.StandardCharsets.UTF_8)) {
        for (String field : List.of("과학자", "교수님", "한국사", "아이돌")) {
            for (String difficulty : List.of("하", "중", "상")) {
                var people = new BattleRepository().load(field, difficulty);
                assertEquals(20, people.size(), field + "/" + difficulty);
                for (var person : people) {
                    assertEquals(3, person.questions().size());
                    assertNotNull(archive.getEntry(person.character().imagePath().substring("images/".length())), person.character().imagePath());
                    for (var q : person.questions()) {
                        assertEquals(person.character().id(), q.characterId());
                        assertEquals(difficulty, q.difficulty());
                        assertTrue(q.answerIndex() >= 0 && q.answerIndex() < 4);
                    }
                }
            }
        }
    }
    }
    @Test void missingAndMalformedDataFailWithoutInventingQuestions() throws Exception {
        var repository = new BattleRepository(temporary);
        assertThrows(IllegalStateException.class, () -> repository.load("교수님", "중"));
        Files.writeString(temporary.resolve("교수님.txt"), "broken\n중\t문제\t1\t2\t3\t4\t9\n");
        assertTrue(repository.load("교수님", "중").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repository.load("../x", "중"));
    }
    @Test void questionDataCannotBeMutatedFromOutside() {
        var q = engine().current();
        String original = q.options()[0];
        q.options()[0] = "changed";
        assertEquals(original, q.options()[0]);
    }

    @Test void victorySavesOneCardWithIdAndDifficultyIsolation() throws Exception {
        String id = "battle-test-" + UUID.randomUUID();
        Path save = Path.of("saves", id + ".txt");
        try {
            var battle = engine();
            finish(battle, true);
            var service = new BattleService();
            service.saveReward(id, battle);
            service.saveReward(id, battle);
            assertEquals(1, Files.readAllLines(save).size());
            var collected = CardRepository.cards(id).stream().filter(card -> card.owned()).toList();
            assertEquals(1, collected.size());
            assertEquals(List.of(2), collected.get(0).stages());
            assertTrue(CardSave.load(id + "-other").isEmpty());
        } finally {
            Files.deleteIfExists(save);
        }
    }

    @Test void unfinishedAndLostBattlesNeverSave() {
        AtomicInteger calls = new AtomicInteger();
        var service = new BattleService((id, field, name, difficulty) -> {
            calls.incrementAndGet();
            return true;
        });
        var battle = engine();
        assertFalse(service.saveReward("test", battle));
        finish(battle, false);
        assertFalse(service.saveReward("test", battle));
        assertEquals(0, calls.get());
    }
}
