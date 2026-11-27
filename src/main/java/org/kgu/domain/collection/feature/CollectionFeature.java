package org.kgu.domain.collection.feature;

import org.kgu.core.application.PlayerSession;
import org.kgu.core.factory.PersonFactory;
import org.kgu.core.manager.ImageManager;
import org.kgu.core.manager.PersonManager;
import org.kgu.core.model.Person;
import org.kgu.core.window.ViewId;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class CollectionFeature {
    public static final String ALL = "전체";

    private final PersonManager personManager;
    private final ImageManager imageManager;

    private final PlayerSession playerSession;
    private final Consumer<ViewId> navigate;

    public CollectionFeature(
        PersonManager personManager,
        ImageManager imageManager,
        PlayerSession playerSession,
        Consumer<ViewId> navigate
    ) {
        this.personManager = Objects.requireNonNull(personManager);
        this.imageManager = Objects.requireNonNull(imageManager);

        this.playerSession = Objects.requireNonNull(playerSession);
        this.navigate = Objects.requireNonNull(navigate);
    }

    /**
     * 잠긴 마을이 풀리는 모션을 이 사용자가 이미 봤는지. 세이브 파일 형식(카테고리 TAB 인물명 TAB 난이도)은 건드리지 않으려고
     * 옆에 따로 둔 {@code <아이디>.unlocked.txt}에 본 마을 이름을 한 줄씩 적는다. 읽지 못하면 안 본 것으로 본다.
     */
    public boolean isUnlockSeen(String village) {
        try {
            Path path = unlockPath();
            return Files.exists(path) && Files.readAllLines(path, StandardCharsets.UTF_8).contains(village);
        } catch (IOException e) {
            return false;
        }
    }

    /** 풀리는 모션을 봤다고 적는다. 못 적으면 다음에 모션이 한 번 더 나올 뿐이라 오류를 알리지 않는다. */
    public void markUnlockSeen(String village) {
        if (isUnlockSeen(village)) return;
        try {
            Path path = unlockPath();
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(path, village + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // 모션 기록은 게임 진행과 무관하다
        }
    }

    private Path unlockPath() {
        Path save = playerSession.saveFactory().getPath();
        return save.resolveSibling(save.getFileName().toString().replaceFirst("\\.txt$", "") + ".unlocked.txt");
    }

    public List<Person> getPersons(String category) {
        var persons = personManager.getAll().stream().map(PersonFactory::create).filter(person -> categoryAvailable(person.getCategory()));

        if (category.equals(ALL)) {
            return persons.sorted(Comparator.comparingInt(this::getHighestStage).reversed())
                .toList();
        }

        return persons.filter(person -> person.getCategory().equals(category)).toList();
    }

    public boolean categoryAvailable(String category) { return !category.equals("교수님") || playerSession.saveFactory().progress().snapshot().professorUnlocked(); }
    public int rescueProgress() { return playerSession.saveFactory().progress().snapshot().rescueProgress(); }

    public List<Integer> getStages(Person person) {
        return playerSession.saveFactory().create().getStages(person.getCategory(), person.getName());
    }
    public boolean isNew(Person person, int stage) { return playerSession.saveFactory().progress().snapshot().isNewCard(person.getCategory(), person.getName(), stage); }
    public int newCount(String category) { return playerSession.saveFactory().progress().snapshot().newCardCount(category.equals(ALL) ? "" : category); }
    public boolean equipped(Person person) { return playerSession.saveFactory().progress().snapshot().equipped().equals(org.kgu.core.progression.CardPower.key(person)); }
    public String powerDescription(Person person, int stage) {
        var profile = playerSession.saveFactory().progress().snapshot();
        return org.kgu.core.progression.CardPower.of(person, stage, profile.mastery(org.kgu.core.progression.CardPower.key(person))).description()
            + " · 숙련도 " + profile.mastery(org.kgu.core.progression.CardPower.key(person));
    }
    public void readCard(Person person, int stage) throws IOException { playerSession.saveFactory().progress().readCard(person.getCategory(), person.getName(), stage); }

    public int getHighestStage(Person person) {
        List<Integer> stages = getStages(person);
        return stages.isEmpty() ? 0 : stages.getLast();
    }

    public boolean isCollected(Person person) {
        return !getStages(person).isEmpty();
    }

    public List<Person> getCollectedPersons() {
        return getPersons(ALL).stream().filter(this::isCollected).toList();
    }

    public int getCollectedCount() {
        return getCollectedPersons().size();
    }

    public int getStageCount(String category) {
        return getPersons(category).stream().mapToInt(person -> getStages(person).size()).sum();
    }

    public Optional<BufferedImage> image(String key) {
        return imageManager.findImage(key);
    }

    public void returnToMenu() {
        navigate.accept(ViewId.MENU);
    }
}
