package org.kgu.domain.adventure.feature;

import org.kgu.core.application.Difficulty;
import org.kgu.core.model.Person;
import org.kgu.core.progression.*;
import org.kgu.domain.game.feature.GameFeature;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public final class AdventureFeature {
    private final GameFeature game;

    public AdventureFeature(GameFeature game) {
        this.game = game;
    }

    public PlayerProgress profile() { return game.profile(); }
    public String playerId() { return game.playerId(); }
    public List<Person> getChallengeCandidates(String category) { return game.getChallengeCandidates(category); }
    public boolean categoryUnlocked(String category) { return game.categoryUnlocked(category); }
    public List<Person> ownedCards() { return game.ownedCards(); }
    public CardPower cardPower(Person person) { return game.cardPower(person); }
    public int cardStage(Person person) { return game.cardStage(person); }
    public Optional<BufferedImage> image(String key) { return game.image(key); }

    public void buy(ShopItem item) throws IOException { game.progress().buy(item); }
    public void claim(Achievement achievement) throws IOException { game.progress().claim(achievement); }
    public void equip(Person person) throws IOException { game.equip(person); }
    public void startChallenge(String category, Difficulty difficulty, Person target, boolean boss) {
        game.startChallenge(category, difficulty, target, boss);
    }
    public void startPractice(String category, Difficulty difficulty) { game.startPractice(category, difficulty); }
    public void startReview() { game.startReview(); }
    public void returnToMenu() { game.returnToMenu(); }
    public boolean takeChallengeMapRequest() { return game.takeChallengeMapRequest(); }

    public void completeTutorial() throws IOException {
        game.progress().completeTutorial();
        returnToMenu();
    }
}
