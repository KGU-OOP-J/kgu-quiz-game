package org.kgu.domain.game.feature;

import org.kgu.core.model.Person;
import org.kgu.core.model.Quiz;
import org.kgu.core.progression.CardPower;
import org.kgu.core.progression.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class BattleEngine {
    public static final int QUESTION_COUNT = 5;
    public static final int REQUIRED_CORRECT = 3;
    public static final int MAX_HP = 60;
    public static final int DAMAGE = 20;

    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    public enum Result { WIN, LOSE }
    public record TurnFeedback(boolean correct, int damage, String explanation) { }

    private final Person target;
    private final List<Quiz> questions;
    private final CardPower power;
    private final boolean boss;
    private final Random random;
    private final List<BattlePhases.Stage> stages;
    private final Quiz supportQuestion;
    private int stageIndex, stageCorrect, stageSuccess, stageAnswered;
    private boolean supportUsed, guardPending, shieldTriggered;
    private String supportFeedback = "";
    private final Set<Integer> eliminated = new HashSet<>();
    private int turn;
    private int answeredCount;
    private int selected = -1;
    private int correctCount;
    private int combo;
    private int bestCombo;
    private int firstWrong = -1;
    private int retryCorrectCount;
    private boolean retry;
    private boolean supportRetry;
    private Quiz retryQuiz;
    private ShopItem itemUsed;
    private Phase phase = Phase.QUESTION;
    private TurnFeedback feedback;

    public BattleEngine(Person target, List<Quiz> questions, CardPower power, boolean boss) {
        this(target, questions, power, boss, new Random());
    }

    public BattleEngine(Person target, List<Quiz> questions, CardPower power, boolean boss, Random random) {
        this(target, questions, power, boss, random, null);
    }

    public BattleEngine(Person target, List<Quiz> questions, CardPower power, boolean boss, Random random, Quiz supportQuestion) {
        this.target = Objects.requireNonNull(target);
        this.questions = List.copyOf(questions);
        this.power = Objects.requireNonNull(power);
        this.boss = boss;
        this.random = Objects.requireNonNull(random);
        this.stages = BattlePhases.forBattle(target, boss);
        this.supportQuestion = supportQuestion;
        if (questions.size() != QUESTION_COUNT * stages.size()) {
            throw new IllegalArgumentException("전투 페이즈마다 서로 다른 5문제가 필요합니다.");
        }
    }

    public Person computer() { return target; }
    public Phase phase() { return phase; }
    public int turn() { return turn; }
    public int questionCount() { return questions.size(); }
    public int answeredCount() { return answeredCount; }
    public int selected() { return selected; }
    /** First-attempt answers only: used for XP, accuracy, combos and achievements. */
    public int correctCount() { return correctCount; }
    public int retryCorrectCount() { return retryCorrectCount; }
    public int successCount() { return stageSuccess; }
    public int stageCorrect() { return stageCorrect; }
    public int stageNumber() { return stageIndex + 1; }
    public int stageCount() { return stages.size(); }
    public int stageTurn() { return turn % QUESTION_COUNT; }
    public int requiredCorrect() { return stages.get(stageIndex).requiredCorrect(); }
    public BattlePhases.Stage stage() { return stages.get(stageIndex); }
    public String supportFeedback() { return supportFeedback; }
    public boolean supportAvailable() {
        return phase == Phase.QUESTION && !retry && !supportUsed && power.strength() > 0
            && (power.kind() == CardPower.Kind.ANALYSIS || power.kind() == CardPower.Kind.GUARD);
    }
    public String supportStatus() {
        if (power.strength() == 0) return "지원 카드 미장착";
        if (power.kind() == CardPower.Kind.POWER || power.kind() == CardPower.Kind.FOCUS) return "자동 적용 · " + power.description();
        return (supportUsed ? "지원 사용 완료 · " : "사용 가능 · ") + power.description();
    }
    public void useSupport() {
        if (!supportAvailable()) throw new IllegalStateException("지금은 카드 지원을 사용할 수 없습니다.");
        supportUsed = true;
        if (power.kind() == CardPower.Kind.ANALYSIS) {
            List<Integer> wrong = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (i != current().getAnswer() - 1 && !eliminated.contains(i)) wrong.add(i);
            Collections.shuffle(wrong, random);
            wrong.stream().limit(power.strength() >= 3 ? 2 : 1).forEach(eliminated::add);
            if (eliminated.contains(selected)) selected = -1;
            supportFeedback = "분석 지원 · 오답 보기 제거" + (power.strength() >= 2 ? "\n" + memoryText() : "");
        } else {
            guardPending = power.strength() >= 2 && supportQuestion != null;
            supportFeedback = guardPending ? "수호 지원 준비 · 실패 위기 시 새 보충 문제 1회" : "수호 지원 · " + memoryText();
            if (power.strength() >= 3) supportFeedback += "\n" + memoryText();
        }
    }
    public int bestCombo() { return bestCombo; }
    public int hintsUsed() { return (itemUsed == null ? 0 : 1) + (supportUsed ? 1 : 0); }
    public CardPower power() { return power; }
    public boolean boss() { return boss; }
    public boolean isRetry() { return retry; }
    public boolean isSupportRetry() { return retry && supportRetry; }
    public int currentQuestionIndex() { return retry ? firstWrong : Math.min(phase == Phase.FINISHED ? Math.max(0, turn - 1) : turn, questions.size() - 1); }
    public Quiz current() { return retry ? retryQuiz : questions.get(currentQuestionIndex()); }
    public Set<Integer> eliminated() { return Set.copyOf(eliminated); }
    public Optional<ShopItem> itemUsed() { return Optional.ofNullable(itemUsed); }
    public int maxComputerHp() { return requiredCorrect() * DAMAGE; }
    public int computerHp() { return Math.max(0, maxComputerHp() - successCount() * DAMAGE); }
    public boolean memoryVisible() { return itemUsed == ShopItem.TONIC; }
    public String memoryText() { return String.join("\n", target.getInformation()); }

    public TurnFeedback feedback() {
        require(Phase.FEEDBACK);
        return feedback;
    }

    public void select(int choice) {
        require(Phase.QUESTION);
        if (choice < 0 || choice >= 4 || eliminated.contains(choice)) {
            throw new IllegalArgumentException("선택할 수 없는 보기입니다.");
        }
        selected = choice;
    }

    /** Shared by UI availability and the pre-persistence validation in GameFeature. */
    public Optional<String> itemUnavailableReason(ShopItem item) {
        Objects.requireNonNull(item);
        if (phase != Phase.QUESTION) return Optional.of("답안을 제출하기 전에 사용하세요.");
        if (retry) return Optional.of("재도전에서는 아이템을 사용할 수 없습니다.");
        if (itemUsed != null) return Optional.of("한 전투에는 아이템 하나만 사용할 수 있습니다.");
        return Optional.empty();
    }

    public void validateItem(ShopItem item) {
        itemUnavailableReason(item).ifPresent(reason -> { throw new IllegalArgumentException(reason); });
    }

    public void useItem(ShopItem item) {
        validateItem(item);
        itemUsed = item;
        if (item == ShopItem.INSIGHT) {
            List<Integer> wrong = new ArrayList<>();
            for (int i = 0; i < 4; i++) if (i != current().getAnswer() - 1 && !eliminated.contains(i)) wrong.add(i);
            int removed = wrong.get(random.nextInt(wrong.size()));
            eliminated.add(removed);
            if (selected == removed) selected = -1;
        }
    }

    public TurnFeedback submit() {
        require(Phase.QUESTION);
        if (selected < 0) throw new IllegalStateException("정답을 선택해 주세요.");
        int previousHp = computerHp();
        boolean correct = selected + 1 == current().getAnswer();
        if (retry) {
            if (correct) { retryCorrectCount++; stageSuccess++; }
        } else {
            answeredCount++; stageAnswered++;
            if (correct) {
                correctCount++; stageCorrect++; stageSuccess++;
                bestCombo = Math.max(bestCombo, ++combo);
            } else {
                combo = 0;
                if (firstWrong < 0) firstWrong = turn;
            }
        }
        feedback = new TurnFeedback(correct, previousHp - computerHp(),
                "정답: " + current().getChoices().get(current().getAnswer() - 1));
        phase = Phase.FEEDBACK;
        return feedback;
    }

    public void advance() { advance(false); }

    /** The feature supplies inventory availability; the engine does not read saves. */
    public void advance(boolean shieldInInventory) {
        require(Phase.FEEDBACK);
        selected = -1;
        eliminated.clear();
        if (retry) {
            if (stageSuccess < requiredCorrect() && prepareRecovery()) phase = Phase.QUESTION;
            else completeStage();
            return;
        }
        turn++;
        int remaining = QUESTION_COUNT - stageAnswered;
        boolean retryPossible = firstWrong >= 0 && !shieldTriggered && (itemUsed == ShopItem.SHIELD
                || (itemUsed == null && shieldInInventory && remaining > 0));
        boolean guardPossible = guardPending && supportQuestion != null;
        if (stageSuccess + remaining + (retryPossible ? 1 : 0) + (guardPossible ? 1 : 0) < requiredCorrect()) {
            phase = Phase.FINISHED;
        } else if (stageAnswered < QUESTION_COUNT) {
            phase = Phase.QUESTION;
        } else if (prepareRecovery()) {
            phase = Phase.QUESTION;
        } else {
            completeStage();
        }
    }

    private boolean prepareRecovery() {
        if (itemUsed == ShopItem.SHIELD && !shieldTriggered && firstWrong >= stageIndex * QUESTION_COUNT
            && (stageSuccess < requiredCorrect() || !boss)) {
            prepareRetry(); return true;
        }
        if (guardPending && supportQuestion != null && stageSuccess < requiredCorrect()) {
            retryQuiz = supportQuestion; retry = true; supportRetry = true; guardPending = false;
            return true;
        }
        return false;
    }

    private void completeStage() {
        if (stageSuccess < requiredCorrect() || stageIndex == stages.size() - 1) { phase = Phase.FINISHED; return; }
        stageIndex++; stageCorrect = 0; stageSuccess = 0; stageAnswered = 0;
        turn = stageIndex * QUESTION_COUNT; retry = false; retryQuiz = null; firstWrong = -1;
        phase = Phase.QUESTION;
    }

    private void prepareRetry() {
        shieldTriggered = true;
        supportRetry = false;
        Quiz original = questions.get(firstWrong);
        // Non-zero rotation guarantees a new answer position, with no repeated original question credit.
        List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3));
        Collections.rotate(order, 1 + random.nextInt(3));
        retryQuiz = new Quiz(original.getDifficulty(), original.getQuestion(),
                order.stream().map(original.getChoices()::get).toList(),
                order.indexOf(original.getAnswer() - 1) + 1);
        retry = true;
    }

    public Result result() {
        require(Phase.FINISHED);
        return stageIndex == stages.size() - 1 && stageSuccess >= requiredCorrect() ? Result.WIN : Result.LOSE;
    }

    public int stars() {
        if (phase != Phase.FINISHED || result() != Result.WIN) return 0;
        return 1 + (correctCount >= questions.size() * .8 ? 1 : 0) + (itemUsed == null ? 1 : 0);
    }

    public List<Person> rewards() { return result() == Result.WIN ? List.of(target) : List.of(); }

    private void require(Phase expected) {
        if (phase != expected) throw new IllegalStateException("전투 단계가 올바르지 않습니다: " + phase);
    }
}
