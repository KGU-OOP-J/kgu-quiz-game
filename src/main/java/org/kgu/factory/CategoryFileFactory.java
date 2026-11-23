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
                    for (int i = 0; i < QuizFactory.PER_DIFFICULTY; i++) {
                        Quiz quiz = Quiz.read(in);
                        if (quiz.getDifficulty() != difficulty) {
                            throw new IllegalArgumentException("Expected difficulty: " + difficulty);
                        }
                        // 문제 화면에 인물 카드(이름)가 보이므로, 이름이 정답 보기에만 있으면 바로 맞힐 수 있다.
                        // 순서 맞히기처럼 모든 보기에 이름이 들어간 문제는 괜찮다.
                        long named = quiz.getChoices().stream().filter(c -> c.contains(person.getName())).count();
                        if (named == 1 && quiz.getChoices().get(quiz.getAnswer() - 1).contains(person.getName())) {
                            throw new IllegalArgumentException("Answer must not be the person: " + quiz.getQuestion());
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
