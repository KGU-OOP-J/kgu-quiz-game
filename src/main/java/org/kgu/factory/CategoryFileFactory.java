package org.kgu.factory;

import org.kgu.application.Difficulty;
import org.kgu.model.Person;
import org.kgu.model.Quiz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class CategoryFileFactory extends TextFileFactory<List<PersonFactory>> {
    private final String category;
    private List<PersonFactory> persons = List.of();

    public CategoryFileFactory(Path path, String category) {
        super(path);
        this.category = Objects.requireNonNull(category);
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
    }

    public String getCategory() {
        return category;
    }

    @Override
    protected void read(Scanner in) throws IOException {
        List<PersonFactory> loaded = new ArrayList<>();
        try {
            while (in.hasNextLine()) {
                Person person = Person.read(in, category);
                List<Quiz> quizzes = new ArrayList<>();
                for (Difficulty difficulty : Difficulty.values()) {
                    for (int i = 0; i < 3; i++) {
                        Quiz quiz = Quiz.read(in);
                        if (quiz.getDifficulty() != difficulty) {
                            throw new IllegalArgumentException("Expected difficulty: " + difficulty);
                        }
                        quizzes.add(quiz);
                    }
                }
                if (loaded.stream().anyMatch(p -> p.create().getName().equals(person.getName()))) {
                    throw new IllegalArgumentException("Duplicate person: " + person.getName());
                }
                loaded.add(new PersonFactory(category, person, new QuizFactory(quizzes)));
            }
            if (in.ioException() != null) {
                throw in.ioException();
            }
            persons = List.copyOf(loaded);
        } catch (IllegalArgumentException | java.util.NoSuchElementException e) {
            throw new IOException("Invalid category file: " + getPath() + ", person " + (loaded.size() + 1), e);
        }
    }

    @Override
    public List<PersonFactory> create() {
        return persons;
    }

    @Override
    public void clear() {
        persons = List.of();
    }
}
