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
    private PracticeLayout practiceLayout;
    private boolean feedbackShown;
    private String hintText = "";
    private int displayedQuestion = -1;
    private String displayedSession = "";
    public boolean isPracticeHintVisible() { return !hintText.isBlank(); }
    public void hidePracticeHint() { showHint(""); }
    public void showHint(String text) { hintText = text; if (practiceLayout != null) practiceLayout.showHint(text); }
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
        if (practiceCard == null || displayedQuestion != session.getQuestionIndex() || !displayedSession.equals(session.id())) {
            practiceCard = new CardView(session.getCurrentPerson(), session.currentEntry().difficulty().getStage(), art);
            practiceCard.reservePracticeEffectSpace(); practiceCard.setName("practicePortrait"); hintText = ""; feedbackShown = false;
            displayedQuestion = session.getQuestionIndex(); displayedSession = session.id();
        }
        practiceLayout = new PracticeLayout(session, practiceCard, answer -> {
            event.select(answer);
            showPractice();
        }, () -> event.showHint(this), () -> event.submit(this), () -> event.next(this),
            () -> event.giveUp(this));
        practiceLayout.showHint(hintText);
        add(practiceLayout, BorderLayout.CENTER);
        revalidate();
        repaint();
        if (session.getPhase() == GameSession.Phase.FEEDBACK) {
            if (!feedbackShown) {
                practiceCard.startPracticeFeedback(session.getSelectedAnswer() == session.getCurrentQuiz().getAnswer(), System.nanoTime());
                feedbackShown = true;
            }
            if (practiceCard.isHitAnimating()) practiceHit.start();
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
        GameTheme.paintBackdrop(source, this, event.getSession().getMode() == GameSession.Mode.REVIEW ? "review" : "study", 0f);
    }
}
