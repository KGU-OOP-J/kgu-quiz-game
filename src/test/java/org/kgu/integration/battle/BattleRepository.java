package org.kgu.integration.battle;

import org.kgu.battle.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.kgu.integration.collection.CardSave;

/** Adapter for the collection team's data/{field}.txt format. No generated questions. */
public final class BattleRepository {
    public record Entry(BattleCharacter character, List<BattleQuestion> questions) {
        @Override public String toString() { return character.name(); }
    }
    private final Path dataDir;
    public BattleRepository() { this(Path.of("data")); }
    public BattleRepository(Path dataDir) { this.dataDir = dataDir; }

    public List<Entry> load(String field, String difficulty) {
        if (!List.of("과학자", "교수님", "한국사", "아이돌").contains(field)
                || CardSave.starsOf(difficulty) == 0) {
            throw new IllegalArgumentException("분야 또는 난이도를 확인해 주세요.");
        }
        List<String> lines;
        try { lines = Files.readAllLines(dataDir.resolve(field + ".txt")); }
        catch (IOException ex) { throw new IllegalStateException("문제 파일을 읽을 수 없습니다: " + field, ex); }
        List<Entry> result = new ArrayList<>();
        BattleCharacter person = null;
        String info = "";
        List<BattleQuestion> questions = new ArrayList<>();
        int lineNo = 0;
        for (String line : lines) {
            lineNo++;
            if (line.isBlank()) continue;
            String[] cells = line.split("\t", -1);
            if (CardSave.starsOf(cells[0]) == 0) {
                addEntry(result, person, questions);
                person = null;
                questions = new ArrayList<>();
                // An invalid header must never attach subsequent questions to the previous person.
                if (cells.length != 5 || cells[0].isBlank()) continue;
                String id = field + "\t" + cells[0];
                person = new BattleCharacter(id, cells[0], field,
                        "images/cards/" + cells[0] + "_" + CardSave.starsOf(difficulty) + ".png");
                info = String.join(" · ", cells[1], cells[2], cells[3], cells[4]);
            } else if (person != null && cells[0].equals(difficulty) && cells.length == 7) {
                try {
                    int answer = Integer.parseInt(cells[6]) - 1; // Team data uses 1..4.
                    String[] options = {cells[2], cells[3], cells[4], cells[5]};
                    if (cells[1].isBlank() || java.util.Arrays.stream(options).anyMatch(String::isBlank)) continue;
                    if (answer < 0 || answer > 3) continue;
                    questions.add(new BattleQuestion(person.id() + ":" + lineNo, person.id(),
                            difficulty, cells[1], options, answer, "", ""));
                } catch (NumberFormatException ex) {
                    // A malformed question is skipped; only people with three valid questions can play.
                }
            }
        }
        addEntry(result, person, questions);
        return List.copyOf(result);
    }
    private static void addEntry(List<Entry> entries, BattleCharacter person, List<BattleQuestion> questions) {
        if (person != null && questions.size() >= 3
                && entries.stream().noneMatch(e -> e.character().id().equals(person.id()))) {
            entries.add(new Entry(person, List.copyOf(questions)));
        }
    }
}
