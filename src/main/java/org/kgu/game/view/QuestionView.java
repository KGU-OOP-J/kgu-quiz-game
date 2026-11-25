package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.event.QuestionGUIEvent;
import org.kgu.game.feature.GameSession;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

public final class QuestionView extends JPanel implements View {
    private final QuestionGUIEvent event;
    private final BattleGUIEvent battleEvent;
    private final CardArt art;
    private BattlePanel battlePanel;
    private CardView practiceCard;
    private Object practiceEntry;
    private boolean feedbackShown;
    private boolean hintVisible;
    private CardLayout portraitLayout;
    private JPanel portraitPages;
    private JButton hintButton;
    private final java.util.List<JButton> practiceChoices = new java.util.ArrayList<>();
    private final Timer practiceHit = new Timer(16, e -> animatePracticeHit());
    private void animatePracticeHit() {
        if (practiceCard == null) { practiceHit.stop(); return; }
        practiceCard.updateHit(System.nanoTime());
        if (!practiceCard.isHitAnimating()) practiceHit.stop();
    }
    public QuestionView(QuestionGUIEvent event, BattleGUIEvent battleEvent) { this.event = event; this.battleEvent = battleEvent; art = new CardArt(event::image); }
    public void display() {
        stopAnimations(); practiceEntry = null; practiceCard = null; battlePanel = null;
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
        GameSession s = event.getSession(); boolean asking = s.getPhase() == GameSession.Phase.QUESTION;
        removeAll(); setLayout(new BorderLayout(24, 20)); setBorder(BorderFactory.createEmptyBorder(32, 45, 32, 45));
        JPanel header = clear(new GridLayout(2, 1, 0, 8));
        JLabel title = label(s.getCategory() + " · " + s.currentEntry().difficulty() + " · " + s.getMode(), 26);
        JProgressBar bar = new JProgressBar(0, s.getQuestionCount()); bar.setValue(s.getQuestionIndex() + 1); bar.setStringPainted(true);
        bar.setString("문제 " + (s.getQuestionIndex() + 1) + "/" + s.getQuestionCount()); bar.setFont(UiStyle.FONT.deriveFont(18f));
        header.add(title); header.add(bar); add(header, BorderLayout.NORTH);
        JPanel main = clear(new BorderLayout(30, 0));
        if (practiceEntry != s.currentEntry() || practiceCard == null) {
            practiceHit.stop(); feedbackShown = false; practiceEntry = s.currentEntry();
            practiceCard = new CardView(s.getCurrentPerson(), s.currentEntry().difficulty().getStage(), art);
        }
        practiceCard.setName("practicePortrait");
        practiceCard.setPreferredSize(new Dimension(320, 460));
        portraitLayout = new CardLayout(); portraitPages = clear(portraitLayout);
        portraitPages.setPreferredSize(new Dimension(320, 460)); portraitPages.add(practiceCard, "portrait");
        JPanel hintContent = GameTheme.panel(new BorderLayout(0, 18));
        hintContent.setName("practiceHintPanel"); hintContent.setBorder(BorderFactory.createEmptyBorder(22, 20, 22, 20));
        hintContent.add(label("인물 힌트", 25), BorderLayout.NORTH);
        JTextArea facts = new JTextArea(String.join("\n\n", s.getCurrentPerson().getInformation()));
        facts.setEditable(false); facts.setLineWrap(true); facts.setWrapStyleWord(true); facts.setOpaque(false);
        facts.setFont(UiStyle.FONT.deriveFont(21f)); facts.setForeground(GameTheme.PAPER);
        hintContent.add(GameTheme.scroll(facts));
        JButton closeHint = button("카드로 돌아가기", this::hidePracticeHint); closeHint.setName("practiceHintClose");
        hintContent.add(closeHint, BorderLayout.SOUTH); portraitPages.add(hintContent, "hint");
        hintVisible = false; portraitLayout.show(portraitPages, "portrait"); main.add(portraitPages, BorderLayout.WEST);
        JPanel qa = clear(new BorderLayout(0, 16));
        JTextArea question = new JTextArea(s.getCurrentQuiz().getQuestion()); question.setEditable(false); question.setLineWrap(true); question.setWrapStyleWord(true);
        question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 32f)); question.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        question.setForeground(GameTheme.PAPER); question.setBackground(GameTheme.DARK); JScrollPane scroll = GameTheme.scroll(question); scroll.setPreferredSize(new Dimension(600, 155)); qa.add(scroll, BorderLayout.NORTH);
        JPanel options = clear(new GridLayout(4, 1, 0, 12));
        practiceChoices.clear();
        for (int i = 0; i < 4; i++) {
            int answer = i + 1; JButton b = new UiButton("<html>" + answer + ". " + escape(s.getCurrentQuiz().getChoices().get(i)) + "</html>", GameTheme.TEAL);
            b.setName("practiceChoice" + i); b.setFont(UiStyle.FONT.deriveFont(27f)); b.setEnabled(asking);
            boolean selected = answer == s.getSelectedAnswer();
            b.putClientProperty("selected", selected);
            b.putClientProperty("selectionTint", asking && selected ? new Color(47, 91, 157) : null);
            b.putClientProperty("correct", !asking && answer == s.getCurrentQuiz().getAnswer());
            b.setBackground(!asking && answer == s.getCurrentQuiz().getAnswer() ? new Color(185, 245, 205) : answer == s.getSelectedAnswer() ? new Color(177, 218, 255) : Color.WHITE);
            b.addActionListener(e -> {
                event.select(answer);
                for (int n = 0; n < practiceChoices.size(); n++) {
                    JButton choice = practiceChoices.get(n); boolean active = n + 1 == answer;
                    choice.putClientProperty("selected", active);
                    choice.putClientProperty("selectionTint", active ? new Color(47, 91, 157) : null); choice.repaint();
                }
            }); practiceChoices.add(b); options.add(b);
            bind("answer" + i, KeyStroke.getKeyStroke((char)('1' + i)), b::doClick);
        }
        qa.add(options, BorderLayout.CENTER); main.add(qa, BorderLayout.CENTER); add(main, BorderLayout.CENTER);
        JPanel bottom = clear(new GridLayout(2, 1, 0, 12));
        bottom.add(label(asking ? (s.getMode() == GameSession.Mode.PRACTICE ? "힌트는 자유롭게 볼 수 있어요. " + s.getQuestionCount() + "문제 모두 정답이면 " + org.kgu.game.feature.GameFeature.PRACTICE_PERFECT_COINS + "코인!" : "틀린 문제를 다시 풀며 확인해 보세요. 힌트도 볼 수 있어요.")
            : (s.getSelectedAnswer() == s.getCurrentQuiz().getAnswer() ? "정답! " : "오답 — ") + "정답: " + s.getCurrentQuiz().getChoices().get(s.getCurrentQuiz().getAnswer() - 1), 25));
        JPanel buttons = clear(new GridLayout(1, 3, 20, 0));
        JButton hint = button("힌트 보기", () -> event.showHint(this)); hint.setName("practiceHint"); hintButton = hint; hint.setEnabled(asking);
        JButton action = button(asking ? "정답 제출" : "다음 문제", () -> { if (asking) event.submit(this); else event.next(this); }); action.setName("practiceSubmit");
        buttons.add(hint); buttons.add(action); buttons.add(button("연습 그만", () -> event.giveUp(this))); bottom.add(buttons); add(bottom, BorderLayout.SOUTH);
        bind("submit", KeyStroke.getKeyStroke("ENTER"), action::doClick); revalidate(); repaint();
        if (s.getPhase() == GameSession.Phase.FEEDBACK && !feedbackShown) {
            feedbackShown = true;
            practiceCard.startPracticeFeedback(s.getSelectedAnswer() == s.getCurrentQuiz().getAnswer(), System.nanoTime());
            practiceHit.start();
        }
    }
    public boolean isPracticeHintVisible() { return hintVisible; }
    public void showPracticeHint() {
        hintVisible = true; portraitLayout.show(portraitPages, "hint"); hintButton.setText("힌트 닫기");
    }
    public void hidePracticeHint() {
        hintVisible = false; portraitLayout.show(portraitPages, "portrait"); hintButton.setText("힌트 보기");
    }
    private JButton button(String text, Runnable action) { JButton b = new UiButton(text, GameTheme.TEAL); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); b.addActionListener(e -> action.run()); return b; }
    private void bind(String name, KeyStroke key, Runnable action) { getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name); getActionMap().put(name, new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); } }); }
    private static String escape(String text) { return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
    private static JPanel clear(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    private static JLabel label(String text, int size) { JLabel l = new JLabel(text); l.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float)size)); l.setForeground(GameTheme.PAPER); return l; }
    public void stopAnimations() { practiceHit.stop(); if (battlePanel != null) battlePanel.stopAnimation(); }
    @Override public void removeNotify() { stopAnimations(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.QUESTION; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "study", 0.84f); g.dispose(); }
}
