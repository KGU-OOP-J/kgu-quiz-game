package org.kgu.factory;

import org.kgu.application.Difficulty;
import org.kgu.model.Quiz;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class QuizFactory extends Factory<List<Quiz>> {
    private final List<Quiz> quizzes;

    public QuizFactory(List<Quiz> quizzes) {
        this.quizzes = List.copyOf(quizzes);
        if (quizzes.size() != 9) {
            throw new IllegalArgumentException("Nine quizzes required.");
        }
        for (Difficulty difficulty : Difficulty.values()) {
            if (get(difficulty).size() != 3) {
                throw new IllegalArgumentException("Three quizzes per difficulty required.");
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
