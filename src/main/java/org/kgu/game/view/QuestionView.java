package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.event.QuestionGUIEvent;
import org.kgu.game.feature.GameSession;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;

/** Responsive practice layout adapted from the QuestionScreen branch. */
public final class QuestionView extends JPanel implements View {
    private final QuestionGUIEvent event;
    private final BattleGUIEvent battleEvent;
    private final CardArt art;
    private BattlePanel battlePanel;
    private final Image background = new ImageIcon(QuestionView.class.getResource("/images/question-background.png")).getImage();
    public QuestionView(QuestionGUIEvent event, BattleGUIEvent battleEvent) { this.event = event; this.battleEvent = battleEvent; art = new CardArt(event::image); }
    public void display() {
        stopAnimations(); battlePanel = null;
        getInputMap(WHEN_IN_FOCUSED_WINDOW).clear(); getActionMap().clear();
        if (event.getSession().getMode() != GameSession.Mode.BATTLE) showPractice();
        else {
            removeAll(); setLayout(new BorderLayout()); setBorder(BorderFactory.createEmptyBorder());
            battlePanel = new BattlePanel(battleEvent.getBattle(), battleEvent, battleEvent::finish, battleEvent::returnToMenu);
            add(battlePanel); revalidate(); repaint();
        }
    }
    public void showPractice() {
        GameSession s = event.getSession(); boolean asking = s.getPhase() == GameSession.Phase.QUESTION;
        removeAll(); setLayout(new BorderLayout(24, 20)); setBorder(BorderFactory.createEmptyBorder(32, 45, 32, 45));
        JPanel header = clear(new GridLayout(2, 1, 0, 8));
        JLabel title = label(s.getCategory() + " · " + s.currentEntry().difficulty() + " · " + s.getMode(), 26);
        JProgressBar bar = new JProgressBar(0, s.getQuestionCount()); bar.setValue(s.getQuestionIndex() + 1); bar.setStringPainted(true);
        bar.setString("문제 " + (s.getQuestionIndex() + 1) + "/" + s.getQuestionCount()); bar.setFont(UiStyle.FONT.deriveFont(18f));
        header.add(title); header.add(bar); add(header, BorderLayout.NORTH);
        JPanel main = clear(new BorderLayout(30, 0));
        CardView card = new CardView(s.getCurrentPerson(), s.currentEntry().difficulty().getStage(), art);
        card.setPreferredSize(new Dimension(320, 460)); main.add(card, BorderLayout.WEST);
        JPanel qa = clear(new BorderLayout(0, 16));
        JTextArea question = new JTextArea(s.getCurrentQuiz().getQuestion()); question.setEditable(false); question.setLineWrap(true); question.setWrapStyleWord(true);
        question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 28f)); question.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        question.setForeground(UiStyle.NAVY); JScrollPane scroll = new JScrollPane(question); scroll.setPreferredSize(new Dimension(600, 155)); qa.add(scroll, BorderLayout.NORTH);
        JPanel options = clear(new GridLayout(4, 1, 0, 12));
        for (int i = 0; i < 4; i++) {
            int answer = i + 1; JButton b = new JButton("<html>" + answer + ". " + escape(s.getCurrentQuiz().getChoices().get(i)) + "</html>");
            b.setName("practiceChoice" + i); b.setFont(UiStyle.FONT.deriveFont(23f)); b.setEnabled(asking);
            b.setBackground(!asking && answer == s.getCurrentQuiz().getAnswer() ? new Color(185, 245, 205) : answer == s.getSelectedAnswer() ? new Color(177, 218, 255) : Color.WHITE);
            b.addActionListener(e -> { event.select(answer); showPractice(); }); options.add(b);
            bind("answer" + i, KeyStroke.getKeyStroke((char)('1' + i)), b::doClick);
        }
        qa.add(options, BorderLayout.CENTER); main.add(qa, BorderLayout.CENTER); add(main, BorderLayout.CENTER);
        JPanel bottom = clear(new GridLayout(2, 1, 0, 12));
        bottom.add(label(asking ? "연습에서는 힌트를 자유롭게 볼 수 있습니다. 정답을 맞히면 단계 카드를 얻습니다."
            : (s.getSelectedAnswer() == s.getCurrentQuiz().getAnswer() ? "정답! " : "오답 — ") + "정답: " + s.getCurrentQuiz().getChoices().get(s.getCurrentQuiz().getAnswer() - 1), 19));
        JPanel buttons = clear(new GridLayout(1, 3, 20, 0));
        JButton hint = button("힌트 보기", () -> event.showHint(this)); hint.setEnabled(asking);
        JButton action = button(asking ? "정답 제출" : "다음 문제", () -> { if (asking) event.submit(this); else event.next(this); }); action.setName("practiceSubmit");
        buttons.add(hint); buttons.add(action); buttons.add(button("연습 그만", () -> event.giveUp(this))); bottom.add(buttons); add(bottom, BorderLayout.SOUTH);
        bind("submit", KeyStroke.getKeyStroke("ENTER"), action::doClick); revalidate(); repaint();
    }
    private JButton button(String text, Runnable action) { JButton b = new JButton(text); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); b.addActionListener(e -> action.run()); return b; }
    private void bind(String name, KeyStroke key, Runnable action) { getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name); getActionMap().put(name, new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); } }); }
    private static String escape(String text) { return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
    private static JPanel clear(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    private static JLabel label(String text, int size) { JLabel l = new JLabel(text); l.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float)size)); l.setForeground(UiStyle.NAVY); return l; }
    public void stopAnimations() { if (battlePanel != null) battlePanel.stopAnimation(); }
    @Override public ViewId getViewId() { return ViewId.QUESTION; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics g) { super.paintComponent(g); g.drawImage(background, 0, 0, getWidth(), getHeight(), this); }
}
