package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import java.util.*;

/** A single run. Its UUID is also the persistent reward idempotency key. */
public final class GameSession {
    public enum Mode {
        PRACTICE("연습"), BATTLE("배틀"), REVIEW("오답 재도전");
        private final String label;
        Mode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    private final String id = UUID.randomUUID().toString();
    private final Mode mode;
    private final String category;
    private final Difficulty difficulty;
    private final List<QuestionBank.Entry> entries;
    private final BattleEngine battle;
    private final List<Person> earnedPersons = new ArrayList<>();
    private int questionIndex, selectedAnswer, correctCount, hints;
    private Phase phase = Phase.QUESTION;
    private String receipt = "";
    private boolean dialoguePlayed;
    public boolean dialoguePlayed() { return dialoguePlayed; }
    public void markDialoguePlayed() { dialoguePlayed = true; }
    private GameSession(Mode mode, String category, Difficulty difficulty, List<QuestionBank.Entry> entries, BattleEngine battle) {
        this.mode = mode; this.category = category; this.difficulty = difficulty; this.entries = List.copyOf(entries); this.battle = battle;
        if (entries.isEmpty()) throw new IllegalArgumentException("출제할 문제가 없습니다.");
    }
    public static GameSession practice(String category, Difficulty difficulty, List<QuestionBank.Entry> entries, boolean review) {
        return new GameSession(review ? Mode.REVIEW : Mode.PRACTICE, category, difficulty, entries, null);
    }
    public static GameSession battle(String category, Difficulty difficulty, List<QuestionBank.Entry> entries, BattleEngine battle) {
        return new GameSession(Mode.BATTLE, category, difficulty, entries, Objects.requireNonNull(battle));
    }
    public String id() { return id; }
    public Mode getMode() { return mode; }
    public String getCategory() { return category; }
    public Difficulty getDifficulty() { return difficulty; }
    public Phase getPhase() { return mode == Mode.BATTLE ? Phase.valueOf(battle.phase().name()) : phase; }
    public int getQuestionIndex() { return mode == Mode.BATTLE ? battle.turn() : questionIndex; }
    public int getQuestionCount() { return entries.size(); }
    public int getAnsweredCount() { return mode == Mode.BATTLE ? battle.turn() : questionIndex; }
    public int getCorrectCount() { return mode == Mode.BATTLE ? battle.correctCount() : correctCount; }
    public int getSelectedAnswer() { return mode == Mode.BATTLE ? battle.selected() + 1 : selectedAnswer; }
    public QuestionBank.Entry currentEntry() { return entries.get(Math.min(getQuestionIndex(), entries.size() - 1)); }
    public Person getCurrentPerson() { return currentEntry().person(); }
    public Quiz getCurrentQuiz() { return currentEntry().quiz(); }
    public BattleEngine getBattle() {
        if (battle == null) throw new IllegalStateException("배틀 모드가 아닙니다.");
        return battle;
    }
    public String receipt() { return receipt; }
    public void receipt(String receipt) { this.receipt = receipt; }
    public int hintsUsed() { return battle == null ? hints : battle.hintsUsed(); }
    public void recordHint() { require(Phase.QUESTION); hints++; }
    public void selectAnswer(int answer) {
        require(Phase.QUESTION);
        if (answer < 1 || answer > 4) throw new IllegalArgumentException("정답은 1~4번 중 선택하세요.");
        selectedAnswer = answer;
    }
    public boolean submitPractice() {
        require(Phase.QUESTION);
        if (selectedAnswer == 0) throw new IllegalStateException("정답을 선택해 주세요.");
        boolean correct = selectedAnswer == getCurrentQuiz().getAnswer();
        if (correct) { correctCount++; if (mode == Mode.PRACTICE) earnedPersons.add(getCurrentPerson()); }
        phase = Phase.FEEDBACK;
        return correct;
    }
    public void nextPractice() {
        require(Phase.FEEDBACK); questionIndex++; selectedAnswer = 0;
        phase = questionIndex == entries.size() ? Phase.FINISHED : Phase.QUESTION;
    }
    public List<Person> getRewards() { return mode == Mode.BATTLE ? battle.rewards() : List.copyOf(earnedPersons); }
    private void require(Phase expected) {
        if (mode == Mode.BATTLE || phase != expected) throw new IllegalStateException("연습 단계가 올바르지 않습니다.");
    }
}
