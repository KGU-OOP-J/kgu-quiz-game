package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.factory.PersonFactory;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import org.kgu.progression.PlayerProgress;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

public final class QuestionBank {
    public record Entry(Person person, Difficulty difficulty, Quiz quiz, String id) { }
    private QuestionBank() { }
    public static Entry entry(Person person, Difficulty difficulty, Quiz quiz) {
        String content = person.getCategory() + "\n" + person.getName() + "\n" + difficulty.name()
            + "\n" + quiz.getQuestion() + "\n" + String.join("\n", quiz.getChoices());
        try {
            String id = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content.getBytes(StandardCharsets.UTF_8)));
            return new Entry(person, difficulty, quiz, id);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    public static List<Entry> all(List<PersonFactory> people) {
        List<Entry> entries = new ArrayList<>();
        for (PersonFactory person : people) for (Difficulty d : Difficulty.values())
            for (Quiz q : person.getQuizFactory().get(d)) entries.add(entry(person.create(), d, q));
        return List.copyOf(entries);
    }
    public static List<Entry> choose(List<Entry> pool, int count, PlayerProgress profile, Random random, boolean uniquePeople) {
        List<Entry> shuffled = new ArrayList<>(pool); Collections.shuffle(shuffled, random);
        Set<String> recent = new HashSet<>(profile.recentQuestions());
        shuffled.sort(Comparator.comparing(e -> recent.contains(e.id())));
        List<Entry> result = new ArrayList<>(); Set<String> people = new HashSet<>(), ids = new HashSet<>();
        for (Entry entry : shuffled) {
            String person = entry.person().getCategory() + "|" + entry.person().getName();
            if ((!uniquePeople || people.add(person)) && ids.add(entry.id())) result.add(entry);
            if (result.size() == count) break;
        }
        return result.stream().map(entry -> {
            List<Integer> order = new ArrayList<>(List.of(0, 1, 2, 3)); Collections.shuffle(order, random);
            Quiz original = entry.quiz();
            Quiz shuffledQuiz = new Quiz(entry.difficulty(), original.getQuestion(), order.stream().map(original.getChoices()::get).toList(),
                order.indexOf(original.getAnswer() - 1) + 1);
            return new Entry(entry.person(), entry.difficulty(), shuffledQuiz, entry.id());
        }).toList();
    }
}
