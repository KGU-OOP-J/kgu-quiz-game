package org.kgu.game.view;

import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.feature.BattleEngine;
import org.kgu.progression.ShopItem;
import org.kgu.ui.battle.ImpactCard;
import org.kgu.window.UiStyle;
import org.kgu.window.UiButton;
import org.kgu.window.GameTheme;
import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;

public final class BattlePanel extends JPanel {
    private final BattleEngine engine;
    private final BattleGUIEvent event;
    private final Runnable onFinish;
    private final JLabel progress = label("", 23), intent = label("", 20), resources = label("", 18), feedback = label("", 18);
    private final JTextArea question = new JTextArea();
    private final JButton[] choices = new JButton[4];
    private final EnumMap<BattleEngine.Action, JButton> actions = new EnumMap<>(BattleEngine.Action.class);
    private final EnumMap<BattleEngine.Skill, JButton> skills = new EnumMap<>(BattleEngine.Skill.class);
    private final EnumMap<ShopItem, JButton> items = new EnumMap<>(ShopItem.class);
    private final JButton submit = button("정답으로 행동", "submit"), next = button("다음 턴", "next");
    private final JProgressBar playerHp, targetHp;
    private final ImpactCard portrait;
    private final Timer animation = new Timer(16, e -> animate());

    public BattlePanel(BattleEngine engine, BattleGUIEvent event, Runnable onFinish, Runnable onMenu) {
        this.engine = engine; this.event = event; this.onFinish = onFinish;
        setLayout(new BorderLayout(15, 12)); setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));
        JPanel header = GameTheme.panel(new GridLayout(0, 1, 4, 6)); header.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        header.add(progress); header.add(intent); header.add(resources);
        JPanel bars = panel(new GridLayout(1, 2, 20, 0));
        playerHp = hp(engine.maxPlayerHp()); targetHp = hp(engine.maxComputerHp()); bars.add(playerHp); bars.add(targetHp); header.add(bars);
        add(header, BorderLayout.NORTH);
        JPanel arena = panel(new BorderLayout(18, 0));
        portrait = new ImpactCard(engine.computer().getName(), event.portrait(engine.computer()), new Color(242, 164, 55), "격파 성공!");
        portrait.setPreferredSize(new Dimension(300, 400)); arena.add(portrait, BorderLayout.WEST);
        JPanel qa = panel(new BorderLayout(0, 12));
        question.setEditable(false); question.setLineWrap(true); question.setWrapStyleWord(true);
        question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 25f)); question.setForeground(GameTheme.PAPER);
        question.setBackground(GameTheme.DARK); question.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JScrollPane questionScroll = GameTheme.scroll(question); questionScroll.setPreferredSize(new Dimension(600, 135)); qa.add(questionScroll, BorderLayout.NORTH);
        JPanel optionPanel = panel(new GridLayout(4, 1, 0, 9));
        for (int i = 0; i < 4; i++) {
            final int index = i; choices[i] = button("", "choice" + i);
            choices[i].addActionListener(e -> run(() -> event.select(index))); optionPanel.add(choices[i]);
            bind("choice" + i, KeyStroke.getKeyStroke((char) ('1' + i)), () -> choices[index].doClick());
        }
        qa.add(optionPanel, BorderLayout.CENTER); arena.add(qa, BorderLayout.CENTER); add(arena, BorderLayout.CENTER);
        JPanel bottom = GameTheme.panel(new GridLayout(0, 1, 0, 7)); bottom.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14)); bottom.add(feedback);
        JPanel actionRow = panel(new GridLayout(1, 3, 10, 0));
        for (BattleEngine.Action action : BattleEngine.Action.values()) {
            JButton b = button(action.label(), "action" + action.name()); b.setToolTipText(action.description());
            b.addActionListener(e -> run(() -> engine.chooseAction(action))); actions.put(action, b); actionRow.add(b);
        }
        bottom.add(actionRow);
        JPanel skillRow = panel(new GridLayout(1, 3, 10, 0));
        String[] labels = {"분석: 보기 제거", "보호: 반격 -15", "집중: 다음 피해 +15"};
        for (BattleEngine.Skill skill : BattleEngine.Skill.values()) {
            JButton b = button(labels[skill.ordinal()], "skill" + skill.name());
            b.addActionListener(e -> run(() -> engine.useSkill(skill))); skills.put(skill, b); skillRow.add(b);
        }
        bottom.add(skillRow);
        JPanel itemRow = panel(new GridLayout(1, 3, 10, 0));
        for (ShopItem item : ShopItem.values()) {
            JButton b = button(item.label(), "item" + item.name()); b.setToolTipText(item.description());
            b.addActionListener(e -> run(() -> { event.useItem(item); animation.start(); })); items.put(item, b); itemRow.add(b);
        }
        bottom.add(itemRow);
        JPanel controls = panel(new GridLayout(1, 3, 10, 0)); JButton quit = button("중단 · 보상 없이 메뉴로", "quit");
        quit.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "진행 중인 전투를 중단할까요? 사용한 아이템은 반환되지 않습니다.", "전투 중단", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { stopAnimation(); onMenu.run(); }
        });
        submit.addActionListener(e -> run(() -> {
            event.submit(); portrait.startHit(engine.feedback().damage(), engine.computerHp() == 0, System.nanoTime()); animation.start();
        }));
        next.addActionListener(e -> run(() -> { event.advance(); if (engine.phase() == BattleEngine.Phase.FINISHED) { stopAnimation(); onFinish.run(); } }));
        controls.add(quit); controls.add(submit); controls.add(next); bottom.add(controls); add(bottom, BorderLayout.SOUTH);
        bind("submit", KeyStroke.getKeyStroke("ENTER"), () -> { if (submit.isEnabled()) submit.doClick(); else next.doClick(); });
        refresh();
    }
    private void animate() {
        animateBar(playerHp, engine.playerHp()); animateBar(targetHp, engine.computerHp()); portrait.updateHit(System.nanoTime());
        if (!portrait.isAnimating() && playerHp.getValue() == engine.playerHp() && targetHp.getValue() == engine.computerHp()) animation.stop();
    }
    private void animateBar(JProgressBar bar, int target) {
        int value = bar.getValue(); if (value != target) bar.setValue(value + Integer.signum(target - value) * Math.max(1, Math.abs(target - value) / 5));
    }
    @FunctionalInterface private interface Command { void run() throws Exception; }
    private void run(Command command) {
        try { command.run(); refresh(); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "전투", JOptionPane.WARNING_MESSAGE); refresh(); }
    }
    public void refresh() {
        if (engine.phase() == BattleEngine.Phase.FINISHED) { submit.setEnabled(false); next.setEnabled(false); return; }
        boolean asking = engine.phase() == BattleEngine.Phase.QUESTION;
        progress.setText((engine.computer().getCategory().equals("교수님") ? "최종 구출 · " : engine.boss() ? "마을 보스 · " : "인물 도전 · ") + engine.computer().getName() + "  |  기회 " + Math.min(engine.turn() + 1, engine.questionCount()) + "/" + engine.questionCount());
        intent.setText("상대 예고: " + engine.intent());
        resources.setText("자원 " + engine.energy() + "/10 · 콤보 " + engine.combo() + " · " + engine.power().title() + " / " + engine.power().description());
        playerHp.setString("내 HP " + engine.playerHp() + "/" + engine.maxPlayerHp());
        targetHp.setString("상대 HP " + engine.computerHp() + "/" + engine.maxComputerHp());
        question.setText(event.session().getCurrentQuiz().getQuestion());
        for (int i = 0; i < 4; i++) {
            choices[i].setText("<html>" + (i + 1) + ". " + escape(engine.current().getChoices().get(i)) + "</html>");
            choices[i].setEnabled(asking && !engine.eliminated().contains(i));
            choices[i].putClientProperty("selected", engine.selected() == i);
            choices[i].putClientProperty("correct", !asking && i == engine.current().getAnswer() - 1);
            choices[i].setBackground(!asking && i == engine.current().getAnswer() - 1 ? new Color(185, 244, 202) : engine.selected() == i ? new Color(173, 216, 255) : Color.WHITE);
        }
        actions.forEach((a, b) -> { b.setEnabled(asking); b.putClientProperty("selected", engine.action() == a); b.repaint(); });
        skills.forEach((s, b) -> { b.setEnabled(asking); b.setText(b.getText().split(" \\[자원")[0] + " [자원 " + engine.skillCost(s) + "]"); });
        items.forEach((i, b) -> { b.setEnabled(asking); b.setText(i.label() + " ×" + event.profile().inventory(i)); });
        submit.setEnabled(asking); next.setEnabled(engine.phase() == BattleEngine.Phase.FEEDBACK);
        feedback.setText(asking ? "행동을 고르고 답을 제출하세요. 강공격은 위험하지만 강력합니다." : engine.feedback().explanation() + " · 공격 피해 " + engine.feedback().damage());
    }
    private static String escape(String text) { return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
    private static JPanel panel(LayoutManager layout) { JPanel p = new JPanel(layout); p.setOpaque(false); return p; }
    private static JLabel label(String text, int size) { JLabel l = new JLabel(text); l.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float)size)); l.setForeground(GameTheme.PAPER); return l; }
    private static JButton button(String text, String name) { JButton b = new UiButton(text, GameTheme.TEAL); b.setName(name); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f)); b.setBackground(Color.WHITE); return b; }
    private static JProgressBar hp(int maximum) { JProgressBar b = new JProgressBar(0, maximum); b.setValue(maximum); b.setStringPainted(true); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f)); b.setPreferredSize(new Dimension(200, 30)); return b; }
    private void bind(String name, KeyStroke key, Runnable action) { getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name); getActionMap().put(name, new AbstractAction() { @Override public void actionPerformed(java.awt.event.ActionEvent e) { action.run(); } }); }
    public void stopAnimation() { animation.stop(); }
    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "battle", 0.82f); g.dispose(); }
}
