package org.kgu.game.view;

import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.feature.BattleEngine;
import org.kgu.progression.ShopItem;
import org.kgu.ui.battle.FinalAttackEffect;
import org.kgu.ui.battle.ImpactCard;
import org.kgu.window.GameTheme;
import org.kgu.window.UiStyle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/** Displays the engine state; the event adapter owns commands, persistence and navigation. */
public final class BattlePanel extends JPanel {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 960;
    private static final Color BLUE = new Color(66, 163, 199);
    private static final Color INK = new Color(229, 243, 253);
    private static final Color MUTED = new Color(158, 193, 219);
    private final JLabel[] milestones = new JLabel[5];
    private final Boolean[] answers = new Boolean[5];
    private final JLabel objective = label("", 20);
    private final JLabel questionTag = label("", 15);
    private final JProgressBar countdown = new BattleProgressBar(1000);
    private static final Color GREEN = new Color(118, 224, 174);
    private static final Color RED = new Color(255, 137, 144);
    private final BattleEngine engine;
    private final BattleGUIEvent event;
    private final Runnable onFinish;
    private final LongSupplier clock;
    private final Map<Component, Rectangle> positions = new LinkedHashMap<>();
    private final Map<Component, Integer> fontSizes = new LinkedHashMap<>();
    private final EnumMap<ShopItem, JButton> items = new EnumMap<>(ShopItem.class);
    private final JButton[] choices = new JButton[4];
    private final JLabel progress = label("", 21);
    private final JLabel hpText = label("", 19);
    private final JLabel hitText = label("", 30);
    private final JLabel itemStatus = label("", 17);
    private final JTextArea feedback = textArea(19);
    private final JTextArea question = textArea(25);
    private final JTextArea memory = textArea(17);
    private final JProgressBar targetHp = new BattleProgressBar(BattleEngine.MAX_HP);
    private final JButton submit = button("정답 제출", "submit");
    private final ImpactCard portrait;
    private final FinalAttackEffect finalAttack = new FinalAttackEffect();
    private final BattleAttackEffect attackEffect;
    private final BattleFinishEffect finishEffect = new BattleFinishEffect();
    private boolean finishingHit;
    private final Timer animation;
    private long effectStarted;
    private boolean ending;
    private boolean closed;

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
        addAt(label("WHO’S NEXT?     /     QUIZ BATTLE", 19), 40, 22, 880, 35, 19);
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
        addAt(label("MISSION  /  배틀 목표", 14), 46, 82, 280, 25, 14);
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
        guide.setForeground(new Color(169, 194, 209));
        addAt(guide, 46, 263, 280, 28, 14);
        addAt(label("인물 정보", 18), 46, 326, 270, 28, 18);
        memory.setName("memoryInfo");
        memory.setText("상대의 정보를 확인하세요.\n정보 물약으로 인물 정보를 열 수 있어요.");
        memory.setForeground(new Color(172, 195, 211));
        addAt(GameTheme.scroll(memory), 46, 368, 266, 110, 16);
        hitText.setName("hitFeedback");
        hitText.setHorizontalAlignment(SwingConstants.CENTER);
        addAt(hitText, 350, 478, 580, 37, 23);
    }

    private void buildQuestions() {
        questionTag.setForeground(new Color(135, 214, 251));
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
        addAt(label("SUPPORT  /  가방", 14), 950, 82, 275, 25, 14);
        JLabel note = label("한 번의 도움, 끝까지 내 힘으로", 16);
        note.setForeground(new Color(169, 194, 209));
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
            description.setForeground(new Color(169, 194, 209));
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
        objective.setForeground(engine.successCount() >= 3 ? GREEN : GameTheme.PAPER);
        questionTag.setText(engine.isRetry() ? "RETRY  /  추가 도전" : "QUESTION 0" + (engine.currentQuestionIndex() + 1) + "  /  배틀 퀴즈");
        countdown.setVisible(!asking && !ending);
        for (int i = 0; i < milestones.length; i++) {
            Boolean result = answers[i];
            milestones[i].setText(result == null ? Integer.toString(i + 1) : result ? "O" : "X");
            milestones[i].setBackground(result == null ? !ending && i == engine.turn() ? BLUE : new Color(37, 61, 80)
                    : result ? new Color(34, 107, 88) : new Color(123, 60, 71));
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
            memory.setForeground(GameTheme.PAPER);
        }
        submit.setEnabled(asking && engine.selected() >= 0);
        submit.setText(ending ? "결과 확인 중" : asking ? engine.selected() < 0 ? "보기를 선택하세요" : (engine.selected() + 1) + "번 답안 제출  →" : "다음 문제로…");
        if (ending) {
            hitText.setText(engine.result() == BattleEngine.Result.WIN ? "" : "");
            feedback.setText("응답 " + engine.answeredCount() + "/5 · 정답 " + engine.correctCount() + " · 재도전 보충 " + engine.retryCorrectCount());
        } else if (asking) {
            hitText.setText(engine.successCount() >= 3 ? "승리 조건 달성 · 마지막까지 공격!" : "답을 골라 공격하세요");
            hitText.setForeground(new Color(153, 225, 255));
            feedback.setText(engine.isRetry() ? "보기를 다시 섞었어요. 재도전 정답은 성공 조건만 보충하며 최초 정답률·경험치에는 더하지 않습니다."
                    : "보기 1~4 또는 클릭으로 선택 · Enter로 제출\n정답과 설명을 확인한 뒤 자동으로 다음 문제로 넘어갑니다.");
        } else {
            boolean correct = engine.feedback().correct();
            hitText.setText(correct ? engine.isRetry() ? "복습 성공!" : "정답! 공격 성공" : "오답 · 정답을 확인하세요");
            hitText.setForeground(correct ? GREEN : RED);
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
        GameTheme.backdrop(g, getWidth(), getHeight(), "battle", 0.60f);
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        g.translate((getWidth() - WIDTH * scale) / 2, (getHeight() - HEIGHT * scale) / 2);
        g.scale(scale, scale);
        g.setPaint(new GradientPaint(0, 0, new Color(9, 23, 39, 235), 0, 540, new Color(9, 23, 39, 90)));
        g.fillRect(0, 0, WIDTH, 540);
        g.setColor(new Color(153, 199, 222, 40));
        g.drawLine(40, 70, 1240, 70);
        g.setPaint(new RadialGradientPaint(640, 330, 285,
                new float[]{0, 1}, new Color[]{new Color(66, 174, 211, 100), new Color(66, 174, 211, 0)}));
        g.fillOval(355, 60, 570, 510);
        g.setColor(new Color(146, 210, 230, 48));
        g.setStroke(new BasicStroke(1.2f));
        g.drawOval(429, 178, 422, 300);
        g.drawOval(415, 461, 450, 22);
        // Quiet side panels frame useful information without competing with the character.
        g.setColor(new Color(16, 35, 52, 195));
        g.fillRoundRect(28, 150, 305, 153, 22, 22);
        g.fillRoundRect(28, 314, 305, 192, 22, 22);
        g.fillRoundRect(930, 150, 314, 356, 22, 22);
        g.setColor(new Color(9, 26, 47));
        g.fillRoundRect(28, 534, 1224, 414, 28, 28);
        g.setColor(new Color(28, 57, 86));
        g.fillRoundRect(28, 530, 1224, 414, 28, 28);
        g.setColor(new Color(83, 148, 190, 150));
        g.drawRoundRect(28, 530, 1224, 414, 28, 28);
        g.setColor(new Color(102, 204, 244));
        g.fillRoundRect(58, 581, 52, 3, 3, 3);
        g.setColor(new Color(107, 180, 216, 65));
        for (int i = 0; i < 7; i++) {
            int x = 450 + i * 63;
            g.drawLine(x, 514, x + 28, 514);
        }
        g.setColor(new Color(72, 117, 151));
        g.drawLine(58, 839, 1222, 839);
        g.dispose();
    }

    private void addAt(Component component, int x, int y, int width, int height, int fontSize) {
        add(component);
        setComponentZOrder(component, 0);
        positions.put(component, new Rectangle(x, y, width, height));
        fontSizes.put(component, fontSize);
    }

    @Override public void doLayout() {
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        int dx = (int) ((getWidth() - WIDTH * scale) / 2);
        int dy = (int) ((getHeight() - HEIGHT * scale) / 2);
        positions.forEach((component, rect) -> {
            component.setBounds(dx + (int) (rect.x * scale), dy + (int) (rect.y * scale),
                    (int) (rect.width * scale), (int) (rect.height * scale));
            component.setFont(UiStyle.FONT.deriveFont(Font.BOLD, Math.max(12f, (float) (fontSizes.get(component) * scale))));
            if (component instanceof JScrollPane scroll && scroll.getViewport().getView() != null) {
                scroll.getViewport().getView().setFont(UiStyle.FONT.deriveFont(Font.BOLD, Math.max(12f, (float) (fontSizes.get(component) * scale))));
            }
            if (component instanceof Container container) container.doLayout();
        });
    }

    private static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float) size));
        label.setForeground(GameTheme.PAPER);
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
