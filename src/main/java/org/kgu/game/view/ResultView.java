package org.kgu.game.view;

import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.event.ResultGUIEvent;
import org.kgu.game.feature.GameSession;
import org.kgu.game.feature.GameSession.LevelProgress;
import org.kgu.game.feature.GameSession.RewardSettlement;
import org.kgu.game.view.ResultPresentation.CardReward;
import org.kgu.game.view.ResultPresentation.CardStatus;
import org.kgu.model.Person;
import org.kgu.progression.PlayerProgress;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;


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
    private static final Color GOLD = new Color(246, 216, 140);
    private static final Color CREAM = new Color(255, 234, 176);
    private static final Color TEXT = new Color(235, 245, 255);
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
    private final SectionPanel cardSection = new SectionPanel("획득한 보상 카드");
    private final SectionPanel xpSection = new SectionPanel("경험치");
    private final SectionPanel coinSection = new SectionPanel("획득 코인");
    private boolean showingBattle = true;

    public ResultView(ResultGUIEvent event) {
        this.event = event;
        art = new CardArt(event::image);
        setLayout(null);
        setBackground(new Color(199, 229, 255));

        title.setForeground(new Color(232, 239, 250));
        score.setForeground(Color.WHITE);
        verdict.setForeground(CREAM);
        hp.setOpaque(false);
        add(victoryHeader);
        add(hp);

        rewardPanels.setOpaque(false);
        emptyRewardMessage.setForeground(TEXT);
        JPanel cards = new JPanel(new BorderLayout(0, 5));
        cards.setOpaque(false);
        cards.add(rewards, BorderLayout.CENTER);
        cards.add(emptyRewardMessage, BorderLayout.SOUTH);
        cardSection.add(cards, BorderLayout.CENTER);

        level.setForeground(TEXT);
        xpProgress.setForeground(new Color(201, 218, 219));
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

        earnedCoins.setForeground(GOLD);
        earnedCoins.setBorder(BorderFactory.createEmptyBorder(4, 33, 4, 33));
        JPanel coinFooter = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        coinFooter.setOpaque(false);
        coinFooter.add(earnedCoins);
        coinContent.add(new CoinArt(), BorderLayout.CENTER);
        coinContent.add(coinFooter, BorderLayout.SOUTH);
        coinSection.add(coinContent, BorderLayout.CENTER);

        JPanel progression = new JPanel(new GridLayout(2, 1, 12, 12));
        progression.setOpaque(false);
        progression.add(xpSection);
        progression.add(coinSection);
        rewardPanels.add(cardSection);
        rewardPanels.add(progression);
        add(rewardPanels);

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
        label.setForeground(CREAM);
        label.setOpaque(true);
        label.setBackground(new Color(18, 30, 55));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GOLD, 1, true),
                BorderFactory.createEmptyBorder(3, padding, 3, padding)));
    }

    @Override
    public void doLayout() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;
        float scale = Math.min(1.18f, Math.min(width / 1280f, height / 960f));
        int margin = Math.round(20 * scale);
        int gap = Math.round(12 * scale);
        int headerHeight = Math.round(168 * scale);
        int hpHeight = hp.getComponentCount() == 0 ? 0 : Math.round(90 * scale);
        int statusHeight = Math.round(48 * scale);
        int buttonHeight = Math.round(62 * scale);
        int retryHeight = retry.isVisible() ? Math.round(43 * scale) : 0;
        int innerWidth = Math.min(width - 2 * margin, Math.round(930 * scale));
        int buttonWidth = Math.min(innerWidth, Math.round(810 * scale));

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
        for (SectionPanel panel : List.of(cardSection, xpSection, coinSection)) panel.resizeTitle(scale);

        int headerWidth = Math.min(width - 2 * margin, Math.round(1140 * scale));
        victoryHeader.setBounds((width - headerWidth) / 2, margin, headerWidth, headerHeight);
        int top = margin + headerHeight + gap;
        int hpWidth = Math.min(innerWidth, Math.round(550 * scale));
        hp.setBounds((width - hpWidth) / 2, top, hpWidth, hpHeight);
        if (hpHeight > 0) top += hpHeight + gap;

        int buttonsTop = height - margin - buttonHeight;
        int retryTop = buttonsTop - (retryHeight > 0 ? retryHeight + gap : 0);
        int statusTop = retryTop - statusHeight;
        int bodyHeight = Math.min(Math.round(440 * scale), Math.max(0, statusTop - gap - top));
        int bodyTop = top + Math.max(0, statusTop - gap - top - bodyHeight) / 2;
        rewardPanels.setBounds((width - innerWidth) / 2, bodyTop, innerWidth, bodyHeight);
        status.setBounds((width - headerWidth) / 2, statusTop, headerWidth, statusHeight);
        retry.setBounds((width - buttonWidth) / 2, retryTop, buttonWidth, retryHeight);
        actions.setBounds((width - buttonWidth) / 2, buttonsTop, buttonWidth, buttonHeight);
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
            verdict.setForeground(victory ? new Color(255, 222, 154) : new Color(255, 158, 168));
            hp.add(hpPanel("상대", battle.opponentHp(), battle.maxOpponentHp()));
            score.setText("정답 " + result.correctCount() + " / " + result.answeredCount() + " · 최고 콤보 " + battle.bestCombo());
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

    private static final class SectionPanel extends JPanel {
        private final JLabel heading;

        SectionPanel(String title) {
            super(new BorderLayout(0, 6));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 14, 12, 14));
            heading = new JLabel(title, SwingConstants.CENTER);
            heading.setForeground(CREAM);
            add(heading, BorderLayout.NORTH);
        }

        void resizeTitle(float scale) {
            setTextSize(heading, 25, scale);
        }

        @Override
        protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            if (getWidth() < 10 || getHeight() < 10) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            int w = getWidth(), h = getHeight();
            g.setPaint(new GradientPaint(0, 0, new Color(9, 22, 51, 246), 0, h, new Color(25, 43, 78, 242)));
            g.fillRoundRect(1, 1, w - 3, h - 3, 23, 23);
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(2, 2, w - 5, h - 5, 23, 23);
            g.setColor(new Color(246, 216, 140, 105));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(7, 7, w - 15, h - 15, 17, 17);

            int badgeHeight = heading.getHeight() + 5;
            int badgeWidth = Math.min(w - 18, heading.getPreferredSize().width + 52);
            int badgeX = (w - badgeWidth) / 2;
            g.setPaint(new GradientPaint(0, 4, new Color(35, 51, 85), 0, 4 + badgeHeight, new Color(11, 22, 47)));
            g.fillRoundRect(badgeX, 4, badgeWidth, badgeHeight, 18, 18);
            g.setColor(GOLD);
            g.drawRoundRect(badgeX, 4, badgeWidth, badgeHeight, 18, 18);
            g.dispose();
        }
    }

    private static final class RewardCard extends JPanel {
        private static final int MAX_BADGE_SPACE = 24;
        private static final Color OWNED_TEXT = new Color(178, 202, 224);
        private static final Color OWNED_BACKGROUND = new Color(35, 53, 75);
        private static final Color NEW_BACKGROUND = new Color(57, 43, 26);
        private final CardView image;
        private final JLabel badge;
        private final boolean newlyAcquired;

        RewardCard(CardReward reward, int stage, CardArt art) {
            super(null);
            setOpaque(false);
            image = new CardView(reward.person(), stage, art);
            newlyAcquired = reward.status() == CardStatus.NEW;
            badge = new JLabel(newlyAcquired ? "NEW!" : "보유 중", SwingConstants.CENTER);
            badge.setForeground(newlyAcquired ? GOLD : OWNED_TEXT);
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
            int arc = Math.min(12, height);
            var track = new RoundRectangle2D.Double(1, y, getWidth() - 3, height, arc, arc);
            var innerTrack = new RoundRectangle2D.Double(4, y + 3, Math.max(0, getWidth() - 9),
                    Math.max(0, height - 6), Math.max(0, arc - 6), Math.max(0, arc - 6));
            g.setPaint(new GradientPaint(0, y, new Color(6, 22, 25), 0, y + height, new Color(26, 66, 55)));
            g.fill(track);
            if (progress > 0) {
                Shape clip = g.getClip();
                g.clip(innerTrack);
                g.setPaint(new GradientPaint(0, y + 3, new Color(145, 246, 166),
                        0, y + height, new Color(18, 138, 79)));
                g.fill(new Rectangle2D.Double(innerTrack.x, innerTrack.y, innerTrack.width * progress, innerTrack.height));
                g.setClip(clip);
            }
            if (flash > 0) {
                g.setColor(new Color(255, 234, 160, Math.round(160 * flash)));
                g.fill(track);
            }
            g.setColor(GOLD);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(track);
            g.setColor(new Color(101, 201, 151, 140));
            g.draw(innerTrack);
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
    

    private enum HeaderEffect { NONE, VICTORY, DEFEAT }

    private static final class VictoryHeader extends JPanel {
        private static final int WIDTH = 1140;
        private static final int HEIGHT = 168;
        private static final Color FRAME = new Color(225, 191, 125);
        private static final Color DEFEAT_TEXT = new Color(185, 191, 202);
        private static final BasicStroke FRAME_STROKE = new BasicStroke(1.5f);
        private static final GradientPaint PANEL_PAINT = new GradientPaint(0, 0, new Color(14, 27, 54, 222),
                0, HEIGHT, new Color(32, 43, 72, 211));

        private final JLabel verdictLabel;
        private final Timer timer = new Timer(25, e -> advance(System.nanoTime()));
        private final Random random = new Random();
        private final Fireworks fireworks = new Fireworks(random);
        private final Storm storm = new Storm(random);
        private final RoundRectangle2D.Double frame = new RoundRectangle2D.Double();
        private String sessionId;
        private HeaderEffect effect = HeaderEffect.NONE;
        private long startedAt;
        private double elapsed;
        private Font defeatFont;

        VictoryHeader(JLabel title, JLabel verdict, JLabel score) {
            verdictLabel = verdict;
            setLayout(new GridLayout(3, 1, 0, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(0, 150));
            add(title);
            add(verdict);
            add(score);
        }

        void showEffect(String id, HeaderEffect nextEffect) {
            if (id.equals(sessionId) && effect == nextEffect) return;
            stop();
            sessionId = id;
            effect = nextEffect;
            if (effect == HeaderEffect.NONE) return;
            if (effect == HeaderEffect.VICTORY) fireworks.start();
            else storm.start();
            startedAt = System.nanoTime();
            verdictLabel.setVisible(effect != HeaderEffect.DEFEAT);
            timer.start();
            repaint();
        }

        void stop() {
            timer.stop();
            sessionId = null;
            elapsed = 0;
            effect = HeaderEffect.NONE;
            fireworks.clear();
            storm.clear();
            verdictLabel.setVisible(true);
            repaint();
        }

        private void advance(long now) {
            if (effect == HeaderEffect.NONE) return;
            elapsed = (now - startedAt) / 1_000_000_000.0;
            if (effect == HeaderEffect.VICTORY) fireworks.advance(elapsed);
            else storm.advance(elapsed);
            repaint();
        }

        @Override protected void paintComponent(Graphics source) {
            super.paintComponent(source);
            int w = getWidth(), h = getHeight();
            if (w < 10 || h < 10) return;
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            g.scale((double) w / WIDTH, (double) h / HEIGHT);
            frame.setRoundRect(2, 2, WIDTH - 4, HEIGHT - 4, 24, 24);
            g.setPaint(PANEL_PAINT);
            g.fill(frame);
            if (effect != HeaderEffect.NONE) {
                Graphics2D scene = (Graphics2D) g.create();
                scene.clip(frame);
                if (effect == HeaderEffect.VICTORY) fireworks.paint(scene, elapsed);
                else storm.paint(scene, elapsed);
                scene.dispose();
            }
            g.setColor(FRAME);
            g.setStroke(FRAME_STROKE);
            g.draw(frame);
            g.dispose();
            if (effect == HeaderEffect.DEFEAT) paintDefeatVerdict(source);
        }

        private void paintDefeatVerdict(Graphics source) {
            Graphics2D g = (Graphics2D) source.create();
            UiStyle.prepare(g);
            Rectangle row = verdictLabel.getBounds();
            g.clip(row);
            double remaining = 1 - Math.clamp(elapsed / 1.3, 0.0, 1.0);
            float emphasis = (float) (remaining * remaining * remaining);
            float fontSize = Math.min(row.height * .9f, verdictLabel.getFont().getSize2D() * (.96f + .44f * emphasis));
            if (fontSize > 0) {
                if (defeatFont == null || defeatFont.getSize2D() != fontSize) {
                    defeatFont = verdictLabel.getFont().deriveFont(Font.BOLD, fontSize);
                }
                g.setFont(defeatFont);
                g.setColor(emphasis == 0 ? DEFEAT_TEXT : new Color(185 + Math.round(70 * emphasis),
                        191 - Math.round(91 * emphasis), 202 - Math.round(87 * emphasis)));
                var metrics = g.getFontMetrics();
                String text = verdictLabel.getText();
                g.drawString(text, row.x + (row.width - metrics.stringWidth(text)) / 2,
                        row.y + (row.height - metrics.getHeight()) / 2 + metrics.getAscent());
            }
            g.dispose();
        }

        @Override public void removeNotify() {
            stop();
            super.removeNotify();
        }

        private static AlphaComposite[] opacitySteps() {
            var levels = new AlphaComposite[65];
            for (int i = 0; i < levels.length; i++) levels[i] = AlphaComposite.SrcOver.derive(i / 64f);
            return levels;
        }

        private static final AlphaComposite[] OPACITY = opacitySteps();

        private static void opacity(Graphics2D g, double strength) {
            g.setComposite(OPACITY[(int) Math.round(64 * Math.clamp(strength, 0.0, 1.0))]);
        }

        private static RadialGradientPaint glow(float x, float y, float radius, Color color) {
            return new RadialGradientPaint(x, y, radius, new float[]{0, .35f, 1}, new Color[]{color,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 3),
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)});
        }

        private static final class Fireworks {
            private static final Color[] COLORS = {new Color(255, 201, 104), new Color(255, 230, 174),
                    new Color(183, 220, 255)};
            private static final Color WHITE_GOLD = new Color(255, 249, 227);
            private static final Paint VERDICT_LIGHT = glow(WIDTH / 2f, 87, 186, new Color(246, 192, 80, 40));
            private static final BasicStroke TRAIL = new BasicStroke(.95f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            private final Random random;
            private final List<Burst> bursts = new ArrayList<>();
            private final double[] next = new double[2];
            private final Line2D.Double trail = new Line2D.Double();
            private final Ellipse2D.Double point = new Ellipse2D.Double();

            Fireworks(Random random) { this.random = random; }

            void start() {
                next[0] = .10;
                next[1] = .44;
            }

            void clear() { bursts.clear(); }

            void advance(double elapsed) {
                for (int i = bursts.size() - 1; i >= 0; i--) {
                    if (elapsed - bursts.get(i).started > 1.75) bursts.remove(i);
                }
                for (int side = 0; side < next.length; side++) {
                    if (elapsed < next[side]) continue;
                    float x = random.nextFloat(128, 282);
                    if (side == 1) x = WIDTH - x;
                    float y = random.nextFloat(50, 100);
                    float speed = random.nextFloat(104, 182);
                    Color color = COLORS[random.nextInt(COLORS.length)];
                    bursts.add(new Burst(x, y, elapsed, speed, color, random.nextInt(36, 56), random));
                    if (speed > 140 && random.nextBoolean()) {
                        bursts.add(new Burst(x + random.nextFloat(-24, 24), y + random.nextFloat(-17, 17),
                                elapsed + random.nextDouble(.34, .53), speed * .46f,
                                COLORS[random.nextInt(COLORS.length)], random.nextInt(14, 23), random));
                    }
                    next[side] = elapsed + random.nextDouble(.68, 1.26);
                }
            }

            void paint(Graphics2D g, double elapsed) {
                g.setPaint(VERDICT_LIGHT);
                g.fillRect(370, 0, 400, HEIGHT);
                g.setStroke(TRAIL);
                for (var burst : bursts) {
                    double age = elapsed - burst.started;
                    if (age < 0) continue;
                    double ignition = Math.max(0, 1 - age / .33);
                    if (ignition > 0) {
                        opacity(g, ignition);
                        g.setPaint(burst.light);
                        point.setFrame(burst.x - 36, burst.y - 36, 72, 72);
                        g.fill(point);
                    }
                    for (var spark : burst.sparks) paintSpark(g, burst, spark, age);
                }
                g.setComposite(AlphaComposite.SrcOver);
            }

            private void paintSpark(Graphics2D g, Burst burst, Spark spark, double age) {
                if (age >= spark.life) return;
                double fade = 1 - age / spark.life;
                double tailAge = Math.max(0, age - spark.tail);
                double travel = age / (1 + 1.15 * age);
                double trailing = tailAge / (1 + 1.15 * tailAge);
                double x = burst.x + spark.vx * travel;
                double y = burst.y + spark.vy * travel + 23 * age * age;
                g.setColor(spark.color);
                opacity(g, .80 * fade);
                trail.setLine(burst.x + spark.vx * trailing, burst.y + spark.vy * trailing + 23 * tailAge * tailAge, x, y);
                g.draw(trail);
                if (spark.bright) {
                    opacity(g, .14 * fade);
                    point.setFrame(x - 3.3, y - 3.3, 6.6, 6.6);
                    g.fill(point);
                    g.setColor(WHITE_GOLD);
                }
                double radius = spark.size * (.55 + .45 * fade);
                opacity(g, fade * (.82 + .18 * Math.cos(age * 35 + spark.vx)));
                point.setFrame(x - radius, y - radius, radius * 2, radius * 2);
                g.fill(point);
            }

            private record Spark(double vx, double vy, double life, double tail, double size, Color color, boolean bright) { }

            private static final class Burst {
                private final float x, y;
                private final double started;
                private final Paint light;
                private final Spark[] sparks;

                Burst(float x, float y, double started, float speed, Color color, int count, Random random) {
                    this.x = x;
                    this.y = y;
                    this.started = started;
                    light = glow(x, y, 36, WHITE_GOLD);
                    sparks = new Spark[count];
                    double rotation = random.nextDouble(Math.TAU);
                    for (int i = 0; i < count; i++) {
                        double angle = rotation + (i + random.nextDouble(-.35, .35)) * Math.TAU / count;
                        double velocity = speed * random.nextDouble(.48, 1.22);
                        sparks[i] = new Spark(Math.cos(angle) * velocity, Math.sin(angle) * velocity * .78,
                                random.nextDouble(.88, 1.72), random.nextDouble(.09, .18), random.nextDouble(.8, 2.1),
                                i % 6 == 0 ? WHITE_GOLD : color, i % 4 == 0);
                    }
                }
            }
        }

        private static final class Storm {
            private static final int TILE_WIDTH = 512;
            private static final Color[] RAIN_COLORS = {new Color(136, 168, 194, 42), new Color(156, 187, 215, 71),
                    new Color(197, 219, 232, 107)};
            private static final BasicStroke[] RAIN_STROKES = {new BasicStroke(.55f), new BasicStroke(.9f), new BasicStroke(1.35f)};
            private static final Paint MIST = new GradientPaint(0, 104, new Color(132, 147, 171, 0),
                    0, HEIGHT, new Color(132, 147, 171, 42));
            private static final Paint FLASH = new GradientPaint(0, 0, new Color(156, 185, 230, 75),
                    0, HEIGHT, new Color(156, 185, 230, 12));
            private final Random random;
            private final RainDrop[] rain = new RainDrop[100];
            private final Line2D.Double line = new Line2D.Double();
            private CloudLayer[] clouds;
            private Lightning lightning;
            private double nextStrike;

            Storm(Random random) {
                this.random = random;
                var rainRandom = new Random(196);
                for (int i = 0; i < rain.length; i++) {
                    int depth = i < 46 ? 0 : i < 82 ? 1 : 2;
                    rain[i] = new RainDrop(rainRandom.nextDouble(WIDTH + 90), rainRandom.nextDouble(HEIGHT + 48),
                            100 + 69 * depth + rainRandom.nextDouble(59), 8 + 7 * depth + rainRandom.nextDouble(7), depth);
                }
            }

            void start() { nextStrike = random.nextDouble(2.7, 4.5); }
            void clear() { lightning = null; }

            void advance(double elapsed) {
                if (lightning != null && elapsed - lightning.started >= .36) lightning = null;
                if (elapsed < nextStrike) return;
                lightning = new Lightning(elapsed, random);
                nextStrike = elapsed + random.nextDouble(5.4, 9.5);
            }

            void paint(Graphics2D g, double elapsed) {
                if (clouds == null) {
                    clouds = new CloudLayer[]{
                            new CloudLayer(135, new Color(64, 76, 96), 7, 18, 0),
                            new CloudLayer(294, new Color(29, 39, 58), -12, 5, 139),
                            new CloudLayer(902, new Color(12, 20, 36), 19, -9, 44)};
                }
                for (var cloud : clouds) cloud.paint(g, elapsed);
                g.setPaint(MIST);
                g.fillRect(0, 104, WIDTH, HEIGHT - 104);
                if (lightning != null) lightning.paint(g, elapsed);
                int depth = -1;
                for (var drop : rain) {
                    if (drop.depth != depth) {
                        depth = drop.depth;
                        g.setStroke(RAIN_STROKES[depth]);
                        g.setColor(RAIN_COLORS[depth]);
                    }
                    double y = (drop.y + elapsed * drop.speed) % (HEIGHT + 48) - 30;
                    double x = (drop.x + (drop.speed * elapsed + drop.y) * .25) % (WIDTH + 90) - 45;
                    line.setLine(x, y, x + drop.length * .26, y + drop.length);
                    g.draw(line);
                }
            }

            private record RainDrop(double x, double y, double speed, double length, int depth) { }

            private static final class CloudLayer {
                private final BufferedImage tile = new BufferedImage(TILE_WIDTH, 118, BufferedImage.TYPE_INT_ARGB_PRE);
                private final int speed, y, offset;

                CloudLayer(long seed, Color color, int speed, int y, int offset) {
                    this.speed = speed;
                    this.y = y;
                    this.offset = offset;
                    var random = new Random(seed);
                    Graphics2D g = tile.createGraphics();
                    UiStyle.prepare(g);
                    var base = new Path2D.Float();
                    base.moveTo(0, 0);
                    base.lineTo(TILE_WIDTH, 0);
                    double phase = random.nextDouble(Math.TAU);
                    for (int x = TILE_WIDTH; x >= 0; x -= 4) {
                        double angle = Math.TAU * x / TILE_WIDTH;
                        double edge = 47 + 8 * Math.cos(3 * angle + phase) + 7 * Math.cos(7 * angle - phase);
                        base.lineTo(x, edge);
                    }
                    base.closePath();
                    g.setPaint(new GradientPaint(0, 0, color, 0, 61,
                            new Color(color.getRed(), color.getGreen(), color.getBlue(), 38)));
                    g.fill(base);
                    for (int i = 0; i < 10; i++) {
                        float x = i * (TILE_WIDTH / 10f) + random.nextFloat(-18, 18);
                        float centerY = random.nextFloat(9, 48);
                        float radius = random.nextFloat(45, 91);
                        for (int copy = -1; copy <= 1; copy++) {
                            float centerX = x + copy * TILE_WIDTH;
                            if (centerX + radius < 0 || centerX - radius > TILE_WIDTH) continue;
                            g.setPaint(glow(centerX, centerY, radius, color));
                            g.fill(new Rectangle2D.Float(centerX - radius, centerY - radius, radius * 2, radius * 2));
                        }
                    }
                    g.dispose();
                }

                void paint(Graphics2D g, double elapsed) {
                    int start = -Math.floorMod((int) Math.round(elapsed * speed) + offset, TILE_WIDTH);
                    for (int x = start; x < WIDTH; x += TILE_WIDTH) g.drawImage(tile, x, y, null);
                }
            }

            private static final class Lightning {
                private static final BasicStroke HALO = new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                private static final BasicStroke CORE = new BasicStroke(1.55f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
                private static final Color HALO_COLOR = new Color(118, 160, 232);
                private static final Color CORE_COLOR = new Color(218, 235, 255);
                private final double started;
                private final Path2D.Float path = new Path2D.Float();
                private final Paint cloudLight;
                private final float x;

                Lightning(double started, Random random) {
                    this.started = started;
                    float startX = random.nextFloat(155, 305);
                    x = random.nextBoolean() ? startX : WIDTH - startX;
                    cloudLight = glow(x, 21, 174, new Color(151, 189, 249, 115));
                    float px = x, py = random.nextFloat(6, 25);
                    path.moveTo(px, py);
                    for (int i = 0; i < 7; i++) {
                        px += random.nextFloat(-24, 24);
                        py += random.nextFloat(9, 17);
                        path.lineTo(px, py);
                        if (i == 1 || i == 3) {
                            float direction = i == 1 ? -1 : 1;
                            path.moveTo(px, py);
                            path.lineTo(px + direction * random.nextFloat(17, 30), py + 10);
                            path.lineTo(px + direction * random.nextFloat(30, 44), py + 28);
                            path.lineTo(px + direction * random.nextFloat(41, 55), py + 37);
                            path.moveTo(px, py);
                        }
                    }
                }

                void paint(Graphics2D g, double elapsed) {
                    double envelope = Math.max(0, 1 - (elapsed - started) / .36);
                    opacity(g, envelope * .72);
                    g.setPaint(FLASH);
                    g.fillRect(0, 0, WIDTH, HEIGHT);
                    g.setPaint(cloudLight);
                    g.fillRect((int) x - 174, 0, 348, HEIGHT);
                    opacity(g, envelope * .32);
                    g.setColor(HALO_COLOR);
                    g.setStroke(HALO);
                    g.draw(path);
                    opacity(g, envelope * envelope * .84);
                    g.setColor(CORE_COLOR);
                    g.setStroke(CORE);
                    g.draw(path);
                    g.setComposite(AlphaComposite.SrcOver);
                }
            }
        }
    }
}
