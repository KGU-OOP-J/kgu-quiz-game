package org.kgu.core.progression;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/** Versioned, user-scoped sidecar. Atomic replace; failed writes never publish a new profile. */
public final class ProgressStore {
    private final Path path;
    private PlayerProgress profile = starter();
    public ProgressStore(Path path) { this.path = Objects.requireNonNull(path); }
    private static PlayerProgress starter() {
        PlayerProgress p = new PlayerProgress();
        p.number("version", 1); p.number("coins", 120);
        return p;
    }
    public synchronized void open() throws IOException {
        if (!Files.exists(path)) return;
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(path)) { properties.load(reader); }
        catch (IllegalArgumentException e) { throw new IOException("진행도 파일이 손상되었습니다: " + path, e); }
        PlayerProgress loaded = new PlayerProgress(properties);
        try {
            if (loaded.number("version") != 1) throw new IllegalArgumentException("지원하지 않는 버전");
            for (String key : properties.stringPropertyNames()) {
                if (key.equals("coins") || key.equals("xp") || key.startsWith("item.") || key.startsWith("mastery.")
                    || key.startsWith("stage.") || Set.of("wins", "correct", "attempts", "bestCombo", "collected", "perfectWins").contains(key)) {
                    if (loaded.number(key) < 0) throw new IllegalArgumentException("음수 진행도");
                    if (key.startsWith("stage.") && loaded.number(key) > 3) throw new IllegalArgumentException("별 범위 초과");
                    if (key.startsWith("item.") && loaded.number(key) > 99) throw new IllegalArgumentException("아이템 보관 범위 초과");
                }
            }
            if (loaded.experience() > 1_000_000_000) throw new IllegalArgumentException("경험치 범위 초과");
        } catch (IllegalArgumentException e) { throw new IOException("진행도 파일 형식 오류: " + path, e); }
        profile = loaded;
    }
    public synchronized PlayerProgress snapshot() { return new PlayerProgress(profile.values); }
    public Path path() { return path; }
    public synchronized void update(Consumer<PlayerProgress> change) throws IOException {
        PlayerProgress next = snapshot(); change.accept(next);
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary)) { next.values.store(writer, "WHO'S NEXT profile v1"); }
            try { Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING); }
            profile = next;
        } finally { Files.deleteIfExists(temporary); }
    }
    public void syncCollected(int count) throws IOException {
        if (snapshot().number("collected") != count) update(p -> p.number("collected", count));
    }
    public void completeTutorial() throws IOException { update(p -> p.flag("tutorial", true)); }
    public void equip(String personKey) throws IOException { update(p -> p.text("equipped", personKey)); }
    public void buy(ShopItem item) throws IOException {
        Objects.requireNonNull(item);
        update(p -> {
            if (p.coins() < item.price()) throw new IllegalArgumentException("코인이 부족합니다.");
            if (p.inventory(item) >= 99) throw new IllegalArgumentException("아이템은 종류별 99개까지 보관할 수 있습니다.");
            p.add("coins", -item.price()); p.add("item." + item.name(), 1);
        });
    }
    public void consume(ShopItem item) throws IOException {
        update(p -> {
            if (p.inventory(item) < 1) throw new IllegalArgumentException("아이템을 먼저 상점에서 구매하세요.");
            p.add("item." + item.name(), -1);
        });
    }
    public void claim(Achievement achievement) throws IOException {
        update(p -> {
            if (!achievement.unlocked(p)) throw new IllegalArgumentException("아직 달성하지 않은 도전과제입니다.");
            if (achievement.claimed(p)) throw new IllegalArgumentException("이미 보상을 받았습니다.");
            p.flag("achievement." + achievement.name(), true); p.add("coins", achievement.reward());
        });
    }
    public void answer(String questionId, boolean correct) throws IOException { answer(questionId, "", correct); }
    public void answer(String questionId, String category, boolean correct) throws IOException {
        update(p -> {
            p.add("attempts", 1);
            if (correct) {
                p.add("correct", 1); p.values.remove("wrong." + questionId);
                if (Set.of("과학자", "한국사", "아이돌").contains(category)) p.flag("solved.external." + questionId, true);
            }
            else p.flag("wrong." + questionId, true);
            LinkedList<String> recent = new LinkedList<>(p.recentQuestions());
            recent.remove(questionId); recent.addFirst(questionId);
            while (recent.size() > 40) recent.removeLast();
            p.text("recent", String.join(",", recent));
        });
    }
    public record Reward(String sessionId, int xp, int coins, boolean battle, boolean win, String stageKey,
                         int stars, int bestCombo, boolean perfect, List<String> masteryKeys, int collected) {
        public Reward { masteryKeys = List.copyOf(masteryKeys); }
    }
    public void settle(Reward reward) throws IOException {
        if (reward.stars() < 0 || reward.stars() > 3 || reward.xp() < 0 || reward.coins() < 0) throw new IllegalArgumentException("잘못된 보상입니다.");
        if (snapshot().flag("settled." + reward.sessionId())) return;
        update(p -> {
            if (p.flag("settled." + reward.sessionId())) return;
            p.add("xp", Math.max(0, reward.xp())); p.add("coins", Math.max(0, reward.coins()));
            if (reward.battle() && reward.win()) p.add("wins", 1);
            if (reward.perfect()) p.add("perfectWins", 1);
            p.number("bestCombo", Math.max(p.number("bestCombo"), reward.bestCombo()));
            p.number("collected", reward.collected());
            if (reward.win() && !reward.stageKey().isBlank()) {
                p.number("stage." + reward.stageKey(), Math.max(p.number("stage." + reward.stageKey()), reward.stars()));
            }
            reward.masteryKeys().stream().distinct().forEach(k -> p.add("mastery." + k, 1));
            p.flag("settled." + reward.sessionId(), true);
        });
    }
}
