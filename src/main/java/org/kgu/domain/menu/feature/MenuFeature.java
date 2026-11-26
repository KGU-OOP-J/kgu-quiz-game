package org.kgu.domain.menu.feature;

import org.kgu.core.application.Difficulty;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.core.model.Person;
import org.kgu.core.window.ViewId;

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
        return gameFeature.getChallengeCandidates(category);
    }

    public void start(String category, Difficulty difficulty, GameSession.Mode mode, Person person) {
        if (mode == GameSession.Mode.PRACTICE) {
            gameFeature.startPractice(category, difficulty);
        } else if (mode == GameSession.Mode.REVIEW) {
            gameFeature.startReview();
        } else {
            gameFeature.startBattle(category, difficulty, person);
        }
    }

    public org.kgu.core.progression.PlayerProgress profile() { return gameFeature.profile(); }
    public String nextGoal() { return gameFeature.nextGoal(); }
    public boolean categoryUnlocked(String category) { return gameFeature.categoryUnlocked(category); }

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
