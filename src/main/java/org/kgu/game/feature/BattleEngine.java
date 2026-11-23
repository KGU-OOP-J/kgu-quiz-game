package org.kgu.game.feature;

import org.kgu.model.Person;
import org.kgu.model.Quiz;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** 프로토타입과 같은 6턴 규칙. Swing과 파일 입출력에 의존하지 않는다. */
public final class BattleEngine {
    public enum Phase { QUESTION, FEEDBACK, FINISHED }
    public enum Result { WIN, LOSE, DRAW }

    public record TurnFeedback(
        boolean correct,
        boolean attackTurn,
        int damage,
        String explanation
    ) { }

    private final Person player;
    private final Person computer;

    private final List<Quiz> questions;

    private int turn;
    private int playerHp = 60;
    private int computerHp = 60;
    private int selected = -1;
    private int correctCount;
    private Phase phase = Phase.QUESTION;
    private Optional<TurnFeedback> feedback = Optional.empty();

    public BattleEngine(Person player, Person computer, List<Quiz> playerQuestions, List<Quiz> computerQuestions) {
        if (player == computer || (player.getCategory().equals(computer.getCategory())
                && player.getName().equals(computer.getName()))) {
            throw new IllegalArgumentException("상대는 다른 인물이어야 합니다.");
        }

        if (playerQuestions.size() != 3 || computerQuestions.size() != 3) {
            throw new IllegalArgumentException("인물별 3문제가 필요합니다.");
        }

        this.player = player;
        this.computer = computer;

        List<Quiz> sequence = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            sequence.add(playerQuestions.get(i));
            sequence.add(computerQuestions.get(i));
        }

        questions = List.copyOf(sequence);
    }

    public Person player() { return player; }
    public Person computer() { return computer; }
    public Phase phase() { return phase; }
    public int turn() { return turn; }
    public int playerHp() { return playerHp; }
    public int computerHp() { return computerHp; }
    public int selected() { return selected; }
    public int correctCount() { return correctCount; }
    public boolean attackTurn() { return turn % 2 == 0; }
    public Quiz current() { return questions.get(Math.min(turn, 5)); }
    public Person currentPerson() { return Math.min(turn, 5) % 2 == 0 ? player : computer; }
    public String hint() { return String.join(" / ", currentPerson().getInformation()); }
    public String explanation() {
        return "정답: " + current().getChoices().get(current().getAnswer() - 1);
    }
    public TurnFeedback feedback() {
        require(Phase.FEEDBACK);
        return feedback.orElseThrow(() -> new IllegalStateException("답안을 먼저 제출하세요."));
    }

    public void select(int choice) {
        require(Phase.QUESTION);
        if (choice < 0 || choice >= 4) { throw new IllegalArgumentException("보기는 1~4번입니다."); }
        selected = choice;
    }

    public TurnFeedback submit() {
        require(Phase.QUESTION);

        if (selected < 0) {
            throw new IllegalStateException("정답을 선택해 주세요.");
        }

        boolean correct = selected + 1 == current().getAnswer();
        if (correct) correctCount++;

        int damage = 0;
        if (attackTurn() && correct) {
            computerHp = Math.max(0, computerHp - 20);
            damage = 20;
        }
        if (!attackTurn() && !correct) {
            playerHp = Math.max(0, playerHp - 20);
            damage = 20;
        }

        TurnFeedback result = new TurnFeedback(correct, attackTurn(), damage, explanation());
        feedback = Optional.of(result);
        phase = Phase.FEEDBACK;

        return result;
    }

    public void advance() {
        require(Phase.FEEDBACK);

        turn++;
        selected = -1;
        phase = turn == 6 ? Phase.FINISHED : Phase.QUESTION;
    }

    public Result result() {
        require(Phase.FINISHED);

        return playerHp > computerHp
            ? Result.WIN
            : playerHp < computerHp
            ? Result.LOSE
            : Result.DRAW;
    }

    public List<Person> rewards() {
        return result() == Result.WIN ? List.of(player, computer) : List.of();
    }

    private void require(Phase expected) {
        if (phase != expected) { throw new IllegalStateException("배틀 단계가 올바르지 않습니다: " + phase); }
    }
}
