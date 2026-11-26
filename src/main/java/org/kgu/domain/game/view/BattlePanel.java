package org.kgu.domain.game.view;

import org.kgu.domain.game.view.component.*;
import org.kgu.core.ui.GradientTokens;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.domain.game.view.layout.BattleLayout;

import org.kgu.domain.game.event.BattleGUIEvent;
import org.kgu.domain.game.feature.BattleEngine;
import org.kgu.core.progression.ShopItem;
import org.kgu.domain.battle.view.component.FinalAttackEffect;
import org.kgu.domain.battle.view.component.ImpactCard;
import org.kgu.core.window.GameTheme;
import org.kgu.core.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.function.LongSupplier;

/** Displays the engine state; the event adapter owns commands, persistence and navigation. */
public final class BattlePanel extends JPanel {
    private static final int WIDTH = BattleLayout.WIDTH;
    private static final int HEIGHT = BattleLayout.HEIGHT;
    private static final Color BLUE = new Color(66, 163, 199);
    private static final Color INK = ComponentPainter.TEXT;
    private static final Color MUTED = ComponentPainter.MUTED;
    private final JLabel[] milestones = new JLabel[5];
    private final Boolean[] answers = new Boolean[5];
    private final JLabel objective = label("", 20);
    private final JLabel questionTag = label("", 15);
    private final JProgressBar countdown = new BattleProgressBar(1000, GradientTokens.PROGRESS);
    private static final Color GREEN = ComponentPainter.CORRECT;
    private final BattleEngine engine;
    private final BattleGUIEvent event;
    private final Runnable onFinish;
    private final LongSupplier clock;
    private final EnumMap<ShopItem, JButton> items = new EnumMap<>(ShopItem.class);
    private final JButton[] choices = new JButton[4];
    private final JLabel progress = label("", 21);
    private final JLabel hpText = label("", 19);
    private final JLabel hitText = label("", 30);
    private final JLabel itemStatus = label("", 17);
    private final JTextArea feedback = textArea(19);
    private final JTextArea question = textArea(25);
    private final JTextArea memory = textArea(17);
    private final JProgressBar targetHp = new BattleProgressBar(BattleEngine.MAX_HP, GradientTokens.HP_ENEMY);
    private final JButton submit = button("정답 제출", "submit");
    private final ImpactCard portrait;
    private final FinalAttackEffect finalAttack = new FinalAttackEffect();
    private final BattleAttackEffect attackEffect;
    private final BattleFinishEffect finishEffect = new BattleFinishEffect();
    private boolean finishingHit;
    private final Timer animation;
    private final BattleLayout battleLayout = new BattleLayout();
    private long effectStarted;
    private boolean ending;
    private boolean closed;
    private Boolean showcaseAnswer;

    void playShowcaseAnswers(boolean correct, boolean continuous) {
        showcaseAnswer = continuous ? correct : null;
        playAnswer(correct);
    }

    private void playAnswer(boolean correct) {
        int index = engine.current().getAnswer() - 1;
        event.select(correct ? index : (index + 1) % 4);
        submitAnswer();
    }

    public BattlePanel(BattleEngine engine, BattleGUIEvent event, Runnable onFinish, Runnable onMenu) {
        this(engine, event, onFinish, onMenu, System::nanoTime);
    }

    // A clock is injected in UI tests to verify animation order without sleeps.
    BattlePanel(BattleEngine engine, BattleGUIEvent event, Runnable onFinish, Runnable onMenu, LongSupplier clock) {
        this.engine = engine;
        this.event = event;
        this.onFinish = onFinish;
        this.clock = clock;
        if (engine.phase() != BattleEngine.Phase.QUESTION || engine.turn() != 0) {
            throw new IllegalArgumentException("새로 시작한 전투를 전달하세요.");
        }
        setLayout(null);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(960, 720));
        animation = new Timer(16, e -> animate());
        animation.setCoalesce(true);
        portrait = new ImpactCard(engine.computer().getName(), event.portrait(engine.computer()), BLUE,
                "");
        portrait.setExternalHitEffect(true);
        attackEffect = new BattleAttackEffect(engine.computer().getCategory());
        buildHeader(onMenu);
        buildArena();
        buildQuestions();
        buildItems();
        refresh();
    }

    private void buildHeader(Runnable onMenu) {
        JLabel title = label("퀴즈 배틀", 19);
        title.setForeground(Color.WHITE);
        addAt(title, 48, 22, 880, 35, 19);
        JButton menu = button("메인으로", "quit");
        menu.addActionListener(e -> {
            if (closed) return;
            // Pause visual time while the confirmation dialog is open.
            boolean running = animation.isRunning();
            animation.stop();
            long before = clock.getAsLong();
            int answer = JOptionPane.showConfirmDialog(this,
                    "도전을 중단할까요? 사용한 아이템은 반환되지 않습니다.", "전투 중단", JOptionPane.YES_NO_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                stopAnimation();
                onMenu.run();
            } else if (running && !closed) {
                long pause = clock.getAsLong() - before;
                effectStarted += pause;
                // Effect clocks use elapsed visual time, so they pause together below.
                pausedNanos += pause;
                animation.start();
            }
        });
        addAt(menu, 1110, 22, 130, 38, 15);
        addAt(progress, 46, 112, 280, 30, 16);
        JLabel target = label(engine.computer().getCategory() + " · " + engine.computer().getName(), 24);
        target.setHorizontalAlignment(SwingConstants.CENTER);
        addAt(target, 380, 84, 520, 37, 24);
        targetHp.setName("targetHp");
        targetHp.setValue(BattleEngine.MAX_HP);
        targetHp.setForeground(BLUE);
        targetHp.setBackground(new Color(34, 53, 76));
        targetHp.setBorder(BorderFactory.createEmptyBorder());
        addAt(targetHp, 450, 130, 380, 10, 14);
        hpText.setHorizontalAlignment(SwingConstants.CENTER);
        addAt(hpText, 410, 146, 460, 25, 16);
    }

    private void buildArena() {
        addAt(portrait, 490, 180, 300, 290, 20);
        addAt(label("배틀 목표", 14), 46, 82, 280, 25, 14);
        addAt(objective, 46, 166, 275, 30, 20);
        for (int i = 0; i < milestones.length; i++) {
            JLabel step = label(Integer.toString(i + 1), 18);
            step.setName("questionStep" + i);
            step.setOpaque(true);
            step.setHorizontalAlignment(SwingConstants.CENTER);
            milestones[i] = step;
            addAt(step, 46 + i * 51, 212, 43, 38, 18);
        }
        JLabel guide = label("정답 3개로 승리 · 역전 불가 시 패배", 14);
        guide.setForeground(MUTED);
        addAt(guide, 46, 263, 280, 28, 14);
        JLabel memoryTitle = label("인물 정보", 18);
        memoryTitle.setForeground(INK);
        addAt(memoryTitle, 46, 326, 270, 28, 18);
        memory.setName("memoryInfo");
        memory.setText("상대의 정보를 확인하세요.\n정보 물약으로 인물 정보를 열 수 있어요.");
        memory.setForeground(MUTED);
        addAt(GameTheme.scroll(memory), 46, 368, 266, 110, 16);
        hitText.setName("hitFeedback");
        hitText.setHorizontalAlignment(SwingConstants.CENTER);
        addAt(hitText, 350, 478, 580, 37, 23);
    }

    private void buildQuestions() {
        questionTag.setForeground(ComponentPainter.SELECTED);
        addAt(questionTag, 58, 550, 750, 25, 15);
        JLabel shortcuts = label("1–4 선택    /    ENTER 제출", 13);
        shortcuts.setForeground(MUTED);
        shortcuts.setHorizontalAlignment(SwingConstants.RIGHT);
        addAt(shortcuts, 900, 550, 320, 25, 13);
        question.setName("battleQuestion");
        question.setForeground(INK);
        question.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 26f));
        addAt(GameTheme.scroll(question), 58, 592, 1160, 79, 26);
        for (int i = 0; i < choices.length; i++) {
            int index = i;
            JButton choice = button("", "choice" + i);
            choice.setHorizontalAlignment(SwingConstants.LEFT);
            choices[i] = choice;
            choice.addActionListener(e -> command(() -> event.select(index)));
            addAt(choice, i % 2 == 0 ? 58 : 648, i < 2 ? 687 : 763, 574, 64, 20);
            bind("choice" + i, KeyStroke.getKeyStroke((char) ('1' + i)), () -> {
                if (choice.isEnabled()) choice.doClick();
            });
        }
        feedback.setName("battleFeedback");
        feedback.setForeground(INK);
        addAt(GameTheme.scroll(feedback), 58, 852, 832, 64, 17);
        submit.addActionListener(e -> submitAnswer());
        addAt(submit, 932, 851, 290, 64, 20);
        countdown.setName("feedbackCountdown");
        countdown.setForeground(BLUE);
        countdown.setBackground(new Color(16, 39, 65));
        countdown.setBorder(BorderFactory.createEmptyBorder());
        countdown.setVisible(false);
        addAt(countdown, 58, 926, 1164, 10, 12);
        bind("submit", KeyStroke.getKeyStroke("ENTER"), () -> {
            if (!closed && submit.isEnabled()) submit.doClick();
        });
    }

    private void buildItems() {
        addAt(label("가방", 14), 950, 82, 275, 25, 14);
        JLabel note = label("한 번의 도움, 끝까지 내 힘으로", 16);
        note.setForeground(ComponentPainter.MUTED);
        addAt(note, 950, 116, 280, 30, 16);
        String[] notes = {"헷갈리는 오답 보기 1개 제거", "마지막에 첫 오답 한 번 재도전", "이번 전투에서 인물 정보 열람"};
        int index = 0;
        for (ShopItem item : ShopItem.values()) {
            JButton button = button(item.label(), "item" + item.name());
            button.addActionListener(e -> command(() -> event.useItem(item)));
            items.put(item, button);
            int y = 174 + index * 94;
            addAt(button, 950, y, 276, 48, 18);
            JLabel description = label(notes[index], 13);
            description.setForeground(MUTED);
            addAt(description, 958, y + 51, 270, 24, 13);
            index++;
        }
        addAt(itemStatus, 950, 461, 276, 33, 14);
    }

    @FunctionalInterface private interface Command { void run() throws Exception; }

    private void command(Command command) {
        if (closed || ending || engine.phase() != BattleEngine.Phase.QUESTION) return;
        try {
            command.run();
            refresh();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "전투", JOptionPane.WARNING_MESSAGE);
            refresh();
        }
    }

    private void submitAnswer() {
        command(() -> {
            event.submit();
            countdown.setValue(1000);
            effectStarted = clock.getAsLong();
            pausedNanos = 0;
            BattleEngine.TurnFeedback result = engine.feedback();
            if (!engine.isRetry()) answers[engine.currentQuestionIndex()] = result.correct();
            if (result.correct()) {
                attackEffect.start(effectStarted);
                portrait.startHit(result.damage(), false, effectStarted);
            }
            animation.start();
        });
    }

    private long pausedNanos;

    private void animate() {
        if (closed) return;
        long now = clock.getAsLong();
        long visualNow = now - pausedNanos;
        double elapsed = (now - effectStarted) / 1_000_000_000.0;
        portrait.updateHit(visualNow);
        attackEffect.update(visualNow);
        if (!ending) {
            double feedbackDuration = engine.feedback().correct() ? 2.2 : 3.2;
            countdown.setValue((int) (1000 * Math.max(0, 1 - elapsed / feedbackDuration)));
            submit.setText(String.format(java.util.Locale.ROOT, "자동 진행  %.1f초", Math.max(0, feedbackDuration - elapsed)));
        }
        int value = targetHp.getValue();
        int target = engine.computerHp();
        if (value != target) targetHp.setValue(value + Integer.signum(target - value) * Math.max(1, Math.abs(target - value) / 5));
        updateHpText();
        repaint();
        if (ending) {
            finishEffect.update(visualNow);
            finalAttack.update(visualNow);
            if (!finishingHit && finishEffect.impacted()) {
                finishingHit = true;
                if (engine.result() == BattleEngine.Result.WIN) portrait.startHit(0, true, visualNow);
            }
            if (!finishEffect.isRunning()) finishOnce();
        } else if (elapsed >= (engine.feedback().correct() ? 2.2 : 3.2) && !portrait.isAnimating()) {
            event.advance();
            if (engine.phase() == BattleEngine.Phase.FINISHED) {
                ending = true;
                effectStarted = now;
                pausedNanos = 0;
                finishEffect.start(engine.result() == BattleEngine.Result.WIN, engine.computer().getCategory(), now);
                if (engine.result() == BattleEngine.Result.LOSE && isProfessor()) finalAttack.start(now);
                refresh();
            } else {
                animation.stop();
                refresh();
                if (showcaseAnswer != null) playAnswer(showcaseAnswer);
            }
        }
    }

    private void finishOnce() {
        if (closed) return;
        stopAnimation();
        onFinish.run();
    }

    public void refresh() {
        boolean asking = !closed && !ending && engine.phase() == BattleEngine.Phase.QUESTION;
        progress.setText(ending ? "전투 종료 · 응답 " + engine.answeredCount() + " / 5" : engine.isRetry() ? "보호 부적 · 오답 재도전"
                : "문제 " + Math.min(engine.turn() + 1, 5) + " / 5  ·  정답 " + engine.correctCount());
        objective.setText(engine.successCount() >= 3 ? "승리 조건 달성!" : "정답  " + engine.successCount() + " / 3");
        objective.setForeground(engine.successCount() >= 3 ? GREEN : INK);
        questionTag.setText(engine.isRetry() ? "RETRY  /  추가 도전" : "QUESTION 0" + (engine.currentQuestionIndex() + 1) + "  /  배틀 퀴즈");
        countdown.setVisible(!asking && !ending);
        for (int i = 0; i < milestones.length; i++) {
            Boolean result = answers[i];
            milestones[i].setText(result == null ? Integer.toString(i + 1) : result ? "O" : "X");
            milestones[i].setBackground(result == null ? (!ending && i == engine.turn() ? new Color(0xCFE5FF) : GameTheme.PAGE)
                    : result ? new Color(0xDDF8E8) : new Color(0xFFE7E4));
            milestones[i].getAccessibleContext().setAccessibleName((i + 1) + "번 문제 " + (result == null ? "미응답" : result ? "정답" : "오답"));
        }
        question.setText(engine.current().getQuestion());
        question.setCaretPosition(0);
        for (int i = 0; i < choices.length; i++) {
            JButton choice = choices[i];
            boolean removed = engine.eliminated().contains(i);
            String state = !asking && i == engine.current().getAnswer() - 1 ? "정답 · "
                    : !asking && engine.selected() == i ? "내 답 · " : "";
            choice.setText(state + (i + 1) + ". " + (removed ? "제외된 보기" : engine.current().getChoices().get(i)));
            choice.setEnabled(asking && !removed);
            choice.putClientProperty("selected", engine.selected() == i);
            choice.putClientProperty("correct", !asking && i == engine.current().getAnswer() - 1);
            choice.putClientProperty("wrong", !asking && engine.selected() == i && i != engine.current().getAnswer() - 1);
            choice.getAccessibleContext().setAccessibleName((i + 1) + "번 " + (removed ? "제외된 보기" : engine.current().getChoices().get(i)));
            choice.repaint();
        }
        items.forEach((item, button) -> {
            int count = event.profile().inventory(item);
            String reason = count == 0 ? "보유한 아이템이 없습니다." : engine.itemUnavailableReason(item).orElse("");
            button.setEnabled(asking && reason.isEmpty());
            button.setText(item.label() + (engine.itemUsed().orElse(null) == item ? "  ·  사용 중" : "  × " + count));
            button.setToolTipText(reason.isEmpty() ? item.description() : reason);
        });
        itemStatus.setText(engine.itemUsed().map(item -> item.label() + " 사용 완료").orElse("아이템은 선택 사항이에요"));
        if (engine.memoryVisible()) {
            memory.setText(engine.memoryText());
            memory.setForeground(INK);
        }
        submit.setEnabled(asking && engine.selected() >= 0);
        submit.setText(ending ? "결과 확인 중" : asking ? engine.selected() < 0 ? "보기를 선택하세요" : (engine.selected() + 1) + "번 답안 제출  →" : "다음 문제로…");
        if (ending) {
            hitText.setText("");
            feedback.setText("응답 " + engine.answeredCount() + "/5 · 정답 " + engine.correctCount() + " · 재도전 보충 " + engine.retryCorrectCount());
        } else if (asking) {
            hitText.setText(engine.successCount() >= 3 ? "승리 조건 달성 · 마지막까지 공격!" : "답을 골라 공격하세요");
            hitText.setForeground(GameTheme.BLUE_TEXT);
            feedback.setText(engine.isRetry() ? "보기를 다시 섞었어요. 재도전 정답은 성공 조건만 보충하며 최초 정답률·경험치에는 더하지 않습니다."
                    : "보기 1~4 또는 클릭으로 선택 · Enter로 제출\n정답과 설명을 확인한 뒤 자동으로 다음 문제로 넘어갑니다.");
        } else {
            boolean correct = engine.feedback().correct();
            hitText.setText(correct ? engine.isRetry() ? "복습 성공!" : "정답! 공격 성공" : "오답 · 정답을 확인하세요");
            hitText.setForeground(correct ? ComponentPainter.CORRECT : ComponentPainter.INCORRECT);
            feedback.setText(engine.feedback().explanation() + "\n" + (engine.isRetry() ? "최초 정답률과 별 평가에는 추가하지 않습니다."
                    : "최초 정답 " + engine.correctCount() + "/5 · 성공까지 " + Math.max(0, 3 - engine.successCount()) + "개"));
        }
        updateHpText();
        repaint();
    }

    private void updateHpText() {
        hpText.setText(targetHp.getValue() == 0 ? ending ? "HP 0 / 60" : "HP 0 · 마지막 문제 후 마무리" : "HP " + targetHp.getValue() + " / 60");
    }

    private boolean isProfessor() { return "교수님".equals(engine.computer().getCategory()); }

    public void stopAnimation() {
        closed = true;
        animation.stop();
        submit.setEnabled(false);
        for (JButton choice : choices) choice.setEnabled(false);
        items.values().forEach(button -> button.setEnabled(false));
    }

    @Override public void removeNotify() { stopAnimation(); super.removeNotify(); }

    @Override public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        double elapsed = (clock.getAsLong() - effectStarted) / 1_000_000_000.0;
        boolean wrong = !closed && !ending && engine.phase() == BattleEngine.Phase.FEEDBACK && !engine.feedback().correct();
        int shake = finalAttack.isRunning() ? finalAttack.shakeOffset() : finishEffect.isRunning() ? finishEffect.shakeOffset() : portrait.shakeOffset();
        if (wrong && elapsed < 0.45) shake = (int) (Math.sin(elapsed * 85) * 7 * (1 - elapsed / 0.45));
        g.translate(shake, -shake / 2);
        super.paint(g);
        if (wrong && elapsed < 0.6) {
            g.setColor(new Color(220, 50, 80, (int) (65 * Math.max(0, 1 - elapsed / 0.6))));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (!ending) attackEffect.paint(g, getWidth(), getHeight());
        finishEffect.paint(g, getWidth(), getHeight());
        finalAttack.paint(g, getWidth(), getHeight());
        g.dispose();
    }

    @Override protected void paintComponent(Graphics source) {
        super.paintComponent(source);
        Graphics2D g = (Graphics2D) source.create();
        UiStyle.prepare(g);
        BattleLayout.paintStage(g, getWidth(), getHeight());
        g.dispose();
    }

    private void addAt(Component component, int x, int y, int width, int height, int fontSize) {
        add(component);
        setComponentZOrder(component, 0);
        battleLayout.add(component, new Rectangle(x, y, width, height), fontSize);
    }

    @Override public void doLayout() {
        battleLayout.place(getWidth(), getHeight());
    }

    private static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float) size));
        label.setForeground(ComponentPainter.TEXT);
        return label;
    }
    private static JTextArea textArea(int size) {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setOpaque(false);
        text.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float) size));
        text.setForeground(GameTheme.PAPER);
        return text;
    }
    private static JButton button(String text, String name) {
        JButton button = new BattleButton(text, name.startsWith("choice"), name.equals("submit"));
        button.setName(name);
        return button;
    }
    private void bind(String name, KeyStroke key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }
}
