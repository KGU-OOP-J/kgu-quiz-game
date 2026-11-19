package org.kgu.collection.feature;

import org.kgu.application.PlayerSession;
import org.kgu.factory.PersonFactory;
import org.kgu.manager.ImageManager;
import org.kgu.manager.PersonManager;
import org.kgu.model.Person;
import org.kgu.window.ViewId;

import java.awt.image.BufferedImage;
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

    public List<Person> getPersons(String category) {
        var persons = personManager.getAll().stream().map(PersonFactory::create);

        if (category.equals(ALL)) {
            return persons.sorted(Comparator.comparingInt(this::getHighestStage).reversed())
                .toList();
        }

        return persons.filter(person -> person.getCategory().equals(category)).toList();
    }

    public List<Integer> getStages(Person person) {
        return playerSession.saveFactory().create().getStages(person.getCategory(), person.getName());
    }

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
