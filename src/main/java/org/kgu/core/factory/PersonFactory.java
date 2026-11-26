package org.kgu.core.factory;

import org.kgu.core.model.Person;
import java.util.Objects;

public final class PersonFactory extends Factory<Person> {
    private final Person person;
    private final QuizFactory quizFactory;

    public PersonFactory(String category, Person person, QuizFactory quizFactory) {
        Objects.requireNonNull(category);
        Objects.requireNonNull(person);

        this.person = person.getCategory().equals(category) ? person
                : new Person(category, person.getName(), person.getInformation(), person.hasDialogue(), person.getDialogue());
        this.quizFactory = Objects.requireNonNull(quizFactory);
    }

    public String getCategory() { return person.getCategory(); }
    public QuizFactory getQuizFactory() { return quizFactory; }

    @Override public Person create() {
        return person;
    }
}
