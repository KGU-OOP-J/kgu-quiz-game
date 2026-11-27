package org.kgu.domain.collection.event;

import org.kgu.domain.collection.view.component.*;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.core.model.Person;
import org.kgu.core.progression.*;
import javax.swing.*;
import java.util.List;

public final class CardRewardEvent implements CardAcquisitionPanel.Actions {
    private final GameFeature game;
    public CardRewardEvent(GameFeature game) { this.game = game; }
    public void present(JComponent owner) {
        if (game.getSession().getMode() != org.kgu.domain.game.feature.GameSession.Mode.BATTLE) return;
        if (!owner.isShowing()) return;
        var session = game.getSession();
        List<Person> cards = game.getSavedRewards().stream().filter(session::isNewCard).filter(session::markCardPresented).toList();
        CardAcquisitionPanel.show(owner, cards, session.getDifficulty().getStage(), new CardArt(game::image), this);
    }
    @Override public CardPower power(Person person) { return game.cardPower(person); }
    @Override public String equippedDescription() { return game.equippedPower().title() + " · " + game.equippedPower().description(); }
    @Override public int mastery(Person person) { return game.profile().mastery(CardPower.key(person)); }
    @Override public void equip(Person person) throws java.io.IOException { game.equip(person); }
    @Override public void openCollection() { game.openCollection(); }
}
