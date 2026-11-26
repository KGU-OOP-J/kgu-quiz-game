package org.kgu.domain.game.feature;

import org.junit.jupiter.api.Test;
import org.kgu.core.application.Difficulty;
import org.kgu.core.model.Person;
import org.kgu.core.model.Quiz;
import org.kgu.core.progression.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class QuizBattleTest {
    private Person person(String name) { return new Person("과학자", name, List.of("소개", "1", "2", "3")); }
    private List<Quiz> questions(int count) {
        List<Quiz> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(new Quiz(Difficulty.LOW, "질문" + i, List.of("가", "나", "다", "라"), 1));
        return result;
    }
    private BattleEngine battle() {
        return new BattleEngine(person("상대"), questions(5), CardPower.beginner(), false, new Random(7));
    }
    private void answer(BattleEngine battle, boolean correct) {
        battle.select(correct ? battle.current().getAnswer() - 1 : battle.current().getAnswer() % 4);
        battle.submit();
        battle.advance();
    }

    @Test void threeCorrectDoesNotSkipTheLastTwoQuestions() {
        BattleEngine battle = battle();
        for (int i = 0; i < 3; i++) answer(battle, true);
        assertEquals(0, battle.computerHp());
        assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
        assertThrows(IllegalStateException.class, battle::result);
        assertThrows(IllegalStateException.class, battle::rewards);
        answer(battle, false);
        assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
        answer(battle, false);
        assertEquals(5, battle.answeredCount());
        assertEquals(BattleEngine.Result.WIN, battle.result());
        assertEquals(List.of(battle.computer()), battle.rewards());
    }

    @Test void impossibleVictoryEndsEarlyAndTwoCorrectIsNotEnough() {
        for (int correct = 0; correct <= 2; correct++) {
            BattleEngine battle = battle();
            for (int i = 0; battle.phase() != BattleEngine.Phase.FINISHED; i++) answer(battle, i < correct);
            assertEquals(correct + 3, battle.answeredCount());
            assertEquals(BattleEngine.Result.LOSE, battle.result());
            assertTrue(battle.rewards().isEmpty());
            assertEquals(0, battle.stars());
        }
    }

    @Test void exactlyFiveQuestionsRequiredForEveryBattle() {
        for (int count : List.of(0, 3, 4, 6, 8)) {
            assertThrows(IllegalArgumentException.class,
                () -> new BattleEngine(person("상대"), questions(count), CardPower.beginner(), true));
        }
    }

    @Test void damageCannotBeBoostedBySupportCardsOrBossMode() {
        for (CardPower.Kind kind : CardPower.Kind.values()) {
            BattleEngine battle = new BattleEngine(person("보스"), questions(5), new CardPower("지원", kind, 8, "support"), true);
            battle.select(0);
            assertEquals(20, battle.submit().damage());
            assertEquals(40, battle.computerHp());
        }
    }

    @Test void lensOnlyRemovesAWrongChoiceAndCannotBeStacked() {
        BattleEngine battle = battle();
        battle.useItem(ShopItem.INSIGHT);
        int removed = battle.eliminated().iterator().next();
        assertNotEquals(battle.current().getAnswer() - 1, removed);
        assertThrows(IllegalArgumentException.class, () -> battle.select(removed));
        for (ShopItem item : ShopItem.values()) assertThrows(IllegalArgumentException.class, () -> battle.useItem(item));
        answer(battle, true);
        assertTrue(battle.eliminated().isEmpty());
        assertTrue(battle.itemUnavailableReason(ShopItem.TONIC).isPresent());
    }

    @Test void memoryIsAvailableUntilTheBattleEndsButIsNotAnAnswer() {
        BattleEngine battle = battle();
        assertFalse(battle.memoryVisible());
        battle.useItem(ShopItem.TONIC);
        assertTrue(battle.memoryVisible());
        assertEquals(String.join("\n", battle.computer().getInformation()), battle.memoryText());
        answer(battle, true);
        assertTrue(battle.memoryVisible());
        assertEquals(1, battle.hintsUsed());
    }

    @Test void shieldRetriesFirstWrongAfterAllFiveWithAChangedAnswerPosition() {
        BattleEngine battle = battle();
        battle.useItem(ShopItem.SHIELD);
        for (int i = 0; i < 5; i++) answer(battle, i < 2);
        assertTrue(battle.isRetry());
        assertEquals(2, battle.currentQuestionIndex());
        assertEquals("질문2", battle.current().getQuestion());
        assertNotEquals(1, battle.current().getAnswer());
        assertEquals("가", battle.current().getChoices().get(battle.current().getAnswer() - 1));
        assertEquals(5, battle.answeredCount());
        answer(battle, true);
        assertEquals(BattleEngine.Result.WIN, battle.result());
        assertEquals(2, battle.correctCount());
        assertEquals(1, battle.retryCorrectCount());
        assertEquals(3, battle.successCount());
        assertEquals(1, battle.stars());
        assertEquals(2, battle.bestCombo());
    }

    @Test void retryCannotRescueAnImpossibleScoreOrCreateAnEndlessLoop() {
        BattleEngine impossible = battle();
        impossible.useItem(ShopItem.SHIELD);
        for (int i = 0; impossible.phase() != BattleEngine.Phase.FINISHED; i++) answer(impossible, i == 0);
        assertEquals(BattleEngine.Result.LOSE, impossible.result());
        assertFalse(impossible.isRetry());
        BattleEngine retry = battle();
        retry.useItem(ShopItem.SHIELD);
        for (int i = 0; i < 5; i++) answer(retry, i < 2);
        assertTrue(retry.isRetry());
        answer(retry, false);
        assertEquals(BattleEngine.Result.LOSE, retry.result());
        assertThrows(IllegalStateException.class, retry::advance);
    }

    @Test void unusedShieldInInventoryPreservesARealComebackOpportunity() {
        BattleEngine battle = battle();
        for (int i = 0; i < 3; i++) {
            battle.select(1); battle.submit(); battle.advance(true);
        }
        assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
        battle.useItem(ShopItem.SHIELD);
        answer(battle, true); answer(battle, true);
        assertTrue(battle.isRetry());
        answer(battle, true);
        assertEquals(BattleEngine.Result.WIN, battle.result());
        assertEquals(2, battle.correctCount());
    }

    @Test void unavailableOrAlreadySpentItemCannotDelayCertainDefeat() {
        for (boolean usedOther : List.of(false, true)) {
            BattleEngine battle = battle();
            if (usedOther) battle.useItem(ShopItem.INSIGHT);
            for (int i = 0; i < 3; i++) {
                int wrong = java.util.stream.IntStream.range(1, 4).filter(n -> !battle.eliminated().contains(n)).findFirst().orElseThrow();
                battle.select(wrong); battle.submit(); battle.advance(usedOther);
            }
            assertEquals(BattleEngine.Phase.FINISHED, battle.phase());
            assertEquals(3, battle.answeredCount());
            assertEquals("질문2", battle.current().getQuestion());
            assertEquals(BattleEngine.Result.LOSE, battle.result());
        }
    }

    @Test void shieldDoesNotAddAQuestionWhenThereWereNoMistakes() {
        BattleEngine battle = battle();
        battle.useItem(ShopItem.SHIELD);
        for (int i = 0; i < 5; i++) answer(battle, true);
        assertFalse(battle.isRetry());
        assertEquals(BattleEngine.Phase.FINISHED, battle.phase());
        assertEquals(2, battle.stars());
    }

    @Test void starsUseOriginalAccuracyAndItemUsage() {
        BattleEngine battle = battle();
        for (int i = 0; i < 5; i++) answer(battle, i < 4);
        assertEquals(3, battle.stars());
    }

    @Test void invalidInputsAndDoubleSubmissionsCannotChangeState() {
        BattleEngine battle = battle();
        assertThrows(IllegalStateException.class, battle::submit);
        assertThrows(IllegalStateException.class, battle::advance);
        assertThrows(IllegalArgumentException.class, () -> battle.select(4));
        battle.select(0);
        battle.submit();
        assertThrows(IllegalStateException.class, battle::submit);
        assertThrows(IllegalStateException.class, () -> battle.select(1));
        assertThrows(IllegalArgumentException.class, () -> battle.useItem(ShopItem.TONIC));
        assertEquals(1, battle.correctCount());
        assertEquals(40, battle.computerHp());
    }

    @Test void stableIdsAndRecentSuppressionImproveQuestionSelection() {
        Person p = person("인물"); Quiz q = questions(1).getFirst();
        var first = QuestionBank.entry(p, Difficulty.LOW, q);
        assertEquals(first.id(), QuestionBank.entry(p, Difficulty.LOW, q).id());
        var second = QuestionBank.entry(person("다른 인물"), Difficulty.LOW, q);
        assertNotEquals(first.id(), second.id());
        List<QuestionBank.Entry> chosen = QuestionBank.choose(List.of(first, first, second), 10, new PlayerProgress(), new Random(1), true);
        assertEquals(2, chosen.size());
        for (QuestionBank.Entry entry : chosen) assertEquals(q.getChoices().get(q.getAnswer() - 1), entry.quiz().getChoices().get(entry.quiz().getAnswer() - 1));
    }
}
