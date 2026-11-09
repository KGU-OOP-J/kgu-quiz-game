package org.kgu.model;

import java.util.List;

public record Person(
    String name,
    String description,
    String info1,
    String info2,
    String info3,
    List<Question> questions
) {
    public Person {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Person name must not be empty."
            );
        }

        questions = List.copyOf(questions);
    }
}
