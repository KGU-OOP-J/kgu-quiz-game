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
        if (!isVillageOpen(category)) return;
        this.category = category;
        refresh(view);
        view.showList();
    }

    public void openPerson(CollectionView view, Person person) {
        List<Person> owned = collectionFeature.getPersons(category).stream().filter(collectionFeature::isCollected)
            .toList();
        view.showPerson(person, owned);
    }

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

    /** 교수님(경기대) 마을을 여는 분야. 이 분야들에서 각각 인물 절반 이상을 모아야 한다. */
    public static final String LOCKED_VILLAGE = "교수님";
    public static final List<String> KEY_VILLAGES = List.of("과학자", "한국사", "아이돌");

    /**
     * 마을에 들어갈 수 있는지. 교수님 마을은 과학자·한국사·아이돌에서 각각 인물을 절반 이상(별 개수 상관없이 1장이라도 모은 인물) 모아야 열린다.
     */
    public boolean isVillageOpen(String category) {
        return !LOCKED_VILLAGE.equals(category)
            || KEY_VILLAGES.stream().allMatch(key -> getCollectedCount(key) * 2 >= getPersonCount(key));
    }

    public Optional<BufferedImage> image(String key) {
        return collectionFeature.image(key);
    }

    public void returnToMenu() {
        collectionFeature.returnToMenu();
    }
}
