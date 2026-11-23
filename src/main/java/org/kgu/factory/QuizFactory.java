package org.kgu.factory;

import org.kgu.application.Difficulty;
import org.kgu.model.Quiz;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class QuizFactory extends Factory<List<Quiz>> {
    /** 인물 한 명이 난이도마다 가진 문제 수. data/*.txt도 이 수에 맞춰 쓴다. */
    public static final int PER_DIFFICULTY = 5;

    private final List<Quiz> quizzes;

    public QuizFactory(List<Quiz> quizzes) {
        this.quizzes = List.copyOf(quizzes);
        if (quizzes.size() != PER_DIFFICULTY * Difficulty.values().length) {
            throw new IllegalArgumentException(PER_DIFFICULTY * Difficulty.values().length + " quizzes required.");
        }
        for (Difficulty difficulty : Difficulty.values()) {
            if (get(difficulty).size() != PER_DIFFICULTY) {
                throw new IllegalArgumentException(PER_DIFFICULTY + " quizzes per difficulty required.");
            }
        }
    }

    public List<Quiz> get(Difficulty difficulty) {
        Objects.requireNonNull(difficulty);

        return quizzes.stream()
            .filter(q -> q.getDifficulty() == difficulty)
            .toList();
    }

    @Override
    public List<Quiz> create() {
        return quizzes;
    }
}
