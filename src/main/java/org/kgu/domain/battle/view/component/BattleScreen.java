package org.kgu.domain.battle.view.component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.*;
import org.kgu.domain.battle.feature.BattleEngine;
import static org.kgu.domain.battle.view.component.BattleComponents.*;

/** Shows one professor. Game rules live in BattleEngine; the caller owns saving/navigation. */
public final class BattleScreen extends JPanel {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 960;
    private static final Color NAVY = new Color(17, 40, 85);
    private static final Color BLUE = new Color(20, 125, 244);
    private static final Font FONT = new Font("Malgun Gothic", Font.BOLD, 20);

    private final BattleEngine engine;
    private final Consumer<BattleEngine> onFinished;
    private final BufferedImage background = backgroundImage();
    private final Map<Component, Rectangle> bounds = new LinkedHashMap<>();
    private final Map<Component, Integer> fontSizes = new LinkedHashMap<>();
    private final JProgressBar hpBar = new JProgressBar(0, BattleEngine.MAX_HP);
    private final ImpactCard portrait;
    private final FinalAttackEffect finalAttack = new FinalAttackEffect();
    private final JLabel hpText = label("", NAVY);
    private final JLabel progress = label("", NAVY);
    private final JLabel damageTotal = label("", NAVY);
    private final JLabel battleStatus = label("", BLUE);
    private final JLabel dialogue = label("", NAVY);
    private final JTextArea question = textArea(Color.WHITE);
    private final JTextArea feedback = textArea(NAVY);
    private final JToggleButton[] choices = new JToggleButton[4];
    private final ButtonGroup answerGroup = new ButtonGroup();
    private final JButton hint = new ActionButton("힌트 (잠김)", BLUE);
    private final JButton cheer = new ActionButton("응원 (잠김)", BLUE);
    private final JButton submit = new ActionButton("정답으로 공격", BLUE);
    private static final int CORRECT_DELAY_MS = 1000;
    private static final int WRONG_DELAY_MS = 2400;
    private final Timer animation;
    private final Timer autoAdvance;
    private boolean closed;

    public BattleScreen(BattleEngine engine, Consumer<BattleEngine> onFinished, Runnable onExit) {
        this.engine = Objects.requireNonNull(engine);
        this.onFinished = Objects.requireNonNull(onFinished);
        Objects.requireNonNull(onExit);
        if (engine.phase() != BattleEngine.Phase.QUESTION || engine.questionIndex() != 0) {
            throw new IllegalArgumentException("새 전투를 전달하세요.");
        }

        setLayout(null);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(800, 600));
        portrait = new ImpactCard("정체를 알 수 없는 인물", engine.target().imagePath(), BLUE,
                "교수님".equals(engine.target().category()) ? "세뇌 해제" : "도전 성공");
        animation = new Timer(16, e -> animateHit());
        autoAdvance = new Timer(0, e -> advance());
        autoAdvance.setRepeats(false);
        addHeader(onExit);
        addProfessorAndStory();
        addSideAccents();
        addQuestionAndChoices();
        addActions();
        refresh();
    }

    private void addHeader(Runnable onExit) {
        addAt(new RoundedPanel(new Color(238, 248, 255), new Color(198, 226, 252), 16),
                8, 8, 1264, 48, 20);
        addAt(label("WHO’S NEXT? — 교수님의 기억을 찾아서", NAVY), 30, 16, 810, 32, 26);
        JButton menu = new ActionButton("메인으로", BLUE);
        menu.setName("menu");
        menu.addActionListener(e -> {
            if (!closed) {
                closed = true;
                stopTimers();
                onExit.run();
            }
        });
        addAt(menu, 1100, 14, 158, 35, 18);
    }

    private void addProfessorAndStory() {
        // Keep the target, HP and hit effects on the same central axis.
        addAt(new RoundedPanel(new Color(255, 255, 255, 240), new Color(222, 238, 254, 240), 26),
                360, 70, 560, 85, 20);
        addAt(label(engine.target().category() + " · 정체를 알 수 없는 인물", NAVY), 385, 76, 510, 30, 25);
        hpBar.setName("professorHp");
        hpBar.setValue(BattleEngine.MAX_HP);
        hpBar.setForeground(new Color(220, 63, 89));
        hpBar.setBackground(new Color(232, 213, 221));
        addAt(hpText, 385, 108, 510, 23, 17);
        addAt(hpBar, 390, 135, 500, 12, 18);

        portrait.setName("professorCard");
        addAt(portrait, 475, 171, 330, 348, 20);
        addAt(new RoundedPanel(new Color(255, 255, 255, 238), new Color(235, 245, 255, 238), 24),
                310, 530, 660, 44, 20);
        dialogue.setName("dialogue");
        addAt(dialogue, 325, 538, 630, 28, 20);
    }

    private void addSideAccents() {
        // A small category emblem and temporary hit text replace permanent information boxes.
        addAt(label(engine.target().category(), NAVY), 125, 431, 210, 32, 23);
        battleStatus.setName("battleStatus");
        damageTotal.setName("hitDamage");
        addAt(battleStatus, 870, 280, 340, 64, 39);
        addAt(damageTotal, 880, 349, 320, 40, 27);
    }

    private void addQuestionAndChoices() {
        feedback.setName("feedback");
        addAt(feedback, 105, 580, 1070, 44, 16);
        addAt(new RoundedPanel(new Color(255, 255, 255, 245), new Color(229, 240, 254, 245), 28),
                74, 629, 1132, 208, 20);
        addAt(progress, 95, 632, 370, 28, 19);
        addAt(label("난이도: " + engine.current().difficulty(), BLUE), 985, 632, 185, 28, 18);
        addAt(new RoundedPanel(new Color(48, 103, 180), new Color(24, 68, 137), 20),
                98, 666, 1084, 58, 20);
        question.setName("question");
        addAt(question, 118, 673, 1044, 47, 22);
        for (int i = 0; i < choices.length; i++) {
            int answer = i;
            JToggleButton choice = new ChoiceButton();
            choice.setName("choice" + i);
            choice.addActionListener(e -> choose(answer));
            choices[i] = choice;
            answerGroup.add(choice);
            addAt(choice, i % 2 == 0 ? 116 : 654, i < 2 ? 734 : 785, 510, 44, 19);
            bind("choice" + i, KeyStroke.getKeyStroke((char) ('1' + i)), () -> choose(answer));
        }
    }

    private void addActions() {
        hint.setName("hint");
        submit.setName("submit");
        // Inventory/reward rules are not finalized. Do not grant free hints or buffs here.
        hint.setEnabled(false);
        hint.setToolTipText("아직 사용할 수 있는 힌트가 없습니다.");
        cheer.setName("cheer");
        cheer.setEnabled(false);
        cheer.setToolTipText("아직 사용할 수 있는 응원이 없습니다.");
        submit.addActionListener(e -> submitAnswer());
        addAt(hint, 92, 861, 240, 72, 23);
        addAt(cheer, 354, 861, 240, 72, 23);
        addAt(submit, 620, 861, 568, 72, 29);
        bind("confirm", KeyStroke.getKeyStroke("ENTER"), () -> {
            if (engine.phase() == BattleEngine.Phase.QUESTION) submitAnswer();
        });
    }

    private void choose(int answer) {
        if (closed || engine.phase() != BattleEngine.Phase.QUESTION) return;
        engine.select(answer);
        refresh();
    }

    private void submitAnswer() {
        if (closed || engine.phase() != BattleEngine.Phase.QUESTION || engine.selected() < 0) return;
        engine.submit();
        refresh();
        // Game damage is already committed once. The timer only draws the hit.
        if (engine.lastCorrect() && isShowing()) {
            portrait.startHit(engine.lastDamage(), engine.defeated(), System.nanoTime());
            animation.start();
        } else {
            hpBar.setValue(engine.remainingHp());
            updateHpLabel();
            queueAdvance();
        }
    }

    private void advance() {
        if (closed || animation.isRunning() || engine.phase() != BattleEngine.Phase.FEEDBACK) return;
        animation.stop();
        hpBar.setValue(engine.remainingHp());
        engine.advance();
        if (engine.phase() == BattleEngine.Phase.FINISHED) {
            submit.setEnabled(false);
            hint.setEnabled(false);
            if (!engine.won() && "교수님".equals(engine.target().category()) && isShowing()) {
                finalAttack.start(System.nanoTime());
                animation.start();
            } else {
                finishBattle();
            }
        } else {
            refresh();
        }
    }

    private void finishBattle() {
        if (closed) return;
        closed = true;
        stopTimers();
        onFinished.accept(engine);
    }

    private void refresh() {
        boolean asking = engine.phase() == BattleEngine.Phase.QUESTION;
        progress.setText("공격 기회 " + (engine.questionIndex() + 1) + " / " + engine.questionCount());
        updateHpLabel();
        dialogue.setText(storyText());
        question.setText(engine.current().text());
        question.setCaretPosition(0);
        refreshChoices(asking);
        submit.setEnabled(asking && engine.selected() >= 0);
        submit.setText(asking ? "정답으로 공격" : "공격 결과");
        battleStatus.setVisible(!asking);
        damageTotal.setVisible(!asking);
        battleStatus.setText(engine.defeated() ? "격파 성공!" : engine.lastCorrect() ? "공격 적중!" : "공격 실패");
        battleStatus.setForeground(engine.lastCorrect() ? new Color(22, 79, 160) : new Color(105, 94, 113));
        damageTotal.setText(engine.lastCorrect() ? "-" + engine.lastDamage() + " HP" : "피해 없음");
        feedback.setText(feedbackText(asking));
        feedback.setToolTipText(feedback.getText());
        feedback.setCaretPosition(0);
        repaint();
    }

    private void refreshChoices(boolean asking) {
        answerGroup.clearSelection();
        String[] options = engine.current().options();
        for (int i = 0; i < choices.length; i++) {
            JToggleButton choice = choices[i];
            choice.setText((i + 1) + "   " + options[i]);
            choice.setToolTipText(options[i]);
            choice.setSelected(engine.selected() == i);
            choice.setEnabled(asking);
            choice.putClientProperty("correct", !asking && engine.current().answerIndex() == i);
            choice.putClientProperty("wrong", !asking && engine.selected() == i && engine.current().answerIndex() != i);
        }
    }

    private String storyText() {
        if (!"교수님".equals(engine.target().category())) {
            return engine.defeated() ? "“좋아! 이 카드를 가지고 다음 여정을 이어가.”" : "“나에 관한 문제를 풀어 도전해 봐!”";
        }
        if (engine.correctCount() == 0) {
            return "“여기는 어디지… 나는 누구였지?”";
        }
        if (engine.defeated()) {
            return "“이제 기억나… 나를 구해줘서 고맙네.”";
        }
        return "“그 이야기… 기억이 조금씩 돌아오고 있어.”";
    }

    private String feedbackText(boolean asking) {
        if (!asking) {
            String verdict = engine.lastCorrect() ? "정답! 공격 적중 · HP -" + engine.lastDamage()
                    : "공격 실패! HP는 줄어들지 않았습니다.";
            String explanation = engine.current().explanation();
            if (explanation == null || explanation.isBlank()) {
                explanation = "정답: " + engine.current().options()[engine.current().answerIndex()];
            }
            return verdict + "  정답: " + (engine.current().answerIndex() + 1) + "번\n" + explanation;
        }
        return "";
    }

    private void animateHit() {
        if (finalAttack.isVisible()) {
            finalAttack.update(System.nanoTime());
            repaint();
            if (!finalAttack.isRunning()) finishBattle();
            return;
        }
        hpBar.setValue(Math.max(engine.remainingHp(), hpBar.getValue() - 2));
        portrait.updateHit(System.nanoTime());
        updateHpLabel();
        repaint();
        if (hpBar.getValue() == engine.remainingHp() && !portrait.isAnimating()) {
            animation.stop();
            queueAdvance();
        }
    }

    private void queueAdvance() {
        if (closed || engine.phase() != BattleEngine.Phase.FEEDBACK) return;
        autoAdvance.setInitialDelay(engine.lastCorrect() ? CORRECT_DELAY_MS : WRONG_DELAY_MS);
        autoAdvance.restart();
    }

    private void stopTimers() {
        animation.stop();
        autoAdvance.stop();
    }

    private void updateHpLabel() {
        hpText.setText("HP " + hpBar.getValue() + " / " + BattleEngine.MAX_HP);
        hpBar.setForeground(hpBar.getValue() > BattleEngine.MAX_HP / 3
                ? new Color(220, 63, 89) : new Color(245, 134, 47));
    }

    @Override
    public void paint(Graphics graphics) {
        Graphics2D shaken = (Graphics2D) graphics.create();
        int offset = finalAttack.isRunning() ? finalAttack.shakeOffset() : portrait.shakeOffset();
        shaken.translate(offset, -offset / 2);
        super.paint(shaken);
        finalAttack.paint(shaken, getWidth(), getHeight());
        shaken.dispose();
    }

    private void addAt(Component component, int x, int y, int width, int height, int fontSize) {
        add(component);
        setComponentZOrder(component, 0);
        bounds.put(component, new Rectangle(x, y, width, height));
        fontSizes.put(component, fontSize);
    }

    @Override
    public void doLayout() {
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        int dx = (int) ((getWidth() - WIDTH * scale) / 2);
        int dy = (int) ((getHeight() - HEIGHT * scale) / 2);
        bounds.forEach((component, rectangle) -> {
            component.setBounds(dx + (int) (rectangle.x * scale), dy + (int) (rectangle.y * scale),
                    (int) (rectangle.width * scale), (int) (rectangle.height * scale));
            int style = component == feedback || component == dialogue ? Font.PLAIN : Font.BOLD;
            component.setFont(FONT.deriveFont(style, Math.max(11f, (float) (fontSizes.get(component) * scale))));
            if (component instanceof Container container) container.doLayout();
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (background != null) graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        paintArena(graphics);
    }

    /** Decorative arena only; no timers or game-state changes are needed here. */
    private void paintArena(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        g.translate((getWidth() - WIDTH * scale) / 2, (getHeight() - HEIGHT * scale) / 2);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        float strength = engine.remainingHp() / (float) BattleEngine.MAX_HP;
        Color aura = new Color(111, 70, 224, (int) (100 * strength));
        g.setPaint(new RadialGradientPaint(640, 350, 290,
                new float[]{0, 0.65f, 1}, new Color[]{aura, new Color(130, 98, 240, (int) (60 * strength)), new Color(130, 98, 240, 0)}));
        g.fillOval(350, 90, 580, 520);
        g.setColor(new Color(23, 45, 94, 65));
        g.fillOval(421, 491, 438, 46);
        g.setStroke(new BasicStroke(3));
        g.setColor(new Color(126, 214, 255, 190));
        g.drawOval(400, 482, 480, 62);
        g.setStroke(new BasicStroke(1.5f));
        g.drawOval(420, 490, 440, 46);
        g.setColor(new Color(226, 240, 255, 210));
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4;
            int x = 640 + (int) (255 * Math.cos(angle));
            int y = 338 + (int) (155 * Math.sin(angle));
            g.fillPolygon(new int[]{x, x + 5, x, x - 5}, new int[]{y - 9, y, y + 9, y}, 4);
        }
        paintCategoryEmblem(g);
        paintEnergyFragments(g, strength);
        g.dispose();
    }

    private void paintCategoryEmblem(Graphics2D source) {
        Graphics2D g = (Graphics2D) source.create();
        g.translate(230, 340);
        g.setPaint(new RadialGradientPaint(0, 0, 106,
                new float[]{0, 0.7f, 1}, new Color[]{new Color(245, 252, 255, 230),
                new Color(200, 232, 255, 150), new Color(200, 232, 255, 0)}));
        g.fillOval(-106, -106, 212, 212);
        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(3));
        g.drawOval(-76, -76, 152, 152);
        g.setColor(new Color(56, 119, 190, 180));
        g.setStroke(new BasicStroke(2));
        g.drawArc(-88, -88, 176, 176, 20, 115);
        g.drawArc(-88, -88, 176, 176, 200, 115);
        g.setColor(new Color(31, 79, 141));
        g.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (engine.target().category()) {
            case "과학자" -> {
                for (int i = 0; i < 3; i++) {
                    Graphics2D orbit = (Graphics2D) g.create();
                    orbit.rotate(i * Math.PI / 3);
                    orbit.drawOval(-45, -17, 90, 34);
                    orbit.dispose();
                }
                g.fillOval(-7, -7, 14, 14);
            }
            case "아이돌" -> {
                java.awt.geom.Path2D star = new java.awt.geom.Path2D.Double();
                for (int i = 0; i < 10; i++) {
                    double angle = -Math.PI / 2 + i * Math.PI / 5;
                    int radius = i % 2 == 0 ? 43 : 20;
                    double x = Math.cos(angle) * radius, y = Math.sin(angle) * radius;
                    if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
                }
                star.closePath();
                g.draw(star);
            }
            case "한국사" -> {
                g.drawPolyline(new int[]{-48, 0, 48}, new int[]{-16, -42, -16}, 3);
                g.drawLine(-40, -10, 40, -10);
                g.drawLine(-50, 39, 50, 39);
                for (int x : new int[]{-28, 0, 28}) g.drawLine(x, -8, x, 36);
            }
            default -> {
                g.drawPolyline(new int[]{-44, -44, -15, 0, 15, 44, 44, 15, 0, -15, -44},
                        new int[]{-31, 30, 30, 39, 30, 30, -31, -31, -22, -31, -31}, 11);
                g.drawLine(0, -22, 0, 39);
                g.drawLine(-33, -11, -14, -11);
                g.drawLine(14, -11, 33, -11);
            }
        }
        g.dispose();
    }

    private void paintEnergyFragments(Graphics2D g, float strength) {
        // Atmospheric shards fill the arena without suggesting unimplemented abilities.
        int[][] fragments = {{364, 219, 14}, {386, 405, 19}, {859, 202, 16},
                {902, 453, 22}, {1090, 224, 12}, {1140, 425, 11}};
        for (int[] fragment : fragments) {
            int x = fragment[0], y = fragment[1], size = fragment[2];
            Polygon shard = new Polygon(new int[]{x, x + size / 2, x, x - size / 2},
                    new int[]{y - size, y, y + size, y}, 4);
            g.setColor(new Color(136, 112, 241, (int) (95 * strength)));
            g.fillPolygon(shard);
            g.setColor(new Color(255, 255, 255, (int) (190 * strength)));
            g.drawPolygon(shard);
        }
    }

    @Override
    public void removeNotify() {
        stopTimers();
        super.removeNotify();
    }

    private void bind(String name, KeyStroke key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(key, name);
        getActionMap().put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }
}
