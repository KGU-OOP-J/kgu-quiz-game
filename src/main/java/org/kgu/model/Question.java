package org.kgu.model;

import java.util.List;

public record Question(
    Difficulty difficulty,
    String content,
    List<String> choices,
    int answerNumber
) {
    public Question {
        if (difficulty == null) {
            throw new IllegalArgumentException(
                "Question difficulty must not be null."
            );
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                "Question content must not be empty."
            );
        }

        if (choices == null || choices.size() != 4) {
            throw new IllegalArgumentException(
                "Question must have exactly 4 choices."
            );
        }

        if (answerNumber < 1 || answerNumber > 4) {
            throw new IllegalArgumentException(
                "Answer number must be between 1 and 4."
            );
        }

        for (String choice : choices) {
            if (choice == null || choice.isBlank()) {
                throw new IllegalArgumentException(
                    "Question choices must not be empty."
                );
            }
        }

        choices = List.copyOf(choices);
    }

    public boolean isCorrect(int selectedNumber) {
        return answerNumber == selectedNumber;
    }
}
