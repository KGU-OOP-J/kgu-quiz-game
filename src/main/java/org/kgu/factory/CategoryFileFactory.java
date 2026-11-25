package org.kgu.factory;

import org.kgu.application.Difficulty;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public final class CategoryFileFactory extends TextFileFactory<List<PersonFactory>> {
    private final String category;
    private List<PersonFactory> persons = List.of();

    public CategoryFileFactory(Path path, String category) {
        super(path);
        this.category = Objects.requireNonNull(category);
        if (category.isBlank()) throw new IllegalArgumentException("Category is required.");
    }

    public String getCategory() { return category; }

    @Override
    protected void read(Scanner in) throws IOException {
        List<PersonFactory> loaded = new ArrayList<>();
        try {
            while (in.hasNextLine() && !in.hasNext("@dialogue")) {
                Person person = Person.read(in, category);
                List<Quiz> quizzes = new ArrayList<>();
                for (Difficulty difficulty : Difficulty.values()) {
                    for (int i = 0; i < QuizFactory.PER_DIFFICULTY; i++) {
                        Quiz quiz = Quiz.read(in);
                        if (quiz.getDifficulty() != difficulty) throw new IllegalArgumentException("Expected difficulty: " + difficulty);
                        long named = quiz.getChoices().stream().filter(c -> c.contains(person.getName())).count();
                        if (named == 1 && quiz.getChoices().get(quiz.getAnswer() - 1).contains(person.getName()))
                            throw new IllegalArgumentException("Answer must not be the person: " + quiz.getQuestion());
                        quizzes.add(quiz);
                    }
                }
                if (loaded.stream().anyMatch(p -> p.create().getName().equals(person.getName())))
                    throw new IllegalArgumentException("Duplicate person: " + person.getName());
                loaded.add(new PersonFactory(category, person, new QuizFactory(quizzes)));
            }
            Set<String> scripted = new HashSet<>();
            while (in.hasNextLine()) {
                String header = in.nextLine();
                if (header.isBlank()) continue;
                String[] cells = header.split("\t", -1);
                if (cells.length != 3 || !cells[0].equals("@dialogue") || (!cells[2].equals("0") && !cells[2].equals("1")))
                    throw new IllegalArgumentException("대사 헤더 형식 오류: " + header);
                if (!scripted.add(cells[1])) throw new IllegalArgumentException("중복 대사: " + cells[1]);
                int index = -1;
                for (int i = 0; i < loaded.size(); i++) if (loaded.get(i).create().getName().equals(cells[1])) index = i;
                if (index < 0) throw new IllegalArgumentException("대사 인물이 없습니다: " + cells[1]);
                List<String> dialogue = new ArrayList<>();
                boolean enabled = cells[2].equals("1");
                if (enabled) {
                    boolean terminated = false;
                    while (in.hasNextLine()) {
                        String line = in.nextLine();
                        if (line.strip().equals("0")) { terminated = true; break; }
                        if (line.startsWith("@dialogue\t")) throw new IllegalArgumentException("대본 종료 0이 없습니다: " + cells[1]);
                        if (!line.isBlank()) dialogue.add(line);
                    }
                    if (!terminated || dialogue.isEmpty()) throw new IllegalArgumentException("비어 있거나 끝나지 않은 대본: " + cells[1]);
                }
                PersonFactory factory = loaded.get(index);
                Person person = factory.create();
                loaded.set(index, new PersonFactory(category,
                    new Person(category, person.getName(), person.getInformation(), enabled, dialogue), factory.getQuizFactory()));
            }
            if (!scripted.isEmpty() && scripted.size() != loaded.size()) throw new IllegalArgumentException("모든 인물의 대사 플래그가 필요합니다.");
            if (in.ioException() != null) throw in.ioException();
            persons = List.copyOf(loaded);
        } catch (IllegalArgumentException | NoSuchElementException e) {
            throw new IOException("Invalid category file: " + getPath() + ", person " + (loaded.size() + 1), e);
        }
    }

    @Override public List<PersonFactory> create() { return persons; }
    @Override public void clear() { persons = List.of(); }
}
