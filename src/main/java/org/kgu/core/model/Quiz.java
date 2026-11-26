package org.kgu.core.model;

import org.kgu.core.application.Difficulty;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class Quiz {
    private final Difficulty difficulty;
    private final String question;
    private final List<String> choices;
    private final int answer;

    public Quiz(Difficulty difficulty, String question, List<String> choices, int answer) {
        this.difficulty = Objects.requireNonNull(difficulty);
        this.question = Objects.requireNonNull(question);
        this.choices = List.copyOf(choices);
        if (question.isBlank() || choices.size() != 4 || answer < 1 || answer > 4) {
            throw new IllegalArgumentException("Quiz requires four choices and a 1-based answer.");
        }
        this.answer = answer;
    }

    /**
     * 한 문제(한 줄)만 읽는다. Scanner 소유권은 호출자에게 있다.
     */
    public static Quiz read(Scanner in) {
        String[] fields = in.nextLine().split("\t", -1);
        if (fields.length != 7) {
            throw new IllegalArgumentException("Quiz line must have seven tab-separated fields.");
        }
        return new Quiz(Difficulty.fromLabel(fields[0]), fields[1],
            List.of(fields[2], fields[3], fields[4], fields[5]), Integer.parseInt(fields[6]));
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public int getAnswer() {
        return answer;
    }
}
