package org.kgu.game.feature;

import org.junit.jupiter.api.Test;
import org.kgu.application.Difficulty;
import org.kgu.model.*;
import org.kgu.progression.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class TacticalBattleTest {
    private Person person(String name) { return new Person("과학자", name, List.of("소개", "1", "2", "3")); }
    private List<Quiz> questions(int count) {
        List<Quiz> result = new ArrayList<>();
        for (int i = 0; i < count; i++) result.add(Quiz.read(new Scanner("하\t질문" + i + "\t가\t나\t다\t라\t1\n")));
        return result;
    }
    private BattleEngine battle(CardPower power, boolean boss) {
        return new BattleEngine(person("탐험가"), person("상대"), questions(boss ? 8 : 5), power, 1, Difficulty.LOW, boss);
    }
    private void answer(BattleEngine b, BattleEngine.Action action, boolean correct) {
        b.chooseAction(action); b.select(correct ? 0 : 1); b.submit(); b.advance();
    }
    @Test void choicesChangeOutcomeWithTheSameNumberOfCorrectAnswers() {
        BattleEngine heavy = battle(CardPower.beginner(), false), normal = battle(CardPower.beginner(), false);
        for (int i = 0; i < 2; i++) answer(heavy, BattleEngine.Action.HEAVY, true);
        while (normal.phase() != BattleEngine.Phase.FINISHED) answer(normal, BattleEngine.Action.ATTACK, normal.turn() < 2);
        assertEquals(2, heavy.correctCount()); assertEquals(2, normal.correctCount());
        assertEquals(BattleEngine.Result.WIN, heavy.result()); assertEquals(BattleEngine.Result.LOSE, normal.result());
    }
    @Test void heavyMissIsRiskyAndDefenseCountersTelegraphedAttack() {
        BattleEngine heavy = battle(CardPower.beginner(), false), defense = battle(CardPower.beginner(), false);
        answer(heavy, BattleEngine.Action.HEAVY, false); answer(defense, BattleEngine.Action.DEFEND, false);
        assertEquals(20, heavy.lastIncoming()); assertEquals(0, defense.lastIncoming());
        assertTrue(defense.intent().contains("次") || defense.intent().contains("다음"));
    }
    @Test void skillsConsumeEnergyAndCannotBeRepeatedInATurn() {
        BattleEngine b = battle(CardPower.beginner(), false);
        b.select(1); b.useSkill(BattleEngine.Skill.ANALYZE);
        assertEquals(0, b.energy()); assertFalse(b.eliminated().contains(0)); assertEquals(1, b.hintsUsed());
        assertThrows(IllegalArgumentException.class, () -> b.useSkill(BattleEngine.Skill.ANALYZE));
        assertThrows(IllegalArgumentException.class, () -> b.useSkill(BattleEngine.Skill.FOCUS));
    }
    @Test void analysisSupportMakesSkillCheaperAndPowerChangesDamage() {
        BattleEngine analysis = battle(new CardPower("분석", CardPower.Kind.ANALYSIS, 2, "a"), false);
        assertEquals(1, analysis.skillCost(BattleEngine.Skill.ANALYZE)); assertEquals(4, analysis.energy());
        BattleEngine power = battle(new CardPower("돌파", CardPower.Kind.POWER, 3, "b"), false);
        answer(power, BattleEngine.Action.ATTACK, true); assertEquals(36, power.computerHp());
    }
    @Test void potionsAreLimitedAndDoNotHealBeyondMaximum() {
        BattleEngine b = battle(CardPower.beginner(), false);
        assertThrows(IllegalArgumentException.class, () -> b.useItem(ShopItem.TONIC));
        answer(b, BattleEngine.Action.HEAVY, false); b.useItem(ShopItem.TONIC);
        assertEquals(b.maxPlayerHp(), b.playerHp());
        assertThrows(IllegalArgumentException.class, () -> b.useItem(ShopItem.TONIC));
        b.useItem(ShopItem.INSIGHT); assertEquals(1, b.hintsUsed());
    }
    @Test void combosStarsAndBossRulesAreAuthoritative() {
        BattleEngine b = battle(CardPower.beginner(), true);
        while (b.phase() != BattleEngine.Phase.FINISHED) answer(b, BattleEngine.Action.HEAVY, true);
        assertEquals(BattleEngine.Result.WIN, b.result()); assertEquals(4, b.bestCombo()); assertEquals(3, b.stars());
        assertEquals(1, b.rewards().size()); assertEquals(110, b.maxComputerHp());
        assertThrows(IllegalStateException.class, b::submit);
    }
    @Test void bossShieldAndEnrageAreTelegraphedBeforeSubmission() {
        BattleEngine b = battle(CardPower.beginner(), true);
        answer(b, BattleEngine.Action.HEAVY, true);
        assertTrue(b.intent().contains("보호막"));
        b.chooseAction(BattleEngine.Action.HEAVY); b.select(0); assertEquals(24, b.submit().damage()); b.advance();
        assertTrue(b.intent().contains("격노")); assertEquals(33, b.incomingDamage());
    }
    @Test void noAnswersAndDoubleSubmissionsAreRejected() {
        BattleEngine b = battle(CardPower.beginner(), false);
        assertThrows(IllegalStateException.class, b::submit); assertThrows(IllegalStateException.class, b::result);
        b.select(0); b.submit(); assertThrows(IllegalStateException.class, b::submit);
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
