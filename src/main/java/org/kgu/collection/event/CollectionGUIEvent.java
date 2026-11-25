package org.kgu.collection.event;

import org.kgu.collection.feature.CollectionFeature;
import org.kgu.collection.view.CollectionView;
import org.kgu.model.Person;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CollectionGUIEvent {
    private final CollectionFeature collectionFeature;
    private String category = CollectionFeature.ALL;

    public CollectionGUIEvent(CollectionFeature collectionFeature) {
        this.collectionFeature = Objects.requireNonNull(collectionFeature);
    }

    public void refresh(CollectionView view) {
        List<Person> persons = collectionFeature.getPersons(category);
        view.display(category, persons, (int) getCollectedCount(category), persons.size(), collectionFeature.getStageCount(category));
    }

    public void selectCategory(CollectionView view, String category) {
        if (!categoryAvailable(category)) { view.showGateNotice(rescueProgress()); return; }
        this.category = category;
        refresh(view);
        view.showList();
    }

    public void openPerson(CollectionView view, Person person) {
        if (!categoryAvailable(person.getCategory())) { view.showGateNotice(rescueProgress()); return; }
        List<Person> owned = collectionFeature.getPersons(category).stream().filter(collectionFeature::isCollected)
            .toList();
        view.showPerson(person, owned);
    }

    public boolean categoryAvailable(String category) { return collectionFeature.categoryAvailable(category); }
    public int rescueProgress() { return collectionFeature.rescueProgress(); }

    public List<Integer> getStages(Person person) {
        return collectionFeature.getStages(person);
    }

    public int getHighestStage(Person person) {
        return collectionFeature.getHighestStage(person);
    }

    public int getPersonCount(String category) {
        return collectionFeature.getPersons(category).size();
    }

    public long getCollectedCount(String category) {
        return collectionFeature.getPersons(category).stream().filter(collectionFeature::isCollected).count();
    }

    public Optional<BufferedImage> image(String key) {
        return collectionFeature.image(key);
    }

    public void returnToMenu() {
        collectionFeature.returnToMenu();
    }
}
