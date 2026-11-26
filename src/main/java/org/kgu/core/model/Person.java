package org.kgu.core.model;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public final class Person {
    private final String category;
    private final String name;
    private final List<String> information;
    private final boolean hasDialogue;
    private final List<String> dialogue;

    public Person(String name, List<String> information) {
        this("", name, information);
    }

    public Person(String category, String name, List<String> information) {
        this(category, name, information, false, List.of());
    }

    public Person(String category, String name, List<String> information, boolean hasDialogue, List<String> dialogue) {
        this.hasDialogue = hasDialogue;
        this.dialogue = List.copyOf(dialogue);
        if (hasDialogue == dialogue.isEmpty()) throw new IllegalArgumentException("대사 플래그와 대본이 일치하지 않습니다.");
        this.category = Objects.requireNonNull(category);
        this.name = Objects.requireNonNull(name);
        this.information = List.copyOf(information);
        if (name.isBlank() || information.size() != 4) {
            throw new IllegalArgumentException("인물 이름과 정보 4개가 필요합니다.");
        }
    }

    /**
     * 인물 한 줄만 소비하며 Scanner를 닫지 않는다.
     */
    public static Person read(Scanner in) {
        return read(in, "");
    }

    public static Person read(Scanner in, String category) {
        String[] fields = in.nextLine().split("\t", -1);
        if (fields.length != 5) {
            throw new IllegalArgumentException("인물 정보는 탭으로 구분된 5개 항목이어야 합니다.");
        }
        return new Person(category, fields[0], List.of(fields[1], fields[2], fields[3], fields[4]));
    }

    public boolean hasDialogue() { return hasDialogue; }
    public List<String> getDialogue() { return dialogue; }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getIntroduction() {
        return information.getFirst();
    }

    public List<String> getAchievements() {
        return information.subList(1, 4);
    }

    public List<String> getInformation() {
        return information;
    }

    @Override
    public String toString() {
        return name;
    }
}
