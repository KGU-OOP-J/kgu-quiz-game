package org.kgu.adventure;

import org.kgu.application.Application;
import org.kgu.application.Difficulty;
import org.kgu.collection.view.CardArt;
import org.kgu.collection.view.CardView;
import org.kgu.game.feature.GameFeature;
import org.kgu.model.Person;
import org.kgu.progression.*;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public final class AdventureView extends JPanel implements View {
    private final GameFeature game;
    private final CardLayout pages = new CardLayout();
    private final JPanel content = GameTheme.clear(pages);
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private final JLabel summary = new JLabel(), notice = new JLabel(" ");
    private final JPanel world = GameTheme.clear(new BorderLayout(12, 12));
    private final JPanel shop = rows(), achievements = rows(), deck = rows(), review = rows();
    private String activeTab = "world", category = "과학자";
    private Difficulty difficulty = Difficulty.LOW;
    private Person target;
    private int preparationStep;
    private ChallengeHelpPanel challengeHelp;

    public AdventureView(GameFeature game) {
        this.game = game; setLayout(new BorderLayout(22, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 32, 24, 32));
        JPanel header = GameTheme.clear(new BorderLayout(12, 9));
        JPanel title = GameTheme.clear(new GridLayout(2, 1, 0, 7));
        title.add(heading("도전 준비", 27));
        summary.setFont(UiStyle.FONT.deriveFont(17f)); summary.setForeground(GameTheme.MUTED); title.add(summary);
        header.add(title, BorderLayout.CENTER); header.add(button("메인으로", "adventureMenu", game::returnToMenu), BorderLayout.EAST); add(header, BorderLayout.NORTH);
        JPanel rail = GameTheme.panel(new BorderLayout(0, 20)); rail.setPreferredSize(new Dimension(180, 0));
        rail.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        JPanel links = GameTheme.clear(new GridLayout(0, 1, 0, 13));
        String[][] linksData = {{"world", "도전 준비"}, {"shop", "상점"}, {"achievements", "도전과제"}, {"deck", "카드 장착"}, {"review", "오답 노트"}};
        for (String[] link : linksData) {
            JButton b = button(link[1], "tab" + link[0], () -> selectTab(link[0])); b.setPreferredSize(new Dimension(150, 55)); navigation.put(link[0], b); links.add(b);
        }
        rail.add(links, BorderLayout.NORTH);
        add(rail, BorderLayout.WEST);
        content.add(world, "world"); content.add(GameTheme.scroll(shop), "shop"); content.add(GameTheme.scroll(achievements), "achievements");
        content.add(GameTheme.scroll(deck), "deck"); content.add(GameTheme.scroll(review), "review"); add(content, BorderLayout.CENTER);
        notice.setFont(UiStyle.FONT.deriveFont(16f)); notice.setForeground(GameTheme.GOLD); add(notice, BorderLayout.SOUTH); selectTab("world");
    }
    public void selectVillage(String village) {
        if (Application.CATEGORIES.contains(village) && game.categoryUnlocked(village)) { category = village; target = null; }
        else if (village.equals("교수님")) notice.setText("다른 분야의 서로 다른 문제 30개를 맞히면 교수님 마을이 열립니다.");
        selectTab("world");
    }
    public void selectTab(String tab) {
        activeTab = navigation.containsKey(tab) ? tab : "world"; pages.show(content, activeTab);
        navigation.forEach((key, b) -> { b.putClientProperty("selected", key.equals(activeTab)); b.repaint(); }); repaint();
    }
    public void refresh() {
        PlayerProgress p = game.profile();
        summary.setText(game.playerId() + (game.isDemoMode() ? " [시연 모드]" : "") + "  /  Lv." + p.level() + "  /  XP " + p.levelExperience() + " · " + PlayerProgress.experienceForLevel(p.level())
            + "  /  " + p.coins() + " COIN  /  " + p.totalStars() + " STARS");
        buildWorld(); buildShop(); buildAchievements(); buildDeck(); buildReview(); revalidate(); repaint();
    }
    private void buildWorld() {
        world.removeAll();
        world.add(journeyProgress(), BorderLayout.NORTH);
        JPanel preparation = GameTheme.clear(new BorderLayout(0, 16));
        JPanel villages = GameTheme.clear(new GridLayout(1, 4, 10, 0));
        for (String village : List.of("과학자", "한국사", "아이돌", "교수님")) {
            boolean unlocked = game.categoryUnlocked(village);
            String state = village.equals("교수님") ? unlocked ? "최종 도전 가능" : "정답 " + Math.min(game.profile().rescueProgress(), PlayerProgress.PROFESSOR_REQUIRED) + "/30 · 잠김"
                : "승리한 인물 " + game.profile().cleared(village) + "명";
            JButton choice = button("<html><center>" + village + "<br><small>" + state + "</small></center></html>",
                "village" + village, () -> { category = village; target = null; preparationStep = 0; buildWorld(); revalidate(); repaint(); });
            choice.putClientProperty("selected", village.equals(category));
            choice.setEnabled(unlocked); choice.setPreferredSize(new Dimension(190, 72));
            if (!unlocked) choice.setToolTipText("다른 분야의 서로 다른 문제 30개를 맞히면 열립니다.");
            villages.add(choice);
        }
        preparation.add(villages, BorderLayout.NORTH);
        preparation.add(challengePreparation(), BorderLayout.CENTER);
        world.add(preparation, BorderLayout.CENTER);
    }

    private JPanel journeyProgress() {
        JPanel gate = GameTheme.clear(new BorderLayout(16, 0));
        int solved = Math.min(game.profile().rescueProgress(), PlayerProgress.PROFESSOR_REQUIRED);
        gate.add(GameTheme.label(game.profile().professorUnlocked() ? "교수님 마을 개방" : "교수님 마을 해금  " + solved + " / 30", 17, GameTheme.MUTED));
        JProgressBar progress = new JProgressBar(0, PlayerProgress.PROFESSOR_REQUIRED);
        progress.setUI(new javax.swing.plaf.basic.BasicProgressBarUI());
        progress.setValue(solved); progress.setForeground(GameTheme.TEAL); progress.setBackground(new Color(33, 53, 73));
        progress.setBorderPainted(false); progress.setPreferredSize(new Dimension(180, 8));
        progress.getAccessibleContext().setAccessibleName("교수님 마을 해금 " + solved + "/30 정답");
        gate.setToolTipText("다른 분야의 서로 다른 문제 30개를 맞히면 교수님 마을이 열립니다.");
        JPanel meter = GameTheme.clear(new GridBagLayout()); meter.add(progress);
        gate.add(meter, BorderLayout.EAST);
        return gate;
    }

    private JPanel challengePreparation() {
        if (!game.categoryUnlocked(category)) { category = "과학자"; target = null; }
        List<Person> candidates = game.getChallengeCandidates(category);
        if (!candidates.contains(target)) target = candidates.getFirst();
        JPanel challenge = GameTheme.panel(new BorderLayout(24, 16));
        challenge.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel portrait = GameTheme.clear(new BorderLayout(0, 14));
        portrait.setPreferredSize(new Dimension(320, 420));
        CardView card = new CardView(target, difficulty.getStage(), new CardArt(game::image));
        card.setName("challengePortrait"); card.setPreferredSize(new Dimension(265, 370));
        JPanel cardCenter = GameTheme.clear(new BorderLayout()); cardCenter.add(card); portrait.add(cardCenter);
        JLabel cardNote = GameTheme.label(category + " · " + difficulty.getLabel() + " 단계 카드", 17, GameTheme.GOLD);
        cardNote.setHorizontalAlignment(SwingConstants.CENTER); portrait.add(cardNote, BorderLayout.SOUTH);
        challenge.add(portrait, BorderLayout.WEST);

        JPanel details = GameTheme.clear(new BorderLayout(0, 16));
        JPanel fields = GameTheme.clear(null); fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        JComboBox<Person> targets = new JComboBox<>(candidates.toArray(Person[]::new));
        targets.setName("challengeTarget"); targets.setSelectedItem(target); targets.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f));
        targets.setPreferredSize(new Dimension(360, 46));
        targets.addActionListener(e -> { target = (Person) targets.getSelectedItem(); buildWorld(); revalidate(); repaint(); });
        JPanel personField = field("인물", targets);
        JPanel levels = GameTheme.clear(new GridLayout(1, 3, 8, 0));
        for (Difficulty level : Difficulty.values()) {
            JButton choice = button(level.getLabel(), "difficulty" + level.name(), () -> { difficulty = level; buildWorld(); revalidate(); repaint(); });
            choice.putClientProperty("selected", level == difficulty); choice.setPreferredSize(new Dimension(100, 42)); levels.add(choice);
        }
        fields.add(field("난이도", levels));
        String stage = "stage." + (category.equals("교수님") ? "boss." : "normal.") + category + "|" + target.getName() + "|" + difficulty.getLabel();
        int best = Math.min(3, game.profile().number(stage));
        cardNote.setText(difficulty.getLabel() + " 단계 · 최고 기록  " + "★".repeat(best) + "☆".repeat(3 - best));


        JPanel ready = GameTheme.clear(new BorderLayout(0, 14));
        ready.add(GameTheme.label("5문제 · 정답 3개 이상이면 승리", 18, GameTheme.PAPER), BorderLayout.NORTH);
        JButton start = primaryButton(category.equals("교수님") ? "교수님 구출 시작  →" : "도전 시작  →", "startChallenge", () -> game.startChallenge(category, difficulty, target, false));
        start.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f)); start.setPreferredSize(new Dimension(360, 56)); ready.add(start, BorderLayout.CENTER);
        ready.add(button("규칙 · 보유 아이템 보기", "challengeDetails", this::showChallengeDetails), BorderLayout.SOUTH);
        CardLayout preparationPages = new CardLayout();
        JPanel steps = GameTheme.clear(preparationPages);
        JPanel choosePerson = GameTheme.clear(new BorderLayout(0, 24));
        choosePerson.add(personField, BorderLayout.NORTH);
        JPanel next = GameTheme.clear(new BorderLayout(0, 18));
        next.add(GameTheme.label("도전할 인물을 고른 뒤 난이도를 선택하세요.", 17, GameTheme.MUTED), BorderLayout.NORTH);
        JButton advance = primaryButton("난이도 선택으로  →", "preparationNext", () -> { preparationStep = 1; buildWorld(); revalidate(); repaint(); });
        advance.setPreferredSize(new Dimension(360, 56)); advance.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 21f));
        next.add(advance);
        JPanel introduction = GameTheme.panel(new BorderLayout(0, 12));
        introduction.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        introduction.add(GameTheme.label("어떤 인물인가요?", 20, GameTheme.GOLD), BorderLayout.NORTH);
        JTextArea biography = new JTextArea(String.join("\n", target.getInformation()));
        biography.setEditable(false); biography.setLineWrap(true); biography.setWrapStyleWord(true);
        biography.setOpaque(false); biography.setFont(UiStyle.FONT.deriveFont(20f)); biography.setForeground(GameTheme.PAPER);
        introduction.add(GameTheme.scroll(biography));
        choosePerson.add(introduction, BorderLayout.CENTER); choosePerson.add(next, BorderLayout.SOUTH);
        JPanel chooseSettings = GameTheme.clear(new BorderLayout(0, 28));
        chooseSettings.add(fields, BorderLayout.NORTH);
        JPanel supplies = GameTheme.panel(new BorderLayout(0, 12));
        supplies.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        supplies.add(GameTheme.label(target.getName() + " · " + difficulty.getLabel() + " 단계", 21, GameTheme.PAPER), BorderLayout.NORTH);
        JPanel inventory = GameTheme.clear(new GridLayout(3, 1, 0, 10));
        for (ShopItem item : ShopItem.values()) {
            JPanel row = GameTheme.clear(new BorderLayout());
            row.add(GameTheme.label(item.label(), 19, GameTheme.MUTED));
            row.add(GameTheme.label("보유 " + game.profile().inventory(item) + "개", 19, GameTheme.GOLD), BorderLayout.EAST);
            inventory.add(row);
        }
        supplies.add(inventory); supplies.add(GameTheme.label("한 판에 아이템 1개 사용 가능", 16, GameTheme.MUTED), BorderLayout.SOUTH);
        chooseSettings.add(supplies, BorderLayout.CENTER); chooseSettings.add(ready, BorderLayout.SOUTH);
        steps.add(choosePerson, "person"); steps.add(chooseSettings, "settings");
        preparationPages.show(steps, preparationStep == 0 ? "person" : "settings");
        JPanel tabs = GameTheme.clear(new GridLayout(1, 2, 10, 0));
        for (int i = 0; i < 2; i++) {
            int step = i;
            JButton tab = button(i == 0 ? "1  인물 선택" : "2  난이도 · 준비", i == 0 ? "preparationPerson" : "preparationSettings", () -> {
                preparationStep = step; buildWorld(); revalidate(); repaint();
            });
            tab.putClientProperty("selected", preparationStep == i); tabs.add(tab);
        }
        JPanel selection = GameTheme.clear(new BorderLayout(0, 22));
        selection.add(tabs, BorderLayout.NORTH); selection.add(steps);
        details.add(selection, BorderLayout.CENTER); challenge.add(details, BorderLayout.CENTER);

        JPanel alternatives = GameTheme.clear(new GridLayout(1, category.equals("교수님") ? 1 : 2, 12, 0));
        if (!category.equals("교수님")) {
            JButton practice = button("연습하기", "worldPractice", () -> game.startPractice(category, difficulty));
            practice.setPreferredSize(new Dimension(300, 46)); alternatives.add(practice);
            boolean open = game.profile().bossUnlocked(category);
            Person bossTarget = candidates.getLast();
            JButton boss = button(open ? "보스 도전 · " + bossTarget.getName() : "보스 잠김 · 승리 " + game.profile().cleared(category) + "/3명", "startBoss", () -> game.startChallenge(category, difficulty, bossTarget, true));
            boss.setEnabled(open); alternatives.add(boss);
        } else {
            alternatives.add(GameTheme.label("배틀이 끝나면 결과 화면에서 정답과 오답을 다시 확인할 수 있어요.", 16, GameTheme.MUTED));
        }
        challenge.add(alternatives, BorderLayout.SOUTH);
        return challenge;
    }

    private void showChallengeDetails() {
        if (challengeHelp != null) content.remove(challengeHelp);
        challengeHelp = new ChallengeHelpPanel(game.profile(), () -> {
            selectTab("world");
        });
        content.add(challengeHelp, "help");
        pages.show(content, "help"); content.revalidate(); content.repaint();
    }

    private static JPanel field(String title, JComponent component) {
        JPanel panel = GameTheme.clear(new BorderLayout(0, 7));
        panel.setAlignmentX(LEFT_ALIGNMENT); panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.add(GameTheme.label(title, 16, GameTheme.MUTED), BorderLayout.NORTH); panel.add(component);
        return panel;
    }
    private void buildShop() {
        shop.removeAll(); shop.add(info("PREPARATION  /  여정의 준비", "승리로 얻은 코인으로 다음 시련을 준비하세요. 종류와 관계없이 전투당 아이템 1개만 사용할 수 있습니다."));
        for (ShopItem item : ShopItem.values()) {
            JPanel row = card(item.label() + "  /  " + item.price() + " COIN", item.description() + " · 보유 " + game.profile().inventory(item), button("구매하기", "buy" + item.name(), () -> { game.progress().buy(item); notice.setText(item.label() + " 구매 완료"); refresh(); }));
            row.add(new Sigil("item-" + item.name().toLowerCase(Locale.ROOT), item.ordinal()), BorderLayout.WEST); shop.add(row);
        }
        shop.add(Box.createVerticalGlue());
    }
    private void buildAchievements() {
        achievements.removeAll(); achievements.add(info("ACHIEVEMENTS  /  여정의 기록", "완료한 도전의 보상을 수령하세요. 수령한 보상은 다시 지급되지 않습니다."));
        for (Achievement achievement : Achievement.values()) {
            PlayerProgress p = game.profile(); boolean claimed = achievement.claimed(p), unlocked = achievement.unlocked(p);
            JButton claim = button(claimed ? "수령 완료" : unlocked ? "보상 받기" : "도전 중", "claim" + achievement.name(), () -> { game.progress().claim(achievement); notice.setText(achievement.title() + " · 코인 +" + achievement.reward()); refresh(); });
            claim.setEnabled(unlocked && !claimed);
            JPanel row = card(achievement.title(), achievement.description() + "  /  " + achievement.reward() + " COIN", claim);
            row.add(new Sigil(unlocked ? "badge-unlocked" : "badge-locked", unlocked ? 3 : 4), BorderLayout.WEST); achievements.add(row);
        }
        achievements.add(Box.createVerticalGlue());
    }
    private void buildDeck() {
        deck.removeAll(); deck.add(info("SUPPORT DECK  /  함께하는 인물", "수집한 인물과 함께 도전하세요. 승리하면 숙련도가 오르고, 집중 성향은 경험치를 더 줍니다. 카드가 정답 피해나 승리 조건을 바꾸지는 않습니다."));
        List<Person> owned = game.ownedCards();
        if (owned.isEmpty()) deck.add(info("아직 동료가 없습니다", "다른 분야에서 연습 정답을 맞히거나 인물 도전에서 승리해 첫 카드를 획득하세요."));
        for (Person person : owned) {
            CardPower power = game.cardPower(person); boolean equipped = game.profile().equipped().equals(CardPower.key(person));
            JButton equip = button(equipped ? "함께하는 중" : "지원 카드 장착", "equip" + person.getName(), () -> { game.equip(person); notice.setText(power.title() + " 장착 완료"); refresh(); }); equip.setEnabled(!equipped);
            JPanel row = card(power.title(), power.description() + " · 숙련도 " + game.profile().mastery(CardPower.key(person)) + " · 강화 " + game.profile().masteryLevel(CardPower.key(person)) + "/5", equip);
            JPanel insignia = GameTheme.clear(new BorderLayout(8, 0));
            game.image("cards/" + person.getName() + "_1").ifPresent(image -> insignia.add(new JLabel(new ImageIcon(image.getScaledInstance(63, 84, Image.SCALE_SMOOTH))), BorderLayout.CENTER));
            insignia.add(new Sigil("buff-" + power.kind().name().toLowerCase(Locale.ROOT), 3), BorderLayout.EAST); row.add(insignia, BorderLayout.WEST); deck.add(row);
        }
        deck.add(Box.createVerticalGlue());
    }
    private void buildReview() {
        review.removeAll(); review.add(info("ARCHIVE  /  다시 마주하는 질문", "틀린 문제를 맞히면 오답 노트에서 지워집니다. 복습 정답도 교수님 구출 해금에 반영되며 같은 문제는 중복 집계되지 않습니다."));
        review.add(card("남겨진 질문 " + game.profile().wrongQuestions().size() + "개", "최대 10문제 · 잠긴 교수님 분야는 해금 이후에 출제됩니다.", button("오답 재도전", "startReview", game::startReview)));
        review.add(info("학습 기록", "정답 " + game.profile().number("correct") + " / " + game.profile().number("attempts") + " · 최고 콤보 " + game.profile().number("bestCombo") + " · 배틀 승리 " + game.profile().number("wins")));
        review.add(Box.createVerticalGlue());
    }
    @FunctionalInterface private interface Command { void run() throws Exception; }
    private JButton button(String text, String name, Command command) {
        JButton b = new JButton(text);
        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override protected void paintText(Graphics g, AbstractButton button, Rectangle rect, String value) {
                if (button.isEnabled()) { super.paintText(g, button, rect, value); return; }
                g.setColor(GameTheme.MUTED); g.setFont(button.getFont());
                g.drawString(value, rect.x, rect.y + g.getFontMetrics().getAscent());
            }
        });
        b.setBackground(new Color(39, 56, 74)); b.setForeground(GameTheme.PAPER);
        b.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        b.setContentAreaFilled(true); b.setOpaque(true); b.setFocusPainted(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addPropertyChangeListener("selected", e -> {
            boolean selected = Boolean.TRUE.equals(e.getNewValue());
            b.setBackground(selected ? new Color(57, 85, 105) : new Color(39, 56, 74));
            b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(selected ? GameTheme.TEAL : new Color(39, 56, 74), 2), BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        });
        b.setName(name); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f));
        b.addActionListener(e -> { try { command.run(); } catch (Exception ex) { notice.setText(ex.getMessage()); } }); return b;
    }
    private JButton primaryButton(String text, String name, Command command) {
        JButton b = new UiButton(text, GameTheme.TEAL); b.putClientProperty("selectionTint", new Color(43, 104, 165)); b.setName(name);
        b.addActionListener(e -> { try { command.run(); } catch (Exception ex) { notice.setText(ex.getMessage()); } });
        return b;
    }
    private static JPanel rows() { JPanel panel = GameTheme.clear(null); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); return panel; }
    private static JLabel heading(String text, float size) { JLabel label = GameTheme.label(text, size, GameTheme.PAPER); label.setFont(label.getFont().deriveFont(Font.BOLD)); return label; }
    private static JPanel info(String title, String description) { return card(title, description, null); }
    private static JPanel card(String title, String description, JButton action) {
        JPanel outer = GameTheme.clear(new BorderLayout()); outer.setAlignmentX(Component.LEFT_ALIGNMENT); outer.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JPanel surface = GameTheme.panel(new BorderLayout(15, 10)); surface.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel text = GameTheme.clear(new BorderLayout(0, 8)); text.add(heading(title, 21), BorderLayout.NORTH);
        JTextArea body = new JTextArea(description); body.setLineWrap(true); body.setWrapStyleWord(true); body.setEditable(false); body.setFocusable(false); body.setOpaque(false); body.setForeground(GameTheme.MUTED); body.setFont(UiStyle.FONT.deriveFont(16f)); body.setRows(2); text.add(body);
        surface.add(text, BorderLayout.CENTER); if (action != null) { action.setPreferredSize(new Dimension(160, 48)); surface.add(action, BorderLayout.EAST); }
        outer.add(surface); outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155)); outer.setPreferredSize(new Dimension(850, 145));
        return new CardRow(outer, surface);
    }
    private static final class CardRow extends JPanel {
        private final JPanel surface;
        CardRow(JPanel outer, JPanel surface) { super(new BorderLayout()); this.surface = surface; setOpaque(false); setAlignmentX(Component.LEFT_ALIGNMENT); super.add(outer, BorderLayout.CENTER); setMaximumSize(outer.getMaximumSize()); setPreferredSize(outer.getPreferredSize()); }
        @Override public void add(Component component, Object constraints) { if (surface == null) super.add(component, constraints); else surface.add(component, constraints); }
    }
    private static final class Sigil extends JComponent {
        private final String image;
        private final int type;
        Sigil(String image, int type) { this.image = image; this.type = type; setPreferredSize(new Dimension(65, 70)); }
        @Override protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g);
            Optional<BufferedImage> asset = GameTheme.asset(image);
            if (asset.isPresent()) GameTheme.cover(g, asset.get(), 0, Math.max(0, (getHeight() - 64) / 2), 64, 64);
            else {
                int cx = getWidth() / 2, cy = getHeight() / 2; g.setColor(new Color(222, 180, 104, 25)); g.fillOval(cx - 28, cy - 28, 56, 56); g.setColor(GameTheme.GOLD); g.setStroke(new BasicStroke(2));
                if (type == 0) { g.drawOval(cx - 12, cy - 14, 22, 22); g.drawLine(cx + 6, cy + 4, cx + 19, cy + 17); }
                else if (type == 1) { Polygon p = new Polygon(new int[]{cx - 16, cx, cx + 16, cx + 12, cx}, new int[]{cy - 15, cy - 22, cy - 15, cy + 8, cy + 23}, 5); g.drawPolygon(p); }
                else if (type == 2) { g.drawRoundRect(cx - 12, cy - 8, 24, 29, 7, 7); g.drawRect(cx - 6, cy - 18, 12, 10); g.drawLine(cx, cy, cx, cy + 12); g.drawLine(cx - 6, cy + 6, cx + 6, cy + 6); }
                else { g.drawOval(cx - 21, cy - 21, 42, 42); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 25f)); g.drawString(type == 3 ? "★" : "·", cx - 12, cy + 9); }
            }
            g.dispose();
        }
    }
    @Override protected void paintComponent(Graphics source) { Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), activeTab.equals("shop") ? "shop" : activeTab.equals("world") ? "map" : "guild", 0.86f); g.dispose(); }
    @Override public ViewId getViewId() { return ViewId.ADVENTURE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
