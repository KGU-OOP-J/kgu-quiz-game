package org.kgu.progression;

import java.util.*;

/** A detached profile snapshot. Only ProgressStore publishes persisted mutations. */
public final class PlayerProgress {
    final Properties values;
    public PlayerProgress() { values = new Properties(); }
    PlayerProgress(Properties source) { values = new Properties(); values.putAll(source); }
    public int number(String key) { return Integer.parseInt(values.getProperty(key, "0")); }
    public boolean flag(String key) { return Boolean.parseBoolean(values.getProperty(key, "false")); }
    public String text(String key) { return values.getProperty(key, ""); }
    void number(String key, int value) { values.setProperty(key, Integer.toString(Math.max(0, value))); }
    void add(String key, int amount) { number(key, Math.addExact(number(key), amount)); }
    void flag(String key, boolean value) { values.setProperty(key, Boolean.toString(value)); }
    void text(String key, String value) { values.setProperty(key, value); }
    public Set<String> keys(String prefix) {
        Set<String> keys = new TreeSet<>();
        values.stringPropertyNames().stream().filter(k -> k.startsWith(prefix)).forEach(keys::add);
        return Collections.unmodifiableSet(keys);
    }
    public int coins() { return number("coins"); }
    public int experience() { return number("xp"); }
    public int level() {
        int level = 1, remaining = experience();
        while (level < 100 && remaining >= experienceForLevel(level)) {
            remaining -= experienceForLevel(level++);
        }
        return level;
    }
    public static int experienceForLevel(int level) { return 100 + (level - 1) * 50; }
    public int levelExperience() {
        int spent = 0;
        for (int i = 1; i < level(); i++) spent += experienceForLevel(i);
        return level() == 100 ? 0 : experience() - spent;
    }
    public int mastery(String personKey) { return number("mastery." + personKey); }
    public int masteryLevel(String personKey) { return Math.min(5, mastery(personKey) / 3); }
    public int cleared(String category) {
        return (int) keys("stage.normal." + category + "|").stream().filter(k -> number(k) > 0)
            .map(k -> k.substring(0, k.lastIndexOf('|'))).distinct().count();
    }
    public int totalStars() { return keys("stage.").stream().mapToInt(this::number).sum(); }
    public boolean bossUnlocked(String category) { return cleared(category) >= 3; }
    public int bossWins() { return (int) keys("stage.boss.").stream().filter(k -> number(k) > 0).count(); }
    public String equipped() { return text("equipped"); }
    public int inventory(ShopItem item) { return number("item." + item.name()); }
    public Set<String> wrongQuestions() {
        Set<String> result = new HashSet<>();
        keys("wrong.").forEach(k -> result.add(k.substring(6)));
        return Set.copyOf(result);
    }
    public List<String> recentQuestions() {
        String recent = text("recent");
        return recent.isBlank() ? List.of() : List.of(recent.split(","));
    }
}
