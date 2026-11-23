package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.factory.PersonFactory;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** 한 판의 선택값과 진행 상태. 로그인 사용자 정보와 분리하고 GameFeature가 보유한다. */
public final class GameSession {
    public enum Mode {
        PRACTICE("연습"), BATTLE("대진");
        private final String label;
        Mode(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    private final Mode mode;
    private final String category;
    private final Difficulty difficulty;
    private final List<PersonFactory> practicePersons;
    private final Optional<BattleEngine> battle;
    private final List<Person> earnedPersons = new ArrayList<>();
    private int questionIndex;
    private int selectedAnswer;
    private int correctCount;
    private Phase phase = Phase.QUESTION;

    private GameSession(Mode mode, String category, Difficulty difficulty,
                        List<PersonFactory> persons, Optional<BattleEngine> battle) {
        this.mode = Objects.requireNonNull(mode);
        this.category = Objects.requireNonNull(category);
        this.difficulty = Objects.requireNonNull(difficulty);
        this.practicePersons = List.copyOf(persons);
        this.battle = battle;
    }

    public static GameSession practice(String category, Difficulty difficulty, List<PersonFactory> persons) {
        if (persons.size() < 10) { throw new IllegalArgumentException("연습 모드에는 인물 10명 이상이 필요합니다."); }
        return new GameSession(Mode.PRACTICE, category, difficulty, persons.subList(0, 10), Optional.empty());
    }

    public static GameSession battle(String category, Difficulty difficulty, BattleEngine battle) {
        return new GameSession(Mode.BATTLE, category, difficulty, List.of(), Optional.of(battle));
    }

    public Mode getMode() { return mode; }
    public String getCategory() { return category; }
    public Difficulty getDifficulty() { return difficulty; }
    public Phase getPhase() { return mode == Mode.BATTLE ? Phase.valueOf(getBattle().phase().name()) : phase; }
    public int getQuestionIndex() { return mode == Mode.BATTLE ? getBattle().turn() : questionIndex; }
    public int getQuestionCount() { return mode == Mode.BATTLE ? 6 : practicePersons.size(); }
    public int getCorrectCount() { return mode == Mode.BATTLE ? getBattle().correctCount() : correctCount; }
    public int getSelectedAnswer() { return mode == Mode.BATTLE ? getBattle().selected() + 1 : selectedAnswer; }
    public Person getCurrentPerson() { return mode == Mode.BATTLE ? getBattle().currentPerson() : currentFactory().create(); }
    public Quiz getCurrentQuiz() { return mode == Mode.BATTLE ? getBattle().current() : currentFactory().getQuizFactory().get(difficulty).get(0); }
    public BattleEngine getBattle() {
        return battle.orElseThrow(() -> new IllegalStateException("대진 모드가 아닙니다."));
    }

    public void selectAnswer(int answer) {
        require(Phase.QUESTION);
        if (answer < 1 || answer > 4) { throw new IllegalArgumentException("정답은 1~4번 중 선택하세요."); }
        selectedAnswer = answer;
    }

    public boolean submitPractice() {
        require(Phase.QUESTION);
        if (selectedAnswer == 0) { throw new IllegalStateException("정답을 선택해 주세요."); }
        boolean correct = selectedAnswer == getCurrentQuiz().getAnswer();
        if (correct) {
            correctCount++;
            earnedPersons.add(getCurrentPerson());
        }
        phase = Phase.FEEDBACK;
        return correct;
    }

    public void nextPractice() {
        require(Phase.FEEDBACK);
        questionIndex++;
        selectedAnswer = 0;
        phase = questionIndex == practicePersons.size() ? Phase.FINISHED : Phase.QUESTION;
    }

    public List<Person> getRewards() {
        return mode == Mode.PRACTICE ? List.copyOf(earnedPersons) : getBattle().rewards();
    }

    private PersonFactory currentFactory() {
        if (mode != Mode.PRACTICE || phase == Phase.FINISHED) {
            throw new IllegalStateException("현재 연습 문제가 없습니다.");
        }
        return practicePersons.get(questionIndex);
    }
    private void require(Phase expected) {
        if (mode != Mode.PRACTICE || phase != expected) {
            throw new IllegalStateException("연습 단계가 올바르지 않습니다.");
        }
    }
}
