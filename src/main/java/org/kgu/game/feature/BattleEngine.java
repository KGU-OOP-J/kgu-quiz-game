package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import org.kgu.progression.CardPower;
import org.kgu.progression.ShopItem;
import java.util.*;

/** Tactical battle model. All combat choices are independent of Swing and persistence. */
public final class BattleEngine {
    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    public enum Result { WIN, LOSE, DRAW }
    public enum Action {
        ATTACK("일반 공격", "정답: 기본 피해 / 정답 시 반격 일부 회피"),
        HEAVY("강공격", "정답: 큰 피해 / 오답: 반격 +8"),
        DEFEND("방어·집중", "정답: 작은 피해·추가 자원 / 반격 -16");
        private final String label, description;
        Action(String label, String description) { this.label = label; this.description = description; }
        public String label() { return label; }
        public String description() { return description; }
    }
    public enum Skill { ANALYZE, SHIELD, FOCUS }
    public record TurnFeedback(boolean correct, boolean attackTurn, int damage, String explanation) { }
    private final Person player, computer;
    private final List<Quiz> questions;
    private final CardPower power;
    private final boolean boss;
    private final int maxPlayerHp, maxComputerHp;
    private final EnumSet<ShopItem> itemsUsed = EnumSet.noneOf(ShopItem.class);
    private final EnumSet<Skill> skillsUsed = EnumSet.noneOf(Skill.class);
    private final Set<Integer> eliminated = new HashSet<>();
    private int turn, playerHp, computerHp, selected = -1, correctCount, combo, bestCombo, hints;
    private int energy, shield, boost, lastIncoming;
    private Phase phase = Phase.QUESTION;
    private Action action = Action.ATTACK;
    private TurnFeedback feedback;

    public BattleEngine(Person player, Person target, List<Quiz> questions, CardPower power,
                        int level, Difficulty difficulty, boolean boss) {
        this.player = Objects.requireNonNull(player); this.computer = Objects.requireNonNull(target);
        this.questions = List.copyOf(questions); this.power = Objects.requireNonNull(power); this.boss = boss;
        if (questions.isEmpty()) throw new IllegalArgumentException("전투 문제가 필요합니다.");
        maxPlayerHp = 70 + Math.min(20, Math.max(0, level - 1) * 2)
            + (power.kind() == CardPower.Kind.GUARD ? power.strength() * 4 : 0);
        maxComputerHp = boss ? 110 + (difficulty.getStage() - 1) * 10 : 60 + (difficulty.getStage() - 1) * 10;
        playerHp = maxPlayerHp; computerHp = maxComputerHp;
        energy = Math.min(10, 2 + (power.kind() == CardPower.Kind.ANALYSIS ? power.strength() : 0));
    }
    public Person player() { return player; }
    public Person computer() { return computer; }
    public Phase phase() { return phase; }
    public int turn() { return turn; }
    public int questionCount() { return questions.size(); }
    public int playerHp() { return playerHp; }
    public int computerHp() { return computerHp; }
    public int maxPlayerHp() { return maxPlayerHp; }
    public int maxComputerHp() { return maxComputerHp; }
    public int selected() { return selected; }
    public int correctCount() { return correctCount; }
    public int combo() { return combo; }
    public int bestCombo() { return bestCombo; }
    public int hintsUsed() { return hints; }
    public int energy() { return energy; }
    public int lastIncoming() { return lastIncoming; }
    public CardPower power() { return power; }
    public Action action() { return action; }
    public boolean boss() { return boss; }
    public boolean attackTurn() { return true; }
    public Quiz current() { return questions.get(Math.min(turn, questions.size() - 1)); }
    public Person currentPerson() { return computer; }
    public Set<Integer> eliminated() { return Set.copyOf(eliminated); }
    public String hint() { return String.join(" / ", computer.getInformation()); }
    public String explanation() { return "정답: " + current().getChoices().get(current().getAnswer() - 1); }
    public String intent() {
        String prefix = boss && computerHp <= maxComputerHp / 2 ? "보스 격노(+4) · " : "";
        return prefix + switch (turn % 3) {
            case 0 -> "견제 공격 — 반격 " + incomingDamage();
            case 1 -> "힘 모으기 — 반격 " + incomingDamage() + ", 다음 강공격 예고" + (boss ? " · 보호막: 공격 피해 -8" : "");
            default -> "강공격! — 반격 " + incomingDamage() + " / 방어·보호 추천";
        };
    }
    public int incomingDamage() { return new int[]{12, 8, 24}[turn % 3] + (boss ? 5 : 0)
        + (boss && computerHp <= maxComputerHp / 2 ? 4 : 0); }
    public TurnFeedback feedback() { require(Phase.FEEDBACK); return feedback; }
    public void chooseAction(Action action) { require(Phase.QUESTION); this.action = Objects.requireNonNull(action); }
    public void select(int choice) {
        require(Phase.QUESTION);
        if (choice < 0 || choice >= 4 || eliminated.contains(choice)) throw new IllegalArgumentException("선택할 수 없는 보기입니다.");
        selected = choice;
    }
    public int skillCost(Skill skill) {
        return skill == Skill.ANALYZE && power.kind() == CardPower.Kind.ANALYSIS && power.strength() > 0 ? 1 : 2;
    }
    public void useSkill(Skill skill) {
        require(Phase.QUESTION);
        if (skillsUsed.contains(skill)) throw new IllegalArgumentException("같은 스킬은 턴당 한 번만 사용할 수 있습니다.");
        int cost = skillCost(skill);
        if (energy < cost) throw new IllegalArgumentException("스킬 자원이 부족합니다.");
        energy -= cost; skillsUsed.add(skill);
        switch (skill) {
            case ANALYZE -> { eliminate(); hints++; }
            case SHIELD -> shield += 15;
            case FOCUS -> boost += 15;
        }
    }
    public void validateItem(ShopItem item) {
        require(Phase.QUESTION);
        if (itemsUsed.contains(item)) throw new IllegalArgumentException("같은 아이템은 전투당 한 번만 사용할 수 있습니다.");
        if (item == ShopItem.TONIC && playerHp == maxPlayerHp) throw new IllegalArgumentException("HP가 가득 차 있습니다.");
        if (item == ShopItem.INSIGHT && eliminated.size() >= 3) throw new IllegalArgumentException("더 제거할 보기가 없습니다.");
    }
    public void useItem(ShopItem item) {
        validateItem(item); itemsUsed.add(item);
        switch (item) {
            case INSIGHT -> { eliminate(); hints++; }
            case SHIELD -> shield += 15;
            case TONIC -> playerHp = Math.min(maxPlayerHp, playerHp + 25);
        }
    }
    private void eliminate() {
        for (int i = 0; i < 4; i++) {
            if (i != current().getAnswer() - 1 && eliminated.add(i)) { if (selected == i) selected = -1; return; }
        }
    }
    public TurnFeedback submit() {
        require(Phase.QUESTION);
        if (selected < 0) throw new IllegalStateException("정답을 선택해 주세요.");
        boolean correct = selected + 1 == current().getAnswer();
        int damage = 0;
        if (correct) {
            correctCount++; combo++; bestCombo = Math.max(bestCombo, combo);
            damage = switch (action) { case ATTACK -> 18; case HEAVY -> 32; case DEFEND -> 10; };
            if (power.kind() == CardPower.Kind.POWER) damage += power.strength() * 2;
            damage += boost;
            if (boss && turn % 3 == 1) damage = Math.max(0, damage - 8);
            energy = Math.min(10, energy + 1 + (combo >= 3 ? 1 : 0) + (action == Action.DEFEND ? 2 : 0)
                + (combo >= 2 && power.kind() == CardPower.Kind.FOCUS && power.strength() > 0 ? 1 : 0));
        } else combo = 0;
        int incoming = incomingDamage();
        computerHp = Math.max(0, computerHp - damage);
        if (action == Action.DEFEND) incoming -= 16;
        if (action == Action.ATTACK && correct) incoming -= 6;
        if (action == Action.HEAVY && !correct) incoming += 8;
        if (power.kind() == CardPower.Kind.GUARD) incoming -= power.strength();
        lastIncoming = computerHp == 0 ? 0 : Math.max(0, incoming - shield);
        playerHp = Math.max(0, playerHp - lastIncoming);
        shield = 0; boost = 0;
        feedback = new TurnFeedback(correct, true, damage, explanation() + " · 받은 피해 " + lastIncoming);
        phase = Phase.FEEDBACK;
        return feedback;
    }
    public void advance() {
        require(Phase.FEEDBACK);
        turn++; selected = -1; eliminated.clear(); skillsUsed.clear(); action = Action.ATTACK;
        phase = computerHp == 0 || playerHp == 0 || turn == questions.size() ? Phase.FINISHED : Phase.QUESTION;
    }
    public Result result() {
        require(Phase.FINISHED);
        return computerHp == 0 && playerHp > 0 ? Result.WIN : Result.LOSE;
    }
    public int stars() {
        if (phase != Phase.FINISHED || result() != Result.WIN) return 0;
        int answered = Math.max(1, turn);
        return 1 + (correctCount * 100 / answered >= 80 ? 1 : 0) + (hints == 0 ? 1 : 0);
    }
    public List<Person> rewards() { return result() == Result.WIN ? List.of(computer) : List.of(); }
    private void require(Phase expected) {
        if (phase != expected) throw new IllegalStateException("전투 단계가 올바르지 않습니다: " + phase);
    }
}
