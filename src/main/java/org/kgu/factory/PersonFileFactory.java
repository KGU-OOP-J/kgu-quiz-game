
package org.kgu.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.kgu.model.Difficulty;
import org.kgu.model.Person;
import org.kgu.model.Question;

public final class PersonFileFactory extends FileFactory<List<Person>> {

    public PersonFileFactory(String id, String path) {
        super(id, path);
    }

    @Override
    protected List<Person> read(Scanner scan) {
        List<Person> people = new ArrayList<>();

        while (scan.hasNextLine()) {
            String line = scan.nextLine();

            if (line.isBlank()) {
                continue;
            }

            people.add(readPerson(scan, line));
        }

        return people;
    }

    private Person readPerson(Scanner scan, String line) {
        String[] info = split(line, 5);
        List<Question> questions = new ArrayList<>();

        readQuestions(scan, questions, Difficulty.EASY);
        readQuestions(scan, questions, Difficulty.NORMAL);
        readQuestions(scan, questions, Difficulty.HARD);

        return new Person(
            info[0], info[1], info[2],
            info[3], info[4], questions
        );
    }

    private void readQuestions(
        Scanner scan,
        List<Question> questions,
        Difficulty difficulty
    ) {
        for (int i = 0; i < 3; i++) {
            questions.add(readQuestion(scan, difficulty));
        }
    }

    private Question readQuestion(Scanner scan, Difficulty difficulty) {
        if (!scan.hasNextLine()) {
            throw new IllegalArgumentException(
                "Incomplete question data."
            );
        }

        String[] data = split(scan.nextLine(), 7);

        if (!data[0].equals(difficulty.getLabel())) {
            throw new IllegalArgumentException(
                "Unexpected question difficulty: " + data[0]
            );
        }

        return new Question(
            difficulty,
            data[1],
            List.of(data[2], data[3], data[4], data[5]),
            Integer.parseInt(data[6])
        );
    }

    private String[] split(String line, int expected) {
        String[] data = line.split("\t", -1);

        if (data.length != expected) {
            throw new IllegalArgumentException(
                "Expected " + expected + " fields, got " + data.length + "."
            );
        }

        return data;
    }
}
