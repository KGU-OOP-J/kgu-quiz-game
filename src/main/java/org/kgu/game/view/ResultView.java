package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.ResultGUIEvent;
import org.kgu.game.feature.BattleEngine;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.List;

public final class ResultView extends JPanel implements View {
    private final ResultGUIEvent event;
    private final CardArt art;
    private final Image background = new ImageIcon(ResultView.class.getResource("/org/kgu/result/result_background.png")).getImage();
    private final JLabel title = new JLabel("게임 결과", SwingConstants.CENTER);
    private final JLabel verdict = new JLabel("", SwingConstants.CENTER);
    private final JLabel score = new JLabel("", SwingConstants.CENTER);
    private final JPanel hp = new JPanel(new GridLayout(1, 2, 30, 0));
    private final JPanel rewards = new RewardPanel();
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JButton retry = new UiButton("보상 저장 재시도", new Color(239, 151, 43));
    private final VictoryHeader victoryHeader = new VictoryHeader(title, verdict, score);
    private GameSession lastEffectSession;

    public ResultView(ResultGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(new BorderLayout(20, 15));
        setBackground(new Color(199, 229, 255));
        setBorder(BorderFactory.createEmptyBorder(25, 50, 25, 50));
        JPanel summary = new JPanel(new BorderLayout(0, 12));
        summary.setOpaque(false);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 25f));
        verdict.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 48f));
        score.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
        title.setForeground(new Color(232, 239, 250));
        score.setForeground(Color.WHITE);
        verdict.setForeground(new Color(255, 222, 154));
        hp.setOpaque(false);
        hp.setPreferredSize(new Dimension(0, 110));
        summary.add(victoryHeader, BorderLayout.NORTH);
        summary.add(hp, BorderLayout.CENTER);
        add(summary, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rewards);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JPanel rewardSection = new JPanel(new BorderLayout());
        rewardSection.setOpaque(false);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        titlePanel.setOpaque(false);
        JLabel rewardTitle = new JLabel("획득한 보상 카드", SwingConstants.CENTER);
        rewardTitle.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
        rewardTitle.setForeground(new Color(245, 225, 178));
        rewardTitle.setOpaque(true);
        rewardTitle.setBackground(new Color(17, 30, 55, 230));
        rewardTitle.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218, 186, 121), 2), BorderFactory.createEmptyBorder(8, 28, 8, 28)));
        titlePanel.add(rewardTitle);
        rewardSection.add(titlePanel, BorderLayout.NORTH);
        rewardSection.add(scroll, BorderLayout.CENTER);
        add(rewardSection, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        status.setFont(UiStyle.FONT.deriveFont(16f));
        bottom.add(status, BorderLayout.NORTH);
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton menu = new UiButton("메인으로", new Color(20, 125, 244));
        JButton collection = new UiButton("도감 확인", new Color(30, 172, 111));
        JButton adventure = new UiButton("다음 도전 · 상점", new Color(132, 86, 196));
        adventure.addActionListener(e -> event.openAdventure());
        for (JButton button : new JButton[]{menu, collection, adventure, retry}) {
            button.setForeground(Color.WHITE);
            button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            button.setPreferredSize(new Dimension(220, 55));
            actions.add(button);
        }
        menu.addActionListener(e -> event.returnToMenu());
        collection.addActionListener(e -> event.openCollection());
        retry.addActionListener(e -> event.refresh(this));
        bottom.add(actions, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    public void display(GameSession session, List<Person> savedRewards, String message, boolean failed) {
        hp.removeAll();
        if (session.getMode() == GameSession.Mode.BATTLE) {
            BattleEngine battle = session.getBattle();
            title.setText(battle.computer().getCategory().equals("교수님") && battle.result() == BattleEngine.Result.WIN ? "이은정 교수님 구출 완료" : "최종 배틀 결과");
            String result = battle.result() == BattleEngine.Result.WIN ? "승리" : battle.result() == BattleEngine.Result.LOSE ? "패배" : "무승부";
            verdict.setText(result);
            if (battle.result() == BattleEngine.Result.WIN) verdict.setForeground(new Color(255, 222, 154));
            else if (battle.result() == BattleEngine.Result.LOSE) verdict.setForeground(new Color(255, 158, 168));
            else {
                verdict.setForeground(new Color(105, 200, 255));
                verdict.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 50f));
            }
            hp.add(hpPanel("플레이어", battle.playerHp(), battle.maxPlayerHp()));
            hp.add(hpPanel("상대", battle.computerHp(), battle.maxComputerHp()));
            score.setText("정답 " + battle.correctCount() + " / " + session.getAnsweredCount() + " · 최고 콤보 " + battle.bestCombo());
        } else {
            title.setText(session.getMode() + " 결과");
            verdict.setText(session.getQuestionCount() + "문제를 모두 풀었습니다!");
            score.setText("맞힌 문제: " + session.getCorrectCount() + " / " + session.getQuestionCount());
            verdict.setForeground(new Color(170, 223, 255));
        }
        rewards.removeAll();
        for (Person person : savedRewards) {
            CardView card = new CardView(person, session.getDifficulty().getStage(), art);
            card.setPreferredSize(new Dimension(170, 250));
            rewards.add(card);
        }
        if (savedRewards.isEmpty()) {
            JPanel emptyPanel = new JPanel(new BorderLayout(0, 12));
            emptyPanel.setOpaque(false);
            JPanel cardArea = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            cardArea.setOpaque(false);
            cardArea.add(new EmptyRewardCard());
            JLabel empty = new JLabel(failed ? "저장된 보상이 없습니다. 재시도해 주세요." : "이번 게임에서 획득한 보상이 없습니다.", SwingConstants.CENTER);
            empty.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
            empty.setForeground(new Color(235, 240, 250));
            emptyPanel.add(cardArea, BorderLayout.CENTER);
            emptyPanel.add(empty, BorderLayout.SOUTH);
            rewards.add(emptyPanel);
        }
        status.setText("<html><div style='text-align:center'>" + message + "</div></html>");
        status.setForeground(failed ? new Color(255, 180, 180) : new Color(235, 245, 255));
        retry.setVisible(failed);
        if (lastEffectSession != session) {
            lastEffectSession = session;
            if (session.getMode() == GameSession.Mode.BATTLE) {
                if (session.getBattle().result() == BattleEngine.Result.WIN) {
                    victoryHeader.play(true);
                } else if (session.getBattle().result() == BattleEngine.Result.LOSE) {
                    victoryHeader.playDefeat();
                } else {
                    victoryHeader.playDraw();
                }
            } else {
                victoryHeader.play(false);
            }
        }
        revalidate();
        repaint();
    }
    
    private static final class RewardPanel extends JPanel {
        RewardPanel() {
            super(new FlowLayout(FlowLayout.CENTER, 20, 12));
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            Rectangle area = null;
            for (Component component : getComponents()) {
                if (!component.isVisible()) continue;
                if (area == null) area = new Rectangle(component.getBounds());
                else area.add(component.getBounds());
            }
            if (area == null) return;
            int x = Math.max(2, area.x - 20);
            int y = Math.max(2, area.y - 15);
            int w = Math.min(getWidth() - x - 2, area.width + 40);
            int h = Math.min(getHeight() - y - 2, area.height + 30);
            if (w <= 0 || h <= 0) return;
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, y, new Color(14, 27, 53, 215), 0, y + h, new Color(28, 42, 69, 190)));
            g.fillRoundRect(x, y, w, h, 22, 22);
            g.setColor(new Color(218, 186, 121, 210));
            g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 22, 22);
            g.dispose();
        }
    }
    private static final class EmptyRewardCard extends JPanel {
        EmptyRewardCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(170, 250));
        }
        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(29, 46, 78), 0, h, new Color(12, 22, 42)));
            g.fillRoundRect(2, 2, w - 4, h - 4, 18, 18);
            g.setColor(new Color(220, 181, 100));
            g.setStroke(new BasicStroke(3f));
            g.drawRoundRect(3, 3, w - 7, h - 7, 18, 18);
            int lockY = h / 2 - 42;
            g.setColor(new Color(231, 240, 255, 180));
            g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc(w / 2 - 23, lockY, 46, 50, 0, 180);
            g.setPaint(new GradientPaint(0, lockY + 23, new Color(210, 221, 235), 0, lockY + 73, new Color(105, 129, 161)));
            g.fillRoundRect(w / 2 - 31, lockY + 29, 62, 53, 10, 10);
            g.setColor(new Color(49, 67, 94));
            g.fillOval(w / 2 - 5, lockY + 46, 10, 10);
            g.fillRoundRect(w / 2 - 2, lockY + 52, 4, 13, 2, 2);
            g.dispose();
        }
    }

    public void stopAnimations() {
        victoryHeader.stop();
        stopHp(hp);
    }
    private void stopHp(Container container) {
        for (Component child : container.getComponents()) {
            if (child instanceof GlossyHpBar bar && bar.timer != null) bar.timer.stop();
            if (child instanceof Container nested) stopHp(nested);
        }
    }
    @Override public void removeNotify() { stopAnimations(); super.removeNotify(); }
    private JPanel hpPanel(String name, int value, int maximum) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(new GlossyHpBar(name, value, maximum));
        return wrapper;
    }
    @Override
    public ViewId getViewId() {
        return ViewId.RESULT;
    }
    @Override
    public JComponent getComponent() {
        return this;
    }
    @Override
    public Dimension getViewSize() {
        return new Dimension(1280, 960);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        org.kgu.window.GameTheme.asset("scene-result").ifPresentOrElse(image -> org.kgu.window.GameTheme.cover((Graphics2D)g, image, 0, 0, getWidth(), getHeight()), () -> g.drawImage(background, 0, 0, getWidth(), getHeight(), this));
    }
    private static final class GlossyHpBar extends JPanel {
        private final int MAX_HP;
        private final String name;
        private final int targetHp;
        private double displayedHp;
        private double glow = 0;
        private Timer timer;
        GlossyHpBar(String name, int targetHp, int maximum) {
            MAX_HP = maximum;
            this.name = name;
            this.targetHp = Math.max(0, Math.min(MAX_HP, targetHp));
            displayedHp = this.targetHp;
            setOpaque(false);
            setPreferredSize(new Dimension(500, 80));
        }
        @Override
        public void addNotify() {
            super.addNotify();
            if (timer != null) timer.stop();
            displayedHp = MAX_HP;
            final long start = System.nanoTime();
            timer = new Timer(16, e -> {
                double progress = Math.min(1.0, (System.nanoTime() - start) / 2_000_000_000.0);
                double eased = 1.0 - Math.pow(1.0 - progress, 3.0);
                displayedHp = MAX_HP + (targetHp - MAX_HP) * eased;
                glow = targetHp < MAX_HP ? Math.min(1.0, (1.0 - progress) * 4.0) : 0.0;
                repaint();
                if (progress >= 1.0) {
                    displayedHp = targetHp;
                    glow = 0;
                    timer.stop();
                    repaint();
                }
            });
            timer.start();
        }
        @Override
        public void removeNotify() {
            if (timer != null) timer.stop();
            super.removeNotify();
        }
        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (getWidth() < 50 || getHeight() < 35) { g.dispose(); return; }
            g.scale(getWidth() / 359.0, getHeight() / 115.0);
            int w = 359, h = 115;
            g.setColor(new Color(6, 12, 30, 95));
            g.fillRoundRect(7, 8, w - 14, h - 12, 20, 20);
            g.setPaint(new GradientPaint(0, 4, new Color(19, 31, 59, 220), 0, h, new Color(24, 30, 54, 219)));
            g.fillRoundRect(4, 4, w - 8, h - 8, 20, 20);
            g.setStroke(new BasicStroke(1.8f));
            g.setColor(new Color(222, 196, 128));
            g.drawRoundRect(5, 5, w - 11, h - 11, 18, 18);
            g.setStroke(new BasicStroke(.8f));
            g.setColor(new Color(254, 230, 177, 95));
            g.drawRoundRect(9, 9, w - 19, h - 19, 15, 15);
            int bx = 28, by = 54, bw = w - 56, bh = 27;
            RoundRectangle2D track = new RoundRectangle2D.Double( bx, by, bw, bh, bh, bh);
            g.setPaint(new GradientPaint(0, by, new Color(5, 10, 23, 252), 0, by + bh, new Color(30, 37, 52, 245)));
            g.fill(track);
            double fillWidth = (bw - 3) * displayedHp / MAX_HP;
            if (fillWidth > 0) {
                Graphics2D fill = (Graphics2D) g.create();
                fill.clip(track);
                RoundRectangle2D gauge = new RoundRectangle2D.Double( bx + 1, by + 2, fillWidth, bh - 4, bh - 5, bh - 5);
                fill.setPaint(new LinearGradientPaint(0, by + 2, 0, by + bh - 2, new float[]{0f, .2f, .52f, .8f, 1f}, new Color[]{new Color(198, 255, 211), new Color(99, 232, 143), new Color(42, 206, 101), new Color(17, 151, 68), new Color(6, 94, 42)}));
                fill.fill(gauge);
                fill.clip(gauge);
                fill.setPaint(new GradientPaint(0, by + 3, new Color(255, 255, 255, 180), 0, by + bh / 2, new Color(255, 255, 255, 0)));
                fill.fill(new RoundRectangle2D.Double( bx + 3, by + 3, Math.max(0, fillWidth - 4), bh * .44, bh, bh));
                fill.setColor(new Color(160, 255, 185, 65));
                fill.dispose();
                if (glow > 0.01) {
                    Graphics2D light = (Graphics2D) g.create();
                    float cx = (float) (bx + fillWidth);
                    float cy = by + bh / 2f;
                    float radius = 43f;
                    light.setComposite(AlphaComposite.getInstance( AlphaComposite.SRC_OVER, (float) glow));
                    light.clip(new RoundRectangle2D.Double( bx - 3, by - 12, bw + 7, bh + 24, 15, 15));
                    light.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), radius, new float[]{0f, .13f, .4f, 1f}, new Color[]{new Color(245, 255, 245, 215), new Color(100, 255, 155, 205), new Color(40, 220, 105, 85), new Color(20, 180, 65, 0)}));
                    light.setColor(new Color(225, 255, 230, 220));
                    light.setStroke(new BasicStroke(2.4f));
                    light.draw(new Line2D.Double(cx - 1, by + 6, cx - 1, by + bh - 6));
                    light.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), 10f, new float[]{0f, 1f}, new Color[]{new Color(245, 255, 245, 240), new Color(175, 255, 200, 0)}));
                    light.fill(new Ellipse2D.Double(cx - 10, cy - 10, 20, 20));
                    light.dispose();
                }
            }
            g.setColor(new Color(239, 213, 156));
            g.setStroke(new BasicStroke(1.7f));
            g.draw(track);
            g.setColor(new Color(255, 255, 255, 75));
            g.setStroke(new BasicStroke(.8f));
            g.draw(new Arc2D.Double(bx + 2, by + 2, bw - 4, bh - 4, 5, 170, Arc2D.OPEN));
            g.dispose();
            Graphics2D textG = (Graphics2D) source.create();
            textG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textG.setFont(new Font("맑은 고딕", Font.BOLD, 19));
            textG.setColor(new Color(247, 250, 255));
            FontMetrics fm = textG.getFontMetrics();
            int textX = (int) (27 * getWidth() / 359.0);
            int textY = (int) (39 * getHeight() / 115.0);
            String hpLabel = "HP " + Math.round(displayedHp) + " / " + MAX_HP;
            textG.drawString(name, textX, textY);
            textG.drawString(hpLabel, getWidth() - textX - fm.stringWidth(hpLabel), textY);
            textG.dispose();
        }
    }
    
    private static final class VictoryHeader extends JPanel {
    	private final JLabel verdictLabel;
    	private boolean defeat;
        private boolean victory;
        private boolean drawResult;
        private double elapsed;
        private Timer timer;
        VictoryHeader(JLabel title, JLabel verdict, JLabel score) {
        	this.verdictLabel = verdict;
            setLayout(new GridLayout(3, 1, 0, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(0, 150));
            add(title); add(verdict); add(score);
        }
        void stop() { if (timer != null) timer.stop(); }
        void play(boolean isVictory) {
        	if (timer != null) timer.stop();
        	victory = isVictory;
        	defeat = false;
        	drawResult = false;
        	verdictLabel.setVisible(true);
        	elapsed = 0;
            repaint();
            if (!victory) return;
            final long started = System.nanoTime();
            timer = new Timer(16, e -> {
                elapsed = (System.nanoTime() - started) / 1_000_000_000.0;
                repaint();
                if (elapsed >= 4) ((Timer)e.getSource()).stop();
            });
            timer.start();
        }
        void playDefeat() {
            if (timer != null) timer.stop();
            victory = false;
            defeat = true;
            drawResult = false;
            elapsed = 0;
            verdictLabel.setVisible(false);
            final long started = System.nanoTime();
            timer = new Timer(16, e -> {
                elapsed = (System.nanoTime() - started) / 1_000_000_000.0;
                repaint();
                if (elapsed >= 4) ((Timer)e.getSource()).stop();
            });
            timer.start();
        }
        void playDraw() {
            if (timer != null) timer.stop();
            victory = false;
            defeat = false;
            drawResult = true;
            elapsed = 0;
            verdictLabel.setVisible(true);
            final long started = System.nanoTime();
            timer = new Timer(16, e -> {
            	elapsed = Math.min(4.0, (System.nanoTime() - started) / 1_000_000_000.0);
            	repaint();
            	if (elapsed >= 4.0) ((Timer) e.getSource()).stop();
            });
            timer.start();
        }
        private void paintDefeat(Graphics2D source, int w, int h) {
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.clip(new RoundRectangle2D.Double(5, 5, w - 10, h - 10, 22, 22));
            double fogFade = Math.min(1.0, Math.max(0.0, (elapsed - 0.2) / 1.2));
            if (fogFade > 0) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) fogFade));
                g.setPaint(new GradientPaint(0, 0, new Color(102, 116, 139, 95), 0, h, new Color(36, 48, 68, 0)));
                g.fillRect(0, 0, w, h);
                float drift = (float) (Math.sin(elapsed * 0.35) * 24);
                g.setPaint(new GradientPaint(0, 0, new Color(175, 190, 207, 38), 0, h * 0.7f, new Color(125, 143, 169, 0)));
                g.fill(new Ellipse2D.Float(w * 0.15f + drift, -h * 0.65f, w * 0.7f, h * 1.2f));
            }
            double rainFade = Math.min(1.0, Math.max(0.0, (elapsed - 1.0) / 0.8));
            if (rainFade > 0) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) rainFade));
                g.setStroke(new BasicStroke(0.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int i = 0; i < 95; i++) {
                    float x = (i * 97 + 23) % Math.max(1, w);
                    float y = (float) ((elapsed * (170 + i % 5 * 20) + i * 37) % (h + 45)) - 25;
                    float length = 9 + i % 5 * 3;
                    g.setColor(i % 4 == 0 ? new Color(201, 224, 245, 115) : new Color(157, 192, 224, 75));
                    g.draw(new Line2D.Float(x, y, x - 3, y + length));
                }
            }
            g.setComposite(AlphaComposite.SrcOver);
            double progress = Math.min(1.0, elapsed / 1.3);
            double eased = 1.0 - Math.pow(1.0 - progress, 3.0);
            float fontSize = (float) (100 - 52 * eased);
            int red = (int) (255 + (185 - 255) * eased);
            int green = (int) (100 + (191 - 100) * eased);
            int blue = (int) (115 + (202 - 115) * eased);
            g.setFont(verdictLabel.getFont().deriveFont(Font.BOLD, fontSize));
            g.setColor(new Color(red, green, blue));
            String text = "패배";
            FontMetrics fm = g.getFontMetrics();
            int textX = (w - fm.stringWidth(text)) / 2;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(text, textX, textY);
            g.dispose();
        }
        private void paintDrawScale(Graphics2D source, int w, int h) {
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.clip(new RoundRectangle2D.Double(5, 5, w - 10, h - 10, 22, 22));
            g.translate(w / 2.0, -20);
            double scale = Math.min(1.0, w / 380.0);
            g.scale(scale, scale);
            double progress = Math.min(1.0, elapsed / 4.0);
            double angle = -0.30 * Math.cos(elapsed * 4.5) * Math.pow(1.0 - progress, 2);
            if (progress >= 1.0) angle = 0;
            g.setPaint(new RadialGradientPaint(new Point2D.Double(0, 76), 135f, new float[]{0f, .55f, 1f}, new Color[]{new Color(142, 218, 255, 95), new Color(98, 167, 225, 35), new Color(75, 145, 210, 0)}));
            g.fill(new Ellipse2D.Double(-135, -59, 270, 270));
            double pivotY = 65;
            double arm = 148;
            double lx = -arm * Math.cos(angle), ly = pivotY - arm * Math.sin(angle);
            double rx = arm * Math.cos(angle), ry = pivotY + arm * Math.sin(angle);
            Graphics2D beam = (Graphics2D) g.create();
            beam.rotate(angle, 0, pivotY);
            beam.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            beam.setColor(new Color(85, 93, 115, 180));
            beam.draw(new Line2D.Double(-151, pivotY + 2, 151, pivotY + 2));
            beam.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            beam.setPaint(new GradientPaint(-110, 0, new Color(244, 225, 170), 110, 0, new Color(134, 162, 189)));
            beam.draw(new Line2D.Double(-148, pivotY, 148, pivotY));
            beam.setColor(new Color(255, 249, 212, 155));
            beam.setStroke(new BasicStroke(0.8f));
            beam.draw(new Line2D.Double(-144, pivotY - 2, 144, pivotY - 2));
            beam.dispose();
            double[] xs = {lx, rx};
            double[] ys = {ly, ry};
            for (int i = 0; i < 2; i++) {
                double x = xs[i], y = ys[i];
                g.setColor(new Color(235, 216, 169));
                g.setStroke(new BasicStroke(1.7f));
                g.draw(new Line2D.Double(x - 28, y + 38, x, y));
                g.draw(new Line2D.Double(x + 28, y + 38, x, y));
                g.setPaint(new GradientPaint(0, (float)(y + 34), new Color(255, 241, 191), 0, (float)(y + 56), new Color(105, 144, 178)));
                Path2D plate = new Path2D.Double();
                plate.moveTo(x - 35, y + 38);
                plate.quadTo(x, y + 64, x + 35, y + 38);
                plate.closePath();
                g.fill(plate);
                g.setColor(new Color(245, 224, 172));
                g.setStroke(new BasicStroke(1.5f));
                g.draw(new Line2D.Double(x - 35, y + 38, x + 35, y + 38));
                g.setColor(new Color(255, 248, 220));
                g.fill(new Ellipse2D.Double(x - 3, y - 3, 6, 6));
            }
            g.setPaint(new GradientPaint(-8, 54, new Color(255, 245, 199), 8, 76, new Color(145, 171, 195)));
            g.fill(new Ellipse2D.Double(-6, pivotY - 6, 12, 12));
            g.setColor(new Color(255, 252, 221));
            g.fill(new Ellipse2D.Double(-2, pivotY - 2, 4, 4));
            g.dispose();
        }        
        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            Graphics2D g = (Graphics2D) source.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            if (w < 10 || h < 10) { g.dispose(); return; }
            g.setPaint(new GradientPaint(0, 0, new Color(14, 27, 54, 222), 0, h, new Color(32, 43, 72, 211)));
            g.fillRoundRect(2, 2, w - 4, h - 4, 24, 24);
            g.setColor(new Color(225, 191, 125)); g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(3, 3, w - 7, h - 7, 24, 24);
            if (victory) {
                float strength = (float) (0.24 + 0.40 * Math.min(1.0, elapsed / 0.65));
                float cx = w / 2f, cy = h * 0.52f, radius = Math.min(w * 0.35f, 225f);
                g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), radius, new float[]{0f, .3f, 1f}, new Color[]{new Color(255, 224, 128, (int)(145 * strength)), new Color(255, 202, 99, (int)(65 * strength)), new Color(255, 198, 80, 0)}));
                g.fill(new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2));
                double cycle = elapsed % 2.3;
                for (int side = 0; side < 2; side++) {
                    double t = (cycle - (side == 0 ? .25 : .85)) / 1.3;
                    if (t < 0 || t > 1) continue;
                    float alpha = (float)(1.0 - t);
                    float originX = (float)(w * (side == 0 ? .23 : .77));
                    float originY = h * .53f;
                    for (int i = 0; i < 24; i++) {
                        double a = 2 * Math.PI * i / 24.0 + side * .12;
                        double speed = 45 + (i % 5) * 11;
                        double distance = 10 + speed * t;
                        float px = originX + (float)(Math.cos(a) * distance);
                        float py = originY + (float)(Math.sin(a) * distance * .70 + 20 * t * t);
                        int transparency = Math.max(0, Math.min(255, (int)(220 * alpha)));
                        g.setColor(i % 4 == 0 ? new Color(195, 231, 255, transparency) : new Color(255, 211, 113, transparency));
                        float dot = 2f + i % 3;
                        g.fill(new Ellipse2D.Float(px - dot / 2, py - dot / 2, dot, dot));
                        if (i % 3 == 0) {
                            g.setStroke(new BasicStroke(1.5f));
                            float tailX = px - (float)(Math.cos(a) * 7 * alpha);
                            float tailY = py - (float)(Math.sin(a) * 5 * alpha);
                            g.draw(new Line2D.Float(tailX, tailY, px, py));
                        }
                    }
                }
            }
            if (defeat) paintDefeat(g, w, h);
            if (drawResult) paintDrawScale(g, w, h);
            g.dispose();
        }
        @Override
        public void removeNotify() {
            if (timer != null) timer.stop();
            super.removeNotify();
        }
    }
}

