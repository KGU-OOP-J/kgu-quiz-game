package org.kgu.domain.collection.event;

import org.kgu.domain.collection.feature.CollectionFeature;
import org.kgu.domain.collection.view.CollectionView;
import org.kgu.core.model.Person;

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

    public static final String LOCKED_VILLAGE = "교수님";

    /** 도감과 실제 전투가 같은 진행도 해금 조건을 사용한다. */
    public boolean isVillageOpen(String category) {
        return categoryAvailable(category);
    }

    /**
     * 잠긴 마을이 열려 있고 이 사용자가 풀리는 모션을 아직 안 봤으면, 봤다고 기록하고 true를 준다(계정마다 한 번).
     */
    public boolean takeUnlockMotion(String village) {
        if (!isVillageOpen(village) || collectionFeature.isUnlockSeen(village)) return false;
        collectionFeature.markUnlockSeen(village);
        return true;
    }

    public Optional<BufferedImage> image(String key) {
        return collectionFeature.image(key);
    }

    public void returnToMenu() {
        collectionFeature.returnToMenu();
    }
}
