package org.kgu.adventure;

import org.kgu.application.Application;
import org.kgu.application.Difficulty;
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

    public AdventureView(GameFeature game) {
        this.game = game; setLayout(new BorderLayout(22, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 32, 24, 32));
        JPanel header = GameTheme.clear(new BorderLayout(12, 9));
        JPanel title = GameTheme.clear(new GridLayout(2, 1, 0, 7));
        title.add(heading("THE LAST SEMESTER  /  탐험가 길드", 27));
        summary.setFont(UiStyle.FONT.deriveFont(17f)); summary.setForeground(GameTheme.MUTED); title.add(summary);
        header.add(title, BorderLayout.CENTER); header.add(button("메인으로", "adventureMenu", game::returnToMenu), BorderLayout.EAST); add(header, BorderLayout.NORTH);
        JPanel rail = GameTheme.panel(new BorderLayout(0, 20)); rail.setPreferredSize(new Dimension(180, 0));
        rail.setBorder(BorderFactory.createEmptyBorder(20, 14, 20, 14));
        JPanel links = GameTheme.clear(new GridLayout(0, 1, 0, 13));
        String[][] linksData = {{"world", "01  마을 지도"}, {"shop", "02  준비 상점"}, {"achievements", "03  도전과제"}, {"deck", "04  카드 장착"}, {"review", "05  오답 노트"}};
        for (String[] link : linksData) {
            JButton b = button(link[1], "tab" + link[0], () -> selectTab(link[0])); b.setPreferredSize(new Dimension(150, 55)); navigation.put(link[0], b); links.add(b);
        }
        rail.add(links, BorderLayout.NORTH);
        JLabel quest = GameTheme.label("<html>FINAL QUEST<br><br>사라진 교수님을<br>찾아 졸업을 향해.</html>", 15, GameTheme.GOLD); rail.add(quest, BorderLayout.SOUTH);
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
        summary.setText(game.playerId() + "  /  Lv." + p.level() + "  /  XP " + p.levelExperience() + " · " + PlayerProgress.experienceForLevel(p.level())
            + "  /  " + p.coins() + " COIN  /  " + p.totalStars() + " STARS");
        buildWorld(); buildShop(); buildAchievements(); buildDeck(); buildReview(); revalidate(); repaint();
    }
    private void buildWorld() {
        world.removeAll();
        JPanel gate = GameTheme.panel(new BorderLayout(15, 8)); gate.setBorder(BorderFactory.createEmptyBorder(14, 19, 14, 19));
        gate.add(heading(game.profile().professorRescued() ? "졸업을 향한 마지막 여정 · 구출 완료" : "FINAL RESCUE  /  이은정 교수님", 22), BorderLayout.NORTH);
        int solved = game.profile().rescueProgress();
        JProgressBar progress = new JProgressBar(0, PlayerProgress.PROFESSOR_REQUIRED); progress.setValue(solved); progress.setStringPainted(true); progress.setFont(UiStyle.FONT.deriveFont(15f));
        progress.setString(game.profile().professorUnlocked() ? "구출 경로 개방" : "다른 분야의 서로 다른 문제  " + solved + " / " + PlayerProgress.PROFESSOR_REQUIRED + "  정답");
        progress.setPreferredSize(new Dimension(350, 26)); gate.add(progress, BorderLayout.CENTER); world.add(gate, BorderLayout.NORTH);
        JPanel villages = GameTheme.clear(new GridLayout(2, 2, 14, 14));
        for (String village : List.of("과학자", "한국사", "아이돌", "교수님")) {
            boolean unlocked = game.categoryUnlocked(village);
            JPanel tile = new VillageTile(village, !unlocked, village.equals(category)); tile.setLayout(new BorderLayout());
            tile.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            tile.add(heading(village.equals("교수님") ? "FINAL · 교수님 구출" : village + " 마을", 20), BorderLayout.NORTH);
            JLabel state = GameTheme.label(village.equals("교수님") ? unlocked ? "이은정 교수님 · 최종 시련" : "잠김 · 정답 " + Math.min(solved, 30) + "/30" : "인물 " + game.profile().cleared(village) + "/20 · " + (game.profile().bossUnlocked(village) ? "보스 개방" : "3명 승리 시 보스 개방"), 15, GameTheme.PAPER);
            JPanel bottom = GameTheme.clear(new GridLayout(2, 1, 0, 7)); bottom.add(state);
            JButton explore = button(unlocked ? village.equals("교수님") ? "최종 구출 준비" : "탐험하기" : "구출 경로 잠김", "village" + village, () -> { category = village; target = null; refresh(); });
            explore.setEnabled(unlocked); bottom.add(explore); tile.add(bottom, BorderLayout.SOUTH); villages.add(tile);
        }
        world.add(villages, BorderLayout.CENTER);
        if (!game.categoryUnlocked(category)) { category = "과학자"; target = null; }
        JPanel challenge = GameTheme.panel(new GridLayout(0, 1, 0, 8)); challenge.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        challenge.add(heading(category + "  /  도전 준비", 21));
        JPanel selectors = GameTheme.clear(new GridLayout(1, 3, 10, 0));
        JComboBox<String> categories = new JComboBox<>(new String[]{"과학자", "한국사", "아이돌", "교수님"}); categories.setSelectedItem(category);
        JComboBox<Difficulty> difficulties = new JComboBox<>(Difficulty.values()); difficulties.setSelectedItem(difficulty);
        List<Person> candidates = game.getChallengeCandidates(category);
        if (!candidates.contains(target)) target = candidates.getFirst();
        JComboBox<Person> targets = new JComboBox<>(candidates.toArray(Person[]::new)); targets.setSelectedItem(target);
        for (JComboBox<?> box : new JComboBox<?>[]{categories, difficulties, targets}) box.setFont(UiStyle.FONT.deriveFont(17f));
        categories.addActionListener(e -> {
            String next = (String)categories.getSelectedItem();
            if (game.categoryUnlocked(next)) { category = next; target = null; }
            else notice.setText("교수님 마을 잠김 · 다른 분야 문제 " + game.profile().rescueProgress() + "/30 정답");
            refresh();
        });
        difficulties.addActionListener(e -> { difficulty = (Difficulty)difficulties.getSelectedItem(); refresh(); });
        targets.addActionListener(e -> { target = (Person)targets.getSelectedItem(); refresh(); });
        selectors.add(categories); selectors.add(difficulties); selectors.add(targets); challenge.add(selectors);
        String stage = "stage." + (category.equals("교수님") ? "boss." : "normal.") + category + "|" + target.getName() + "|" + difficulty.getLabel();
        int best = Math.min(3, game.profile().number(stage));
        challenge.add(GameTheme.label("★".repeat(best) + "☆".repeat(3 - best) + "  /  승리 · 정답률 80% · 분석 미사용", 15, GameTheme.GOLD));
        JPanel actions = GameTheme.clear(new GridLayout(1, category.equals("교수님") ? 1 : 3, 10, 0));
        actions.add(button(category.equals("교수님") ? "이은정 교수님 구출 · 최종 5문제" : "인물 도전 · 5문제", "startChallenge", () -> game.startChallenge(category, difficulty, target, false)));
        if (!category.equals("교수님")) {
            JButton boss = button("마을 보스 · 5문제", "startBoss", () -> game.startChallenge(category, difficulty, candidates.getLast(), true));
            boss.setEnabled(game.profile().bossUnlocked(category)); actions.add(boss);
            actions.add(button("연습하기", "worldPractice", () -> game.startPractice(category, difficulty)));
        }
        challenge.add(actions); world.add(challenge, BorderLayout.SOUTH);
    }
    private void buildShop() {
        shop.removeAll(); shop.add(info("PREPARATION  /  여정의 준비", "승리로 얻은 코인으로 다음 시련을 준비하세요. 각 아이템은 전투마다 한 번씩 사용할 수 있습니다."));
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
        deck.removeAll(); deck.add(info("SUPPORT DECK  /  함께하는 인물", "수집한 카드 한 장을 장착하세요. 인물 고유 버프와 단계, 숙련도가 전투에 적용됩니다."));
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
        JButton b = new UiButton(text, GameTheme.TEAL); b.setName(name); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f));
        b.addActionListener(e -> { try { command.run(); } catch (Exception ex) { notice.setText(ex.getMessage()); } }); return b;
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
    private final class VillageTile extends JPanel {
        private final String village;
        private final boolean locked, selected;
        VillageTile(String village, boolean locked, boolean selected) { this.village = village; this.locked = locked; this.selected = selected; setOpaque(false); }
        @Override protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); GameTheme.paintSurface(g, 0, 0, getWidth(), getHeight(), 18, selected);
            String key = switch (village) { case "과학자" -> "science"; case "한국사" -> "history"; case "아이돌" -> "idol"; default -> "professor"; };
            Optional<BufferedImage> image = GameTheme.asset("village-" + key);
            if (image.isEmpty()) image = game.image("ui/" + village);
            if (image.isPresent()) { Shape clip = g.getClip(); g.clip(new java.awt.geom.RoundRectangle2D.Float(2, 2, getWidth() - 4, getHeight() - 4, 18, 18)); GameTheme.cover(g, image.get(), 0, 0, getWidth(), getHeight()); g.setClip(clip); }
            g.setPaint(new GradientPaint(0, 0, new Color(5, 14, 26, locked ? 230 : 155), 0, getHeight(), new Color(5, 14, 26, 245))); g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 18, 18);
            if (locked) { g.setColor(GameTheme.GOLD); g.setStroke(new BasicStroke(2)); int x = getWidth() - 45; g.drawArc(x - 10, 22, 20, 24, 0, 180); g.fillRoundRect(x - 13, 34, 26, 21, 5, 5); }
            g.dispose();
        }
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
