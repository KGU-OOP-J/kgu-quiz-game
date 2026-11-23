package org.kgu.menu.feature;

import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameFeature;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.ViewId;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class MenuFeature {
    private final GameFeature gameFeature;
    private final Consumer<ViewId> navigate;

    public MenuFeature(GameFeature gameFeature, Consumer<ViewId> navigate) {
        this.gameFeature = Objects.requireNonNull(gameFeature);
        this.navigate = Objects.requireNonNull(navigate);
    }

    public List<Person> getCandidates(String category) {
        return gameFeature.getCandidates(category);
    }

    public void start(String category, Difficulty difficulty, GameSession.Mode mode, Person person) {
        if (mode == GameSession.Mode.PRACTICE) {
            gameFeature.startPractice(category, difficulty);
        } else {
            gameFeature.startBattle(category, difficulty, person);
        }
    }

    public void openCollection() {
        navigate.accept(ViewId.COLLECTION);
    }

    public void openSetting() {
        navigate.accept(ViewId.SETTING);
    }

    public void returnToLogin() {
        navigate.accept(ViewId.LOGIN);
    }
}
