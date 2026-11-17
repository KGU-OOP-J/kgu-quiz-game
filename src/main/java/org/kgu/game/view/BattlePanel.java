package org.kgu.game.view;

import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.feature.BattleEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Component-based battle view. Game rules remain in BattleEngine; navigation belongs to the caller.
 */
public final class BattlePanel extends JPanel {
    private static final int WIDTH = 1280, HEIGHT = 960;
    private static final Color NAVY = new Color(17, 40, 85);
    private static final Color BLUE = new Color(20, 125, 244);
    private static final Font FONT = new Font("Malgun Gothic", Font.PLAIN, 20);
    private final BattleEngine engine;
    private final BattleGUIEvent event;
    private final Runnable onFinish;
    private final BufferedImage background;
    private final Map<Component, Rectangle> positions = new LinkedHashMap<>();
    private final Map<Component, Integer> fontSizes = new LinkedHashMap<>();
    private final JToggleButton[] choices = new JToggleButton[4];
    private final ButtonGroup answerGroup = new ButtonGroup();
    private final JProgressBar playerHp = hpBar("playerHp");
    private final JProgressBar computerHp = hpBar("computerHp");
    private final JLabel playerHpText = label("HP 60 / 60", NAVY);
    private final JLabel computerHpText = label("HP 60 / 60", NAVY);
    private final JLabel turn = label("", NAVY);
    private final JLabel phase = label("", Color.WHITE);
    private final JLabel difficulty = label("", Color.WHITE);
    private final JTextArea question = textArea(Color.WHITE);
    private final JTextArea feedback = textArea(NAVY);
    private final JButton hint = new ActionButton("힌트 보기", BLUE);
    private final JButton submit = new ActionButton("정답 제출", BLUE);
    private final JButton next = new ActionButton("다음 턴  ▶", BLUE);
    private final Timer animation;
    private boolean finished;
    private boolean disposed;
    private boolean hintVisible;

    /**
     * @param engine   prepared six-turn battle
     * @param onFinish called once after the sixth feedback is acknowledged
     * @param onMenu   called when the user leaves the battle
     */
    public BattlePanel(BattleEngine engine, BattleGUIEvent event, Runnable onFinish, Runnable onMenu) {
        this.engine = engine;
        this.event = event;
        this.onFinish = onFinish;
        background = event.image("battle/background").orElseThrow(() -> new IllegalStateException("배틀 배경이 없습니다."));
        setLayout(null);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setMinimumSize(new Dimension(800, 600));
        setBackground(new Color(199, 229, 255));
        animation = new Timer(20, e -> animateHp());
        animation.setCoalesce(true);

        addAt(new RoundedPanel(new Color(238, 248, 255), new Color(198, 226, 252), 12), 8, 8, 1264, 42);
        addAt(label("WHO’S NEXT? — 인물 카드 퀴즈 배틀", NAVY), 22, 13, 615, 32, 23);
        JButton menu = new ActionButton("메인으로", BLUE);
        menu.setName("menu");
        menu.addActionListener(e -> {
            disposed = true;
            animation.stop();
            onMenu.run();
        });
        addAt(menu, 1114, 12, 145, 34, 18);

        addHpGroup("PLAYER", playerHpText, playerHp, 72);
        addHpGroup("COMPUTER", computerHpText, computerHp, 846);
        JLabel swords = label("", NAVY);
        swords.setIcon(new SwordsIcon());
        addAt(swords, 590, 59, 100, 66, 42);
        addAt(turn, 548, 123, 184, 34, 21);
        addAt(new RoundedPanel(new Color(255, 176, 59), new Color(239, 111, 8), 38), 550, 161, 180, 47);
        addAt(phase, 550, 161, 180, 47, 25);

        addAt(new CardPanel(engine.player().getName(), event.portrait(engine.player()), BLUE), 258, 208, 274, 318);
        addAt(new CardPanel(engine.computer().getName(), event.portrait(engine.computer()), new Color(248, 149, 29)), 748, 208, 274, 318);
        JLabel versus = label("VS", new Color(177, 99, 8));
        addAt(versus, 546, 325, 188, 112, 81);
        feedback.setName("feedback");
        addAt(feedback, 110, 535, 1060, 45, 16);

        addAt(new RoundedPanel(new Color(255, 255, 255, 245), new Color(229, 240, 254, 245), 28), 74, 587, 1132, 250);
        addAt(label("인물 퀴즈", NAVY), 99, 596, 230, 42, 28);
        addAt(new RoundedPanel(new Color(69, 166, 255), new Color(21, 110, 238), 28), 995, 598, 183, 37);
        addAt(difficulty, 995, 598, 183, 37, 18);
        addAt(new RoundedPanel(new Color(48, 103, 180), new Color(24, 68, 137), 20), 98, 642, 1084, 62);
        question.setName("question");
        addAt(question, 118, 649, 1044, 51, 23);
        for (int i = 0; i < choices.length; i++) {
            final int index = i;
            JToggleButton choice = new ChoiceButton();
            choice.setName("choice" + i);
            choice.setHorizontalAlignment(SwingConstants.LEFT);
            choice.setMargin(new Insets(3, 18, 3, 12));
            choice.addActionListener(e -> choose(index));
            choices[i] = choice;
            answerGroup.add(choice);
            addAt(choice, i % 2 == 0 ? 116 : 654, i < 2 ? 714 : 777, 510, 51, 20);
            bind("choice" + i, KeyStroke.getKeyStroke((char) ('1' + i)), () -> choose(index));
        }
        hint.setText("힌트 보기");
        hint.setName("hint");
        submit.setName("submit");
        next.setName("next");
        hint.addActionListener(e -> showHint());
        submit.addActionListener(e -> submit());
        next.addActionListener(e -> advance());
        addAt(hint, 92, 861, 310, 72, 28);
        addAt(submit, 437, 861, 406, 72, 30);
        addAt(next, 878, 861, 310, 72, 27);
        bind("confirm", KeyStroke.getKeyStroke("ENTER"), () -> {
            if (engine.phase() == BattleEngine.Phase.QUESTION) submit();
            else if (engine.phase() == BattleEngine.Phase.FEEDBACK) advance();
        });
        refresh();
    }

    private static int step(int value, int target) {
        return value + Integer.signum(target - value) * Math.min(1, Math.abs(target - value));
    }

    private static JLabel label(String text, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(color);
        return label;
    }

    private static JTextArea textArea(Color color) {
        JTextArea text = new JTextArea();
        text.setEditable(false);
        text.setOpaque(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setForeground(color);
        return text;
    }

    private static JProgressBar hpBar(String name) {
        JProgressBar bar = new JProgressBar(0, 60);
        bar.setValue(60);
        bar.setName(name);
        bar.setForeground(new Color(17, 211, 89));
        bar.setBackground(new Color(171, 201, 211));
        bar.setBorder(BorderFactory.createLineBorder(new Color(65, 170, 103)));
        return bar;
    }

    private static void rounded(Graphics graphics, JComponent component, Color top, Color bottom, int arc) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, top, 0, component.getHeight(), bottom));
        g.fillRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, arc, arc);
        g.setColor(new Color(255, 255, 255, 230));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(1, 1, component.getWidth() - 3, component.getHeight() - 3, arc, arc);
        g.dispose();
    }

    private static void buttonText(Graphics graphics, AbstractButton button, boolean centered) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(button.getFont());
        g.setColor(centered ? (button.isEnabled() ? Color.WHITE : new Color(232, 240, 250)) : NAVY);
        FontMetrics metrics = g.getFontMetrics();
        String text = button.getText();
        int max = Math.max(1, button.getWidth() - 32);
        if (metrics.stringWidth(text) > max) {
            while (!text.isEmpty() && metrics.stringWidth(text + "…") > max)
                text = text.substring(0, text.length() - 1);
            text += "…";
        }
        int x = centered ? (button.getWidth() - metrics.stringWidth(text)) / 2 : 18;
        g.drawString(text, x, (button.getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
        if (button.hasFocus()) {
            g.setColor(new Color(26, 66, 123));
            g.drawRoundRect(5, 5, button.getWidth() - 11, button.getHeight() - 11, 24, 24);
        }
        g.dispose();
    }

    private void choose(int index) {
        if (disposed || finished || engine.phase() != BattleEngine.Phase.QUESTION) return;
        event.select(index);
        refresh();
    }

    private void submit() {
        if (disposed || finished || engine.phase() != BattleEngine.Phase.QUESTION || engine.selected() < 0) return;
        event.submit();
        hintVisible = false;
        refresh();
        // Only the displayed values animate. Authoritative HP changes exactly once in the model.
        if (isShowing()) animation.start();
        else {
            playerHp.setValue(engine.playerHp());
            computerHp.setValue(engine.computerHp());
            updateHpText();
        }
    }

    private void advance() {
        if (disposed || finished || engine.phase() != BattleEngine.Phase.FEEDBACK) return;
        animation.stop();
        playerHp.setValue(engine.playerHp());
        computerHp.setValue(engine.computerHp());
        updateHpText();
        event.advance();
        hintVisible = false;
        if (engine.phase() == BattleEngine.Phase.FINISHED) {
            finished = true;
            submit.setEnabled(false);
            next.setEnabled(false);
            hint.setEnabled(false);
            onFinish.run();
        } else refresh();
    }

    private void showHint() {
        if (disposed || engine.phase() != BattleEngine.Phase.QUESTION) return;
        hintVisible = !hintVisible;
        refresh();
    }

    private void refresh() {
        boolean asking = engine.phase() == BattleEngine.Phase.QUESTION;
        turn.setText("턴 " + (engine.turn() + 1) + " / 6");
        phase.setText(engine.attackTurn() ? "공격 턴" : "방어 턴");
        difficulty.setText("난이도 : " + engine.current().getDifficulty().getLabel());
        question.setText(engine.current().getQuestion());
        question.setCaretPosition(0);
        answerGroup.clearSelection();
        for (int i = 0; i < 4; i++) {
            choices[i].setText((i + 1) + "   " + engine.current().getChoices().get(i));
            choices[i].setToolTipText(engine.current().getChoices().get(i));
            choices[i].setSelected(engine.selected() == i);
            choices[i].setEnabled(asking);
            choices[i].putClientProperty("correct", !asking && engine.current().getAnswer() == i + 1);
            choices[i].putClientProperty("wrong", !asking && engine.selected() == i && engine.current().getAnswer() != i + 1);
        }
        submit.setEnabled(asking && engine.selected() >= 0);
        submit.setText(asking ? "정답 제출" : "제출 완료");
        next.setEnabled(!asking);
        next.setText(engine.turn() == 5 ? "결과 확인  ▶" : "다음 턴  ▶");
        hint.setEnabled(asking);
        hint.setText(hintVisible ? "힌트 닫기" : "힌트 보기");
        if (!asking) {
            var result = engine.feedback();
            String verdict = result.correct()
                ? (result.attackTurn() ? "정답! 상대 HP -20" : "정답! 방어 성공")
                : (result.attackTurn() ? "오답! 공격 실패" : "오답! 내 HP -20");
            feedback.setText(verdict + "  ·  " + engine.explanation());
        } else if (hintVisible) {
            feedback.setText("힌트: " + engine.hint());
        } else feedback.setText("");
        feedback.setToolTipText(feedback.getText());
        feedback.setCaretPosition(0);
        repaint();
    }

    private void animateHp() {
        playerHp.setValue(step(playerHp.getValue(), engine.playerHp()));
        computerHp.setValue(step(computerHp.getValue(), engine.computerHp()));
        updateHpText();
        if (playerHp.getValue() == engine.playerHp() && computerHp.getValue() == engine.computerHp()) animation.stop();
    }

    private void updateHpText() {
        playerHpText.setText("HP " + playerHp.getValue() + " / 60");
        computerHpText.setText("HP " + computerHp.getValue() + " / 60");
    }

    public void stopAnimation() {
        animation.stop();
    }

    @Override
    public void removeNotify() {
        animation.stop();
        super.removeNotify();
    }

    private void addHpGroup(String name, JLabel text, JProgressBar bar, int x) {
        addAt(new RoundedPanel(Color.WHITE, new Color(214, 237, 255), 45), x, 91, 362, 94);
        addAt(new RoundedPanel(new Color(62, 162, 255), new Color(8, 74, 186), 32), x + 55, 65, 252, 42);
        addAt(label(name, Color.WHITE), x + 55, 65, 252, 42, 25);
        addAt(text, x + 25, 112, 312, 32, 27);
        addAt(bar, x + 25, 151, 312, 19);
    }

    private void addAt(Component component, int x, int y, int w, int h) {
        addAt(component, x, y, w, h, 20);
    }

    private void addAt(Component component, int x, int y, int w, int h, int fontSize) {
        add(component);
        setComponentZOrder(component, 0);
        positions.put(component, new Rectangle(x, y, w, h));
        fontSizes.put(component, fontSize);
    }

    @Override
    public void doLayout() {
        double scale = Math.min(getWidth() / (double) WIDTH, getHeight() / (double) HEIGHT);
        int dx = (int) ((getWidth() - WIDTH * scale) / 2), dy = (int) ((getHeight() - HEIGHT * scale) / 2);
        positions.forEach((component, bounds) -> {
            component.setBounds(dx + (int) (bounds.x * scale), dy + (int) (bounds.y * scale),
                (int) (bounds.width * scale), (int) (bounds.height * scale));
            component.setFont(FONT.deriveFont(Font.BOLD, Math.max(11f, (float) (fontSizes.get(component) * scale))));
            if (component instanceof Container container) container.doLayout();
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.drawImage(background, 0, 0, getWidth(), getHeight(), null);
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

    private static class RoundedPanel extends JPanel {
        private final Color top, bottom;
        private final int arc;

        RoundedPanel(Color top, Color bottom, int arc) {
            this.top = top;
            this.bottom = bottom;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            rounded(g, this, top, bottom, arc);
        }
    }

    private static final class SwordsIcon implements Icon {
        public int getIconWidth() {
            return 58;
        }

        public int getIconHeight() {
            return 58;
        }

        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(x + 29, y + 29);
            for (int angle : new int[]{-45, 45}) {
                Graphics2D sword = (Graphics2D) g.create();
                sword.rotate(Math.toRadians(angle));
                Polygon blade = new Polygon(new int[]{-5, 0, 5, 5, -5}, new int[]{-18, -27, -18, 11, 11}, 5);
                sword.setColor(new Color(234, 248, 255));
                sword.fillPolygon(blade);
                sword.setColor(NAVY);
                sword.setStroke(new BasicStroke(2));
                sword.drawPolygon(blade);
                sword.setColor(new Color(247, 179, 31));
                sword.fillRoundRect(-11, 9, 22, 6, 4, 4);
                sword.setColor(NAVY);
                sword.drawRoundRect(-11, 9, 22, 6, 4, 4);
                sword.setColor(new Color(147, 81, 21));
                sword.fillRoundRect(-3, 15, 6, 12, 3, 3);
                sword.dispose();
            }
            g.dispose();
        }
    }

    private static final class ActionButton extends JButton {
        private final Color tint;

        ActionButton(String text, Color tint) {
            super(text);
            this.tint = tint;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color top = isEnabled() ? tint.brighter() : new Color(165, 179, 199);
            rounded(g, this, top, isEnabled() ? tint.darker() : new Color(108, 124, 146), 40);
            buttonText(g, this, true);
        }
    }

    private static final class ChoiceButton extends JToggleButton {
        ChoiceButton() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setForeground(NAVY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Color top = isSelected() ? new Color(193, 237, 255) : new Color(255, 250, 237);
            Color bottom = isSelected() ? new Color(112, 204, 252) : new Color(237, 222, 192);
            if (Boolean.TRUE.equals(getClientProperty("correct"))) {
                top = new Color(210, 255, 220);
                bottom = new Color(125, 226, 153);
            }
            if (Boolean.TRUE.equals(getClientProperty("wrong"))) {
                top = new Color(255, 222, 222);
                bottom = new Color(244, 157, 157);
            }
            rounded(g, this, top, bottom, 23);
            buttonText(g, this, false);
        }
    }

    private static final class CardPanel extends RoundedPanel {
        private final JLabel name;
        private final JLabel portrait;

        CardPanel(String text, BufferedImage image, Color edge) {
            super(edge.brighter(), edge.darker(), 28);
            setLayout(null);
            name = label(text, Color.WHITE);
            name.setOpaque(true);
            name.setBackground(new Color(15, 32, 64));
            portrait = new PortraitLabel(image);
            add(portrait);
            add(name);
        }

        @Override
        public void doLayout() {
            int inset = Math.max(6, getWidth() / 35), height = Math.max(36, getHeight() / 7);
            portrait.setBounds(inset, inset, getWidth() - inset * 2, getHeight() - height - inset * 2);
            name.setBounds(inset, getHeight() - height - inset, getWidth() - inset * 2, height);
            name.setFont(FONT.deriveFont(Font.BOLD, Math.max(14f, getWidth() / 12f)));
        }
    }

    private static final class PortraitLabel extends JLabel {
        private final BufferedImage portrait;

        PortraitLabel(BufferedImage image) {
            portrait = java.util.Objects.requireNonNull(image);
            setForeground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setColor(new Color(16, 37, 75));
            g.fillRect(0, 0, getWidth(), getHeight());
            double scale = Math.min(getWidth() / (double) portrait.getWidth(), getHeight() / (double) portrait.getHeight());
            int w = (int) (portrait.getWidth() * scale), h = (int) (portrait.getHeight() * scale);
            g.drawImage(portrait, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
            g.dispose();
        }
    }
}
