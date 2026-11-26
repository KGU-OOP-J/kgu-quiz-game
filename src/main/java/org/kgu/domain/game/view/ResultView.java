package org.kgu.domain.game.view;

import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.ui.GradientTokens;
import org.kgu.domain.game.view.component.ResultSection;
import org.kgu.domain.game.view.layout.ResultLayout;
import org.kgu.domain.game.view.component.VictoryHeader;
import org.kgu.domain.game.view.component.VictoryHeader.HeaderEffect;

import org.kgu.domain.collection.view.component.CardArt;
import org.kgu.domain.collection.view.component.CardView;
import org.kgu.domain.game.event.ResultGUIEvent;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.domain.game.feature.GameSession.LevelProgress;
import org.kgu.domain.game.feature.GameSession.RewardSettlement;
import org.kgu.domain.game.view.ResultPresentation.CardReward;
import org.kgu.domain.game.view.ResultPresentation.CardStatus;
import org.kgu.core.model.Person;
import org.kgu.core.progression.PlayerProgress;
import org.kgu.core.window.UiButton;
import org.kgu.core.window.UiStyle;
import org.kgu.core.window.View;
import org.kgu.core.window.ViewId;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class ResultView extends JPanel implements View {
    private static final Color ACCENT = new Color(0x95D1FA);
    private static final Color HEADER_TEXT = new Color(0xEFF7FF);
    private static final int XP_FILL_MS = 700;
    private static final int XP_FULL_HOLD_MS = 200;
    private static final int XP_LEVEL_UP_MS = 2000;
    private static final int XP_FADE_MS = 500;
    private static final int COIN_FADE_MS = 2000;

    private final ResultGUIEvent event;
    private final CardArt art;
    private final Image background = new ImageIcon(ResultView.class.getResource("/org/kgu/result/result_background.png")).getImage();
    private final JLabel title = new JLabel("게임 결과", SwingConstants.CENTER);
    private final JLabel verdict = new JLabel("", SwingConstants.CENTER);
    private final JLabel score = new JLabel("", SwingConstants.CENTER);
    private final JPanel hp = new JPanel(new BorderLayout());
    private final RewardPanel rewards = new RewardPanel();
    private final JLabel emptyRewardMessage = new JLabel("", SwingConstants.CENTER);
    private final JLabel level = new JLabel("Lv. —");
    private final LevelUpLabel levelUp = new LevelUpLabel();
    private final JLabel earnedXp = new JLabel("— XP", SwingConstants.CENTER);
    private final JLabel earnedCoins = new JLabel("— 코인", SwingConstants.CENTER);
    private final CoinContent coinContent = new CoinContent();
    private final Timer coinTimer = new Timer(16, e -> animateCoins(System.nanoTime()));
    private final Set<String> animatedCoinSessions = new HashSet<>();
    private String coinFadeSessionId;
    private long coinFadeStartedAt;
    private final JLabel xpProgress = new JLabel("XP 진행도 미확정", SwingConstants.RIGHT);
    private final XpTrack xpTrack = new XpTrack();
    private final Timer xpTimer = new Timer(16, e -> animateXp(System.nanoTime()));
    private final Set<String> animatedXpSessions = new HashSet<>();
    private XpAnimation xpAnimation;
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JButton retry = new UiButton("보상 저장 재시도", new Color(239, 151, 43));
    private final VictoryHeader victoryHeader = new VictoryHeader(title, verdict, score);
    private final JPanel rewardPanels = new JPanel(new GridLayout(1, 2, 24, 0));
    private final JPanel actions = new JPanel(new GridLayout(1, 3, 10, 0));
    private final ResultSection cardSection = new ResultSection("획득한 보상 카드");
    private final ResultSection xpSection = new ResultSection("경험치");
    private final ResultSection coinSection = new ResultSection("획득 코인");
    private boolean showingBattle = true;

    public ResultView(ResultGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(null);
        setBackground(new Color(199, 229, 255));

        summarySection();

        cardSection();

        xpSection();

        coinSection();

        rewardContainer();

        actionSection();
    }


    private void summarySection() {
        title.setForeground(new Color(232, 239, 250));
        score.setForeground(Color.WHITE);
        verdict.setForeground(HEADER_TEXT);
        hp.setOpaque(false);
        add(victoryHeader);
        add(hp);
    }

    private void cardSection() {
        rewardPanels.setOpaque(false);
        emptyRewardMessage.setForeground(ComponentPainter.TEXT);
        JPanel cards = new JPanel(new BorderLayout(0, 5));
        cards.setOpaque(false);
        cards.add(rewards, BorderLayout.CENTER);
        cards.add(emptyRewardMessage, BorderLayout.SOUTH);
        cardSection.add(cards, BorderLayout.CENTER);
    }

    private void xpSection() {
        level.setForeground(ComponentPainter.TEXT);
        xpProgress.setForeground(ComponentPainter.MUTED);
        JPanel levelGroup = new JPanel(new BorderLayout());
        levelGroup.setOpaque(false);
        levelGroup.add(level, BorderLayout.WEST);
        levelUp.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        levelGroup.add(levelUp, BorderLayout.CENTER);
        JPanel xpHeading = new JPanel(new BorderLayout(4, 0));
        xpHeading.setOpaque(false);
        xpHeading.add(levelGroup, BorderLayout.CENTER);
        xpHeading.add(xpProgress, BorderLayout.EAST);
        styleAmount(earnedXp, 12);
        JPanel xpFooter = new JPanel(new BorderLayout());
        xpFooter.setOpaque(false);
        xpFooter.add(earnedXp, BorderLayout.EAST);
        JPanel xp = new JPanel(new BorderLayout(0, 4));
        xp.setOpaque(false);
        xp.add(xpHeading, BorderLayout.NORTH);
        xp.add(xpTrack, BorderLayout.CENTER);
        xp.add(xpFooter, BorderLayout.SOUTH);
        xpSection.add(xp, BorderLayout.CENTER);
    }

    private void coinSection() {
        earnedCoins.setForeground(new Color(0x275898));
        earnedCoins.setBorder(BorderFactory.createEmptyBorder(4, 33, 4, 33));
        JPanel coinFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        coinFooter.setOpaque(false);
        coinFooter.add(earnedCoins);
        coinContent.add(new CoinArt(), BorderLayout.CENTER);
        coinContent.add(coinFooter, BorderLayout.SOUTH);
        coinSection.add(coinContent, BorderLayout.CENTER);
    }

    private void rewardContainer() {
        JPanel progression = new JPanel(new GridLayout(2, 1, 12, 12));
        progression.setOpaque(false);
        progression.add(xpSection);
        progression.add(coinSection);
        rewardPanels.add(cardSection);
        rewardPanels.add(progression);
        add(rewardPanels);
    }

    private void actionSection() {
        add(status);
        actions.setOpaque(false);
        actions.add(action("메인으로", new Color(14, 134, 222), event::returnToMenu));
        actions.add(action("도감 확인", new Color(15, 165, 102), event::openCollection));
        actions.add(action("다음 도전 · 상점", new Color(137, 78, 191), event::openAdventure));
        add(actions);
        retry.setForeground(Color.WHITE);
        retry.addActionListener(e -> event.refresh(this));
        retry.setVisible(false);
        add(retry);
    }

    private JButton action(String text, Color color, Runnable callback) {
        JButton button = new UiButton(text, color);
        button.setForeground(Color.WHITE);
        button.addActionListener(e -> callback.run());
        return button;
    }

    private static void styleAmount(JLabel label, int padding) {
        label.setForeground(ComponentPainter.TEXT);
        label.setOpaque(true);
        label.setBackground(new Color(0xEFF7FF));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1, true),
                BorderFactory.createEmptyBorder(3, padding, 3, padding)));
    }

    @Override
    public void doLayout() {
        if (getWidth() == 0 || getHeight() == 0) return;
        var bounds = ResultLayout.measure(getWidth(), getHeight(), hp.getComponentCount() != 0, retry.isVisible());
        float scale = bounds.scale();

        setTextSize(title, 28, scale);
        setTextSize(verdict, showingBattle ? 50 : 37, scale);
        setTextSize(score, 21, scale);
        setTextSize(level, 36, scale);
        setTextSize(levelUp, 14, scale);
        setTextSize(earnedXp, 18, scale);
        setTextSize(earnedCoins, 27, scale);
        setTextSize(xpProgress, 19, scale);
        setTextSize(status, 17, scale);
        setTextSize(emptyRewardMessage, 16, scale);
        setTextSize(retry, 19, scale);
        for (Component button : actions.getComponents()) setTextSize(button, 24, scale);
        for (ResultSection panel : List.of(cardSection, xpSection, coinSection)) panel.resizeTitle(scale);

        victoryHeader.setBounds(bounds.header());
        hp.setBounds(bounds.hp());
        rewardPanels.setBounds(bounds.rewards());
        status.setBounds(bounds.status());
        retry.setBounds(bounds.retry());
        actions.setBounds(bounds.actions());
    }

    private static void setTextSize(Component component, int size, float scale) {
        Font font = UiStyle.FONT.deriveFont(Font.BOLD, Math.max(9f, Math.round(size * scale)));
        if (!font.equals(component.getFont())) component.setFont(font);
    }

    public void display(GameSession session, List<Person> savedRewards, String message, boolean failed) {
        display(ResultGUIEvent.presentation(session, savedRewards, message, failed));
    }

    public void display(ResultPresentation result) {
        hp.removeAll();
        showingBattle = result.battle().isPresent();
        var battle = result.battle().orElse(null);
        if (battle != null) {
            boolean victory = battle.victory();
            title.setText(battle.professor() && victory ? "이은정 교수님 구출 완료" : "최종 배틀 결과");
            verdict.setText(victory ? "승리" : "패배");
            verdict.setForeground(victory ? new Color(0xEFF7FF) : new Color(255, 158, 168));
            hp.add(hpPanel("상대", battle.opponentHp(), battle.maxOpponentHp()));
            score.setText("최초 정답 " + result.correctCount() + " / " + result.questionCount()
                    + " · 풀이 " + result.answeredCount() + "문제"
                    + (battle.retryCorrectCount() > 0 ? " · 재도전 성공 +1" : "")
                    + " · 최고 콤보 " + battle.bestCombo());
        } else {
            title.setText(result.modeLabel() + " 결과");
            verdict.setText(result.questionCount() + "문제를 모두 풀었습니다!");
            score.setText("맞힌 문제: " + result.correctCount() + " / " + result.questionCount());
            verdict.setForeground(new Color(170, 223, 255));
        }
        rewards.removeAll();
        for (CardReward card : result.cardRewards()) {
            rewards.add(new RewardCard(card, result.cardStage(), art));
        }
        if (result.cardRewards().isEmpty()) rewards.add(new EmptyRewardCard());
        emptyRewardMessage.setText(result.saveFailed() ? "저장된 보상이 없습니다." : "획득한 보상이 없습니다.");
        emptyRewardMessage.setVisible(result.cardRewards().isEmpty());
        showSettlement(result);
        showCoins(result);
        status.setText("<html><div style='text-align:center'>" + result.statusMessage() + "</div></html>");
        status.setForeground(result.saveFailed() ? new Color(180, 30, 30) : new Color(20, 35, 60));
        retry.setVisible(result.saveFailed());
        victoryHeader.showEffect(result.sessionId(), battle == null ? HeaderEffect.NONE
                : battle.victory() ? HeaderEffect.VICTORY : HeaderEffect.DEFEAT);
        revalidate();
        repaint();
    }
    
    private void showSettlement(ResultPresentation result) {
        var reward = result.settlement().orElse(null);
        if (xpAnimation != null) {
            if (reward != null && !result.saveFailed() && xpAnimation.sessionId.equals(result.sessionId())) return;
            finishXpAnimation();
        }
        if (reward == null) {
            level.setText("Lv. —");
            earnedXp.setText("— XP");
            xpProgress.setText("XP 진행도 미확정");
            xpTrack.update(0, 0);
            return;
        }
        if (result.saveFailed() || reward.xpEarned() == 0 || !animatedXpSessions.add(result.sessionId())) {
            showFinalXp(reward);
            return;
        }
        long now = System.nanoTime();
        xpAnimation = new XpAnimation(result.sessionId(), reward, now);
        animateXp(now);
        xpTimer.start();
    }

    private void showCoins(ResultPresentation result) {
        var reward = result.settlement().orElse(null);
        if (reward == null) {
            finishCoinFade();
            earnedCoins.setText("— 코인");
            return;
        }
        int coins = reward.coinsEarned();
        earnedCoins.setText("+" + coins + " 코인");
        if (!result.saveFailed() && coins > 0 && result.sessionId().equals(coinFadeSessionId)) return;
        finishCoinFade();
        if (result.saveFailed() || coins <= 0 || !animatedCoinSessions.add(result.sessionId())) return;
        coinFadeSessionId = result.sessionId();
        coinFadeStartedAt = System.nanoTime();
        coinContent.setFadeOpacity(0);
        coinTimer.start();
    }

    private void animateCoins(long now) {
        if (coinFadeSessionId == null) return;
        float progress = Math.clamp((now - coinFadeStartedAt) / (COIN_FADE_MS * 1_000_000f), 0f, 1f);
        if (progress >= 1) {
            finishCoinFade();
            return;
        }
        coinContent.setFadeOpacity(progress * progress * (3 - 2 * progress));
    }

    private void finishCoinFade() {
        coinTimer.stop();
        coinFadeSessionId = null;
        coinContent.setFadeOpacity(1);
    }

    private void animateXp(long now) {
        var animation = xpAnimation;
        if (animation == null) return;
        var step = animation.steps.get(animation.index);
        double elapsed = (now - animation.startedAt) / 1_000_000.0;
        boolean leveledUp = animation.index > 0;
        boolean hasNextLevel = animation.index + 1 < animation.steps.size();
        // Finish a consecutive fill as the current banner fades, with time left to show a full bar.
        int fillMs = leveledUp && hasNextLevel ? XP_LEVEL_UP_MS - XP_FULL_HOLD_MS : XP_FILL_MS;
        double remaining = 1 - Math.clamp(elapsed / fillMs, 0.0, 1.0);
        double fraction = 1 - remaining * remaining * remaining;
        double gained = (step.earnedAfter() - step.earnedBefore()) * fraction;
        double currentXp = Math.min(step.toXp(), step.fromXp() + gained);
        float opacity = leveledUp ? (float) Math.clamp((XP_LEVEL_UP_MS - elapsed) / XP_FADE_MS, 0.0, 1.0) : 0;
        float flash = leveledUp ? (float) Math.max(0, 1 - Math.abs(elapsed - 100) / 100)
                + (float) Math.max(0, 1 - Math.abs(elapsed - 340) / 100) : 0;
        float pop = leveledUp ? (float) Math.clamp(Math.min(elapsed / 100, (240 - elapsed) / 140), 0.0, 1.0) : 0;
        levelUp.update(opacity, flash, pop);
        showXp(step.level(), currentXp, step.requiredXp(), step.earnedBefore() + (int) Math.round(gained), flash);
        int stepDuration = leveledUp ? XP_LEVEL_UP_MS : fillMs + (hasNextLevel ? XP_FULL_HOLD_MS : 0);
        if (elapsed < stepDuration) return;
        if (!hasNextLevel) {
            finishXpAnimation();
        } else {
            // Paint the completed gauge before the next timer tick shows the new level.
            animation.index++;
            animation.startedAt = now;
        }
    }

    private void showFinalXp(RewardSettlement reward) {
        LevelProgress after = reward.after();
        levelUp.update(0, 0, 0);
        showXp(after.level(), after.levelXp(), after.nextLevelXp(), reward.xpEarned(), 0);
    }

    private void showXp(int currentLevel, double currentXp, int requiredXp, int gained, float flash) {
        level.setText("Lv." + currentLevel);
        earnedXp.setText("+" + gained + " XP");
        xpProgress.setText(requiredXp == 0 ? "최대 레벨" : Math.round(currentXp) + " / " + requiredXp + " XP");
        xpTrack.update(requiredXp == 0 ? 1 : currentXp / requiredXp, flash);
    }

    private void finishXpAnimation() {
        xpTimer.stop();
        if (xpAnimation == null) return;
        showFinalXp(xpAnimation.reward);
        xpAnimation = null;
    }

    private record XpStep(int level, int fromXp, int toXp, int requiredXp, int earnedBefore, int earnedAfter) { }

    private static final class XpAnimation {
        private final String sessionId;
        private final RewardSettlement reward;
        private final List<XpStep> steps = new ArrayList<>();
        private int index;
        private long startedAt;

        XpAnimation(String sessionId, RewardSettlement reward, long now) {
            this.sessionId = sessionId;
            this.reward = reward;
            startedAt = now;
            LevelProgress before = reward.before();
            LevelProgress after = reward.after();
            int earned = 0;
            for (int level = before.level(); level <= after.level(); level++) {
                boolean last = level == after.level();
                int required = last ? after.nextLevelXp() : level == before.level()
                        ? before.nextLevelXp() : PlayerProgress.experienceForLevel(level);
                int from = level == before.level() ? before.levelXp() : 0;
                int to = last ? after.levelXp() : required;
                int totalEarned = last ? reward.xpEarned() : earned + to - from;
                steps.add(new XpStep(level, from, to, required, earned, totalEarned));
                earned = totalEarned;
            }
        }
    }

    private static final class RewardCard extends JPanel {
        private static final int MAX_BADGE_SPACE = 24;
        private static final Color OWNED_TEXT = ComponentPainter.MUTED;
        private static final Color OWNED_BACKGROUND = new Color(0xF4F9FD);
        private static final Color NEW_BACKGROUND = new Color(0xE1F0FF);
        private final CardView image;
        private final JLabel badge;
        private final boolean newlyAcquired;

        RewardCard(CardReward reward, int stage, CardArt art) {
            super(null);
            setOpaque(false);
            image = new CardView(reward.person(), stage, art);
            newlyAcquired = reward.status() == CardStatus.NEW;
            badge = new JLabel(newlyAcquired ? "NEW!" : "보유 중", SwingConstants.CENTER);
            badge.setForeground(newlyAcquired ? new Color(0x275898) : OWNED_TEXT);
            add(image);
            add(badge);
        }

        static int imageHeight(int slotHeight) {
            return Math.max(slotHeight - MAX_BADGE_SPACE, slotHeight * 5 / 6);
        }

        static int slotHeight(int imageHeight) {
            return imageHeight + Math.min(MAX_BADGE_SPACE, (imageHeight + 4) / 5);
        }

        @Override public void doLayout() {
            int height = imageHeight(getHeight());
            image.setBounds(0, 0, getWidth(), height);
            int space = getHeight() - height;
            int gap = Math.min(3, space / 5);
            int badgeHeight = space - gap;
            float size = Math.max(1f, Math.min(13f, badgeHeight * .72f));
            int padding = Math.min(12, getWidth() / 6);
            int available = Math.max(0, getWidth() - padding);
            Font font = UiStyle.FONT.deriveFont(Font.BOLD, size);
            FontMetrics metrics = badge.getFontMetrics(font);
            while (size > 1f && (metrics.getHeight() > badgeHeight || metrics.stringWidth(badge.getText()) > available)) {
                size = Math.max(1f, size - 1f);
                font = font.deriveFont(size);
                metrics = badge.getFontMetrics(font);
            }
            if (!font.equals(badge.getFont())) badge.setFont(font);
            int badgeWidth = Math.min(getWidth(), metrics.stringWidth(badge.getText()) + padding);
            badge.setBounds((getWidth() - badgeWidth) / 2, height + gap, badgeWidth, badgeHeight);
        }

        @Override protected void paintComponent(Graphics source) {
            if (badge.getWidth() < 2 || badge.getHeight() < 2) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            int arc = Math.min(10, badge.getHeight());
            g.setColor(newlyAcquired ? NEW_BACKGROUND : OWNED_BACKGROUND);
            g.fillRoundRect(badge.getX(), badge.getY(), badge.getWidth() - 1, badge.getHeight() - 1, arc, arc);
            g.setColor(badge.getForeground());
            g.drawRoundRect(badge.getX(), badge.getY(), badge.getWidth() - 1, badge.getHeight() - 1, arc, arc);
            g.dispose();
        }
    }

    private static final class RewardPanel extends JPanel {
        private static final double CARD_ASPECT = 170.0 / 250;

        RewardPanel() {
            super(null);
            setOpaque(false);
        }

        @Override
        public void doLayout() {
            int count = getComponentCount();
            if (count == 0) return;
            boolean hasBadges = getComponent(0) instanceof RewardCard;
            int gap = Math.min(12, Math.min(getWidth(), getHeight()) / 24);
            int bestColumns = 1;
            int cardHeight = 0;
            for (int columns = 1; columns <= count; columns++) {
                int rows = (count + columns - 1) / columns;
                int slotHeight = (getHeight() - (rows + 1) * gap) / rows;
                int width = (getWidth() - (columns + 1) * gap) / columns;
                int height = hasBadges ? RewardCard.imageHeight(slotHeight) : slotHeight;
                height = Math.min(height, (int) (width / CARD_ASPECT));
                if (height > cardHeight) {
                    cardHeight = height;
                    bestColumns = columns;
                }
            }
            int rows = (count + bestColumns - 1) / bestColumns;
            int cardWidth = (int) (cardHeight * CARD_ASPECT);
            int slotHeight = hasBadges ? RewardCard.slotHeight(cardHeight) : cardHeight;
            int y = (getHeight() - rows * slotHeight - (rows - 1) * gap) / 2;
            for (int i = 0; i < count; i += bestColumns) {
                int inRow = Math.min(bestColumns, count - i);
                int x = (getWidth() - inRow * cardWidth - (inRow - 1) * gap) / 2;
                for (int column = 0; column < inRow; column++) {
                    getComponent(i + column).setBounds(x + column * (cardWidth + gap), y, cardWidth, slotHeight);
                }
                y += slotHeight + gap;
            }
        }

        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            if (getComponentCount() != 1 || !(getComponent(0) instanceof RewardCard)) return;
            int w = getWidth(), h = getHeight();
            if (w < 1 || h < 1) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            float centerX = w / 2f, centerY = h / 2f;
            g.setPaint(new RadialGradientPaint(centerX, centerY, Math.max(w, h) * .55f,
                    new float[]{0, .55f, 1},
                    new Color[]{new Color(255, 202, 91, 140), new Color(218, 164, 72, 54), new Color(218, 164, 72, 0)}));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(249, 210, 120, 130));
            for (int i = 0; i < 10; i++) {
                double angle = i * Math.PI / 5;
                g.draw(new Line2D.Double(centerX, centerY, centerX + Math.cos(angle) * w * .48,
                        centerY + Math.sin(angle) * h * .43));
            }
            g.dispose();
        }
    }

    private static final class LevelUpLabel extends JLabel {
        private static final float MAX_POP_SCALE = 1.18f;
        private float opacity;
        private float pop;

        LevelUpLabel() {
            super("LEVEL UP! ↑");
        }

        void update(float opacity, float flash, float pop) {
            this.opacity = opacity;
            this.pop = pop;
            setForeground(new Color(255, 216 + Math.round(39 * flash), 140 + Math.round(90 * flash)));
            repaint();
        }

        @Override public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont().deriveFont(getFont().getSize2D() * MAX_POP_SCALE));
            Insets insets = getInsets();
            return new Dimension(metrics.stringWidth(getText()) + insets.left + insets.right + 2,
                    metrics.getHeight() + insets.top + insets.bottom);
        }

        @Override protected void paintComponent(Graphics source) {
            Insets insets = getInsets();
            int horizontalPadding = insets.left + insets.right + 2;
            int available = getWidth() - horizontalPadding;
            if (opacity == 0 || available <= 0) return;
            // Keep space for the peak size so the pop never changes the surrounding layout.
            float fit = Math.min(1f, (float) available / (getPreferredSize().width - horizontalPadding));
            float scale = fit * (1 + (MAX_POP_SCALE - 1) * pop);
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            g.setComposite(AlphaComposite.SrcOver.derive(opacity));
            g.setColor(getForeground());
            g.setFont(getFont().deriveFont(getFont().getSize2D() * scale));
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(getText(), insets.left, (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g.dispose();
        }
    }

    private static final class XpTrack extends JComponent {
        private double progress;
        private float flash;

        void update(double progress, float flash) {
            this.progress = Math.clamp(progress, 0.0, 1.0);
            this.flash = flash;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics source) {
            if (getWidth() < 4 || getHeight() < 4) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            int height = Math.min(24, getHeight() - 2);
            int y = (getHeight() - height) / 2;
            g.translate(1, y);
            ComponentPainter.progress(g, getWidth() - 2, height, progress, GradientTokens.PROGRESS);
            if (flash > 0) {
                g.setColor(new Color(149, 209, 250, Math.round(160 * flash)));
                g.fillRoundRect(0, 0, getWidth() - 2, height, height, height);
            }
            g.dispose();
        }
    }

    private static final class CoinContent extends JPanel {
        private float fadeOpacity = 1;

        CoinContent() {
            super(new BorderLayout(0, 3));
            setOpaque(false);
        }

        void setFadeOpacity(float opacity) {
            if (fadeOpacity == opacity) return;
            fadeOpacity = opacity;
            repaint();
        }

        @Override protected boolean isPaintingOrigin() { return true; }

        @Override protected void paintChildren(Graphics source) {
            if (fadeOpacity == 0) return;
            if (fadeOpacity == 1) {
                super.paintChildren(source);
                return;
            }
            Graphics2D g = (Graphics2D) source.create();
            g.setComposite(AlphaComposite.SrcOver.derive(fadeOpacity));
            super.paintChildren(g);
            g.dispose();
        }
    }

    private static final class CoinArt extends JComponent {
        @Override
        protected void paintComponent(Graphics source) {
            if (getWidth() < 1 || getHeight() < 1) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            double scale = Math.min(getWidth() / 300.0, getHeight() / 110.0);
            g.translate((getWidth() - 300 * scale) / 2, (getHeight() - 110 * scale) / 2);
            g.scale(scale, scale);
            g.setPaint(new RadialGradientPaint(150, 75, 132, new float[]{0, 1},
                    new Color[]{new Color(241, 173, 45, 120), new Color(241, 173, 45, 0)}));
            g.fillRect(0, 0, 300, 110);
            drawStack(g, 210, 92, 3);
            drawStack(g, 115, 91, 7);
            drawStack(g, 73, 99, 4);
            g.setPaint(new GradientPaint(0, 24, new Color(255, 235, 132), 0, 104, new Color(217, 137, 25)));
            g.fillOval(138, 25, 78, 78);
            g.setColor(new Color(255, 225, 116));
            g.setStroke(new BasicStroke(3f));
            g.drawOval(138, 25, 78, 78);
            g.setColor(new Color(152, 88, 25));
            g.drawOval(145, 32, 64, 64);
            Path2D star = new Path2D.Double();
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                int radius = i % 2 == 0 ? 24 : 11;
                double x = 177 + Math.cos(angle) * radius;
                double y = 64 + Math.sin(angle) * radius;
                if (i == 0) star.moveTo(x, y); else star.lineTo(x, y);
            }
            star.closePath();
            g.setColor(new Color(255, 237, 152));
            g.fill(star);
            g.setColor(new Color(176, 107, 28));
            g.setStroke(new BasicStroke(1.5f));
            g.draw(star);
            g.dispose();
        }

        private void drawStack(Graphics2D g, int x, int bottom, int count) {
            for (int i = 0; i < count; i++) {
                int y = bottom - 8 * i;
                g.setPaint(new GradientPaint(x - 29, y, new Color(135, 74, 19), x + 29, y, new Color(251, 183, 38)));
                g.fillRoundRect(x - 29, y - 6, 58, 14, 7, 7);
                g.setColor(new Color(255, 202, 74));
                g.drawRoundRect(x - 29, y - 6, 58, 14, 7, 7);
            }
            int top = bottom - 8 * (count - 1);
            g.setColor(new Color(255, 224, 100));
            g.fillOval(x - 29, top - 10, 58, 13);
            g.setColor(new Color(173, 105, 23));
            g.drawOval(x - 24, top - 8, 48, 9);
        }
    }

    private static final class EmptyRewardCard extends JPanel {
        EmptyRewardCard() {
            setOpaque(false);
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
        finishXpAnimation();
        finishCoinFade();
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
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.weighty = 1;
        wrapper.add(new GlossyHpBar(name, value, maximum), constraints);
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
        org.kgu.core.window.GameTheme.asset("scene-result").ifPresentOrElse(image -> org.kgu.core.window.GameTheme.cover((Graphics2D)g, image, 0, 0, getWidth(), getHeight()), () -> g.drawImage(background, 0, 0, getWidth(), getHeight(), this));
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
            ComponentPainter.surface(g, 4, 4, w - 8, h - 8, 12, GradientTokens.PANEL, ComponentPainter.BORDER);
            int bx = 28, by = 54, bw = w - 56, bh = 27;
            g.translate(bx, by);
            double fraction = MAX_HP == 0 ? 0 : displayedHp / MAX_HP;
            ComponentPainter.progress(g, bw, bh, fraction, GradientTokens.HP_ENEMY);
            if (glow > 0.01) {
                g.setColor(new Color(255, 255, 255, (int)(130 * glow)));
                int position = (int)(bw * fraction);
                g.drawLine(position, 5, position, bh - 5);
            }
            g.dispose();
            Graphics2D textG = (Graphics2D) source.create();
            textG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            textG.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f));
            textG.setColor(ComponentPainter.TEXT);
            FontMetrics fm = textG.getFontMetrics();
            int textX = (int) (27 * getWidth() / 359.0);
            int textY = (int) (39 * getHeight() / 115.0);
            String hpLabel = "HP " + Math.round(displayedHp) + " / " + MAX_HP;
            textG.drawString(name, textX, textY);
            textG.drawString(hpLabel, getWidth() - textX - fm.stringWidth(hpLabel), textY);
            textG.dispose();
        }
    }
}
