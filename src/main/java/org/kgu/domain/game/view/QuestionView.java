package org.kgu.domain.game.view;

import org.kgu.domain.collection.view.component.CardArt;
import org.kgu.domain.collection.view.component.CardView;
import org.kgu.domain.game.event.BattleGUIEvent;
import org.kgu.domain.game.event.QuestionGUIEvent;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.domain.game.view.layout.PracticeLayout;
import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;

public final class QuestionView extends JPanel implements View {
    private final QuestionGUIEvent event;
    private final BattleGUIEvent battleEvent;
    private final CardArt art;
    private BattlePanel battlePanel;
    private CardView practiceCard;
    private final Timer practiceHit = new Timer(16, e -> animatePracticeHit());
    private void animatePracticeHit() {
        if (practiceCard == null) { practiceHit.stop(); return; }
        practiceCard.updateHit(System.nanoTime());
        if (!practiceCard.isHitAnimating()) practiceHit.stop();
    }
    public QuestionView(QuestionGUIEvent event, BattleGUIEvent battleEvent) { setBackground(GameTheme.PAGE); this.event = event; this.battleEvent = battleEvent; art = new CardArt(event::image); }
    public void display() {
        stopAnimations(); practiceCard = null; battlePanel = null;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).clear(); getActionMap().clear();
        if (event.getSession().getMode() != GameSession.Mode.BATTLE) showPractice();
        else {
            removeAll(); setLayout(new BorderLayout()); setBorder(BorderFactory.createEmptyBorder());
            battlePanel = new BattlePanel(battleEvent.getBattle(), battleEvent, battleEvent::finish, battleEvent::returnToMenu);
            add(battlePanel); revalidate(); repaint();
        }
    }
    public void showPractice() {
        showPractice(false);
    }
    public void showPractice(boolean correctAnswer) {
        practiceHit.stop();
        GameSession session = event.getSession();
        removeAll();
        setLayout(new BorderLayout());
        setBorder(null);
        practiceCard = new CardView(session.getCurrentPerson(), session.currentEntry().difficulty().getStage(), art);
        practiceCard.reservePracticeEffectSpace();
        add(new PracticeLayout(session, practiceCard, answer -> {
            event.select(answer);
            showPractice();
        }, () -> event.showHint(this), () -> event.submit(this), () -> event.next(this),
            () -> event.giveUp(this)), BorderLayout.CENTER);
        revalidate();
        repaint();
        if (session.getPhase() == GameSession.Phase.FEEDBACK) {
            if (correctAnswer) practiceCard.startCorrectHit(System.nanoTime());
            else practiceCard.startHit(System.nanoTime());
            practiceHit.start();
        }
    }
    public void playShowcaseAnswers(boolean correct, boolean continuous) {
        if (battlePanel == null) throw new IllegalStateException("전투 화면이 열려 있어야 합니다.");
        battlePanel.playShowcaseAnswers(correct, continuous);
    }

    public void stopAnimations() { practiceHit.stop(); if (battlePanel != null) battlePanel.stopAnimation(); }
    @Override public ViewId getViewId() { return ViewId.QUESTION; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, "study", 0.6f);
    }
}
