package org.kgu.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.integration.collection.CardRepository;
import org.kgu.integration.collection.CardSave;
import org.kgu.ui.battle.BattlePanel;
import org.kgu.integration.battle.*;

class BattleIntegrationTest {
    @TempDir Path temporary;
    private BattleEngine engine() {
        var people = new BattleRepository().load("교수님", "중");
        return new BattleService().start(people.get(0), people, new Random(7));
    }
    private void finish(BattleEngine battle, boolean correct) {
        while (battle.phase() != BattleEngine.Phase.FINISHED) {
            battle.select((battle.current().answerIndex() + (correct ? 0 : 1)) % 4);
            battle.submit(); battle.advance();
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
    @Test void allCorrectStillPlaysSixTurnsAndWinsTwoCards() {
        var battle = engine();
        for (int i = 0; i < 5; i++) {
            battle.select(battle.current().answerIndex()); battle.submit(); battle.advance();
        }
        assertEquals(0, battle.computerHp());
        assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
        finish(battle, true);
        assertEquals(6, battle.turn()); assertEquals(6, battle.correctCount());
        assertEquals(BattleEngine.Result.WIN, battle.result());
        assertEquals(2, battle.rewardIds().size());
    }
    @Test void allWrongLosesWithoutRewards() {
        var battle = engine(); finish(battle, false);
        assertEquals(0, battle.playerHp()); assertEquals(60, battle.computerHp());
        assertEquals(BattleEngine.Result.LOSE, battle.result());
        AtomicInteger saves = new AtomicInteger();
        new BattleService((id, field, name, difficulty) -> { saves.incrementAndGet(); return true; }).saveRewards("x", battle);
        assertEquals(0, saves.get());
    }
    @Test void failedAttackAndSuccessfulDefenseDrawWithoutRewards() {
        var battle = engine();
        while (battle.phase() != BattleEngine.Phase.FINISHED) {
            battle.select((battle.current().answerIndex() + (battle.attackTurn() ? 1 : 0)) % 4);
            battle.submit(); battle.advance();
        }
        assertEquals(BattleEngine.Result.DRAW, battle.result());
        assertTrue(battle.rewardIds().isEmpty());
    }
    @Test void inputLocksAndTurnOrderAreEnforced() {
        var battle = engine();
        assertThrows(IllegalStateException.class, battle::submit);
        assertThrows(IllegalStateException.class, battle::advance);
        assertThrows(IllegalStateException.class, battle::result);
        battle.select(battle.current().answerIndex()); battle.submit();
        int selected = battle.selected(); battle.select((selected + 1) % 4);
        assertEquals(selected, battle.selected());
        assertThrows(IllegalStateException.class, battle::submit);
        assertEquals(40, battle.computerHp()); battle.advance();
        assertEquals(-1, battle.selected());
        battle.select((battle.current().answerIndex() + 1) % 4); battle.submit();
        assertEquals(40, battle.playerHp());
    }
    @Test void realCollectionSaveIsIsolatedByIdDifficultyAndDeduplicated() throws Exception {
        String id = "battle-test-" + UUID.randomUUID();
        Path save = Path.of("saves", id + ".txt");
        try {
            var battle = engine(); finish(battle, true);
            var service = new BattleService(); service.saveRewards(id, battle); service.saveRewards(id, battle);
            assertEquals(2, Files.readAllLines(save).size());
            var collected = CardRepository.cards(id).stream().filter(c -> c.owned()).toList();
            assertEquals(2, collected.size());
            assertTrue(collected.stream().allMatch(c -> c.stages().equals(List.of(2))));
            assertTrue(CardSave.load(id + "-other").isEmpty());
        } finally { Files.deleteIfExists(save); }
    }
    @Test void retryAfterPartialSavePreservesFirstReward() {
        var battle = engine(); finish(battle, true);
        Set<String> saved = new HashSet<>(); AtomicInteger calls = new AtomicInteger();
        var service = new BattleService((id, field, name, difficulty) -> {
            if (calls.incrementAndGet() == 2) throw new IllegalStateException("disk failure");
            return saved.add(field + name + difficulty);
        });
        assertThrows(IllegalStateException.class, () -> service.saveRewards("x", battle));
        assertEquals(1, saved.size()); service.saveRewards("x", battle); assertEquals(2, saved.size());
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
    @Test void panelKeyboardCompletesBattleAndRendersAtScaledSize() throws Exception {
        AtomicInteger finished = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            var battle = engine();
            var panel = new BattlePanel(battle, finished::incrementAndGet, () -> {});
            panel.setSize(1280, 960);
            panel.doLayout();
            BufferedImage image = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_RGB);
            var graphics = image.createGraphics(); panel.paint(graphics); graphics.dispose();
            try {
                Path preview = Path.of(System.getProperty("battle.previewPath", "battle-preview.png")); Files.createDirectories(preview.getParent());
                ImageIO.write(image, "png", preview.toFile());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            for (int i = 0; i < 6; i++) {
                panel.getActionMap().get("choice" + battle.current().answerIndex()).actionPerformed(null);
                panel.getActionMap().get("confirm").actionPerformed(null);
                panel.getActionMap().get("confirm").actionPerformed(null);
            }
            assertEquals(BattleEngine.Result.WIN, battle.result());
        });
        assertEquals(1, finished.get());
    }
}
