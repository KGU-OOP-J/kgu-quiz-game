package org.kgu.core.model;

import org.kgu.core.application.Difficulty;

import java.util.*;

/**
 * 기존 영속 계약: 카테고리 TAB 인물명 TAB 난이도. 런타임 Factory ID를 저장하지 않는다.
 */
public final class Save {
    private final Map<String, Map<String, EnumSet<Difficulty>>> collected = new LinkedHashMap<>();

    public static Save read(Scanner in) {
        Save save = new Save();
        int lineNumber = 0;
        while (in.hasNextLine()) {
            String line = in.nextLine();
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] fields = line.split("\t", -1);
            if (fields.length != 3 || !isCell(fields[0]) || !isCell(fields[1])) {
                throw new IllegalArgumentException("세이브 " + lineNumber + "번째 줄의 형식이 잘못되었습니다.");
            }
            save.record(fields[0], fields[1], Difficulty.fromLabel(fields[2]));
        }
        return save;
    }

    private static boolean isCell(String value) {
        return value != null && !value.isBlank() && !value.contains("\t")
            && !value.contains("\n") && !value.contains("\r");
    }

    public boolean record(String category, String name, Difficulty difficulty) {
        if (!isCell(category) || !isCell(name) || difficulty == null) {
            throw new IllegalArgumentException("저장할 카테고리·인물명·난이도를 확인해 주세요.");
        }
        return collected.computeIfAbsent(category, key -> new LinkedHashMap<>())
            .computeIfAbsent(name, key -> EnumSet.noneOf(Difficulty.class)).add(difficulty);
    }

    public boolean contains(String category, String name, Difficulty difficulty) {
        Map<String, EnumSet<Difficulty>> persons = collected.getOrDefault(category, Collections.emptyMap());
        return persons.getOrDefault(name, EnumSet.noneOf(Difficulty.class)).contains(difficulty);
    }

    public boolean isCollected(String category, String name) {
        return !getStages(category, name).isEmpty();
    }

    public List<Integer> getStages(String category, String name) {
        Map<String, EnumSet<Difficulty>> persons = collected.getOrDefault(category, Collections.emptyMap());
        List<Integer> stages = new ArrayList<>();
        for (Difficulty difficulty : persons.getOrDefault(name, EnumSet.noneOf(Difficulty.class))) {
            stages.add(difficulty.getStage());
        }
        return List.copyOf(stages);
    }

    public int getCollectedCount() {
        return collected.values().stream().mapToInt(Map::size).sum();
    }

    public List<String> getLines() {
        List<String> lines = new ArrayList<>();
        collected.forEach((category, persons) -> persons.forEach((name, levels) -> {
            for (Difficulty difficulty : levels) {
                lines.add(category + "\t" + name + "\t" + difficulty.getLabel());
            }
        }));
        return List.copyOf(lines);
    }
}
