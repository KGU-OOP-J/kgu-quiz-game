package org.kgu.domain.battle.feature;

import java.util.*;

/** Single-target quiz battle. No Swing, navigation or persistence dependencies. */
public final class BattleEngine {
    public static final int MAX_HP = 60;

    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    private final BattleCharacter target;
    private final List<BattleQuestion> questions;
    private final int requiredCorrect;
    private int index;
    private int selected = -1;
    private int correctCount;
    private boolean lastCorrect;
    private int lastDamage;
    private Phase phase = Phase.QUESTION;

    public BattleEngine(BattleCharacter target, List<BattleQuestion> questions, int requiredCorrect) {
        this.target = Objects.requireNonNull(target);
        this.questions = List.copyOf(questions);
        if (!List.of("교수님", "과학자", "아이돌", "한국사").contains(target.category()))
            throw new IllegalArgumentException("지원하지 않는 카테고리입니다.");
        if (questions.isEmpty() || requiredCorrect < 1 || requiredCorrect > questions.size())
            throw new IllegalArgumentException("문제 수와 승리 조건을 확인하세요.");
        Set<String> ids = new HashSet<>();
        String difficulty = questions.get(0).difficulty();
        for (BattleQuestion q : questions) {
            if (!target.id().equals(q.characterId()) || !difficulty.equals(q.difficulty()) || !ids.add(q.id()))
                throw new IllegalArgumentException("동일 인물·난이도의 서로 다른 문제를 전달하세요.");
        }
        this.requiredCorrect = requiredCorrect;
    }
    /** Default is two thirds; callers can supply another threshold to the constructor. */
    public static int defaultRequiredCorrect(int questionCount) {
        if (questionCount < 1) throw new IllegalArgumentException("문제가 한 개 이상 필요합니다.");
        return questionCount - questionCount / 3;
    }

    public BattleCharacter target() { return target; }
    public int questionCount() { return questions.size(); }
    public int questionIndex() { return index; }
    public int requiredCorrect() { return requiredCorrect; }
    public int correctCount() { return correctCount; }
    public int selected() { return selected; }
    public Phase phase() { return phase; }
    public BattleQuestion current() { return questions.get(phase == Phase.FINISHED ? index - 1 : index); }
    public boolean lastCorrect() { return lastCorrect; }
    public int remainingHp() {
        return MAX_HP - (int) Math.min(MAX_HP, (long) correctCount * MAX_HP / requiredCorrect);
    }
    public int lastDamage() { return lastDamage; }
    public boolean defeated() { return remainingHp() == 0; }

    public void select(int answer) {
        if (phase == Phase.QUESTION && answer >= 0 && answer < 4) selected = answer;
    }

    public void submit() {
        if (phase != Phase.QUESTION || selected < 0) throw new IllegalStateException("답안을 먼저 선택하세요.");
        int previousHp = remainingHp();
        lastCorrect = selected == current().answerIndex();
        if (lastCorrect) correctCount++;
        lastDamage = previousHp - remainingHp();
        phase = Phase.FEEDBACK;
    }

    public void advance() {
        if (phase != Phase.FEEDBACK) throw new IllegalStateException("답안을 먼저 제출하세요.");
        index++;
        selected = -1;
        phase = defeated() || index == questionCount() ? Phase.FINISHED : Phase.QUESTION;
    }

    public boolean won() {
        if (phase != Phase.FINISHED) throw new IllegalStateException("마지막 공격 결과를 확인한 뒤 전투 결과를 확인하세요.");
        return defeated();
    }

    public Set<String> rewardIds() {
        return phase == Phase.FINISHED && won() ? Set.of(target.id()) : Set.of();
    }
}
