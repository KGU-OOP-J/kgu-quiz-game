package org.kgu.adventure;

import org.kgu.application.Application;
import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameFeature;
import org.kgu.model.Person;
import org.kgu.progression.*;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/** One coherent hub for world progression, the economy, achievements and the support deck. */
public final class AdventureView extends JPanel implements View {
    private final GameFeature game;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel summary = new JLabel(), notice = new JLabel(" ");
    private final JPanel world = new JPanel(new BorderLayout(15, 15));
    private final JPanel shop = rows(), achievements = rows(), deck = rows(), review = rows();
    private String category = "과학자";
    private Difficulty difficulty = Difficulty.LOW;
    private Person target;
    public AdventureView(GameFeature game) {
        this.game = game;
        setLayout(new BorderLayout(16, 16)); setBackground(new Color(232, 242, 253));
        setBorder(BorderFactory.createEmptyBorder(22, 30, 22, 30));
        JPanel top = new JPanel(new BorderLayout(10, 8)); top.setOpaque(false);
        JLabel title = heading("탐험가 길드", 30); top.add(title, BorderLayout.NORTH);
        summary.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); top.add(summary, BorderLayout.CENTER);
        JButton menu = button("메인으로", "adventureMenu", game::returnToMenu); top.add(menu, BorderLayout.EAST); add(top, BorderLayout.NORTH);
        tabs.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 20f));
        tabs.addTab("마을 지도", world); tabs.addTab("상점", scroll(shop)); tabs.addTab("도전과제", scroll(achievements));
        tabs.addTab("카드 장착", scroll(deck)); tabs.addTab("오답 노트", scroll(review));
        add(tabs, BorderLayout.CENTER); notice.setFont(UiStyle.FONT.deriveFont(18f)); notice.setForeground(new Color(57, 81, 118)); add(notice, BorderLayout.SOUTH);
    }
    public void selectVillage(String village) {
        if (Application.CATEGORIES.contains(village)) { category = village; target = null; }
        selectTab("world");
    }
    public void selectTab(String tab) {
        int index = switch (tab) { case "shop" -> 1; case "achievements" -> 2; case "deck" -> 3; case "review" -> 4; default -> 0; };
        tabs.setSelectedIndex(index);
    }
    public void refresh() {
        PlayerProgress p = game.profile();
        summary.setText(game.playerId() + " · Lv." + p.level() + " · XP " + p.levelExperience() + "/" + PlayerProgress.experienceForLevel(p.level())
            + " · 코인 " + p.coins() + " · 별 " + p.totalStars() + " · 장착: " + game.equippedPower().title());
        buildWorld(); buildShop(); buildAchievements(); buildDeck(); buildReview();
        revalidate(); repaint();
    }
    private void buildWorld() {
        world.removeAll(); world.setOpaque(false); world.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        JPanel villages = new JPanel(new GridLayout(2, 2, 16, 16)); villages.setOpaque(false);
        for (String village : Application.CATEGORIES) {
            JPanel tile = new JPanel(new BorderLayout(8, 12)); tile.setBackground(Color.WHITE);
            tile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(village.equals(category) ? new Color(41, 135, 232) : new Color(200, 215, 236), 2), BorderFactory.createEmptyBorder(18, 20, 18, 20)));
            tile.add(heading(village + " 마을", 23), BorderLayout.NORTH);
            int cleared = game.profile().cleared(village);
            JLabel state = new JLabel("<html>인물 도전 " + cleared + "/20 클리어<br><br>" + (game.profile().bossUnlocked(village) ? "보스 해금 완료" : "서로 다른 인물 3명 승리 → 보스 해금") + "</html>");
            state.setFont(UiStyle.FONT.deriveFont(18f)); tile.add(state, BorderLayout.CENTER);
            tile.add(button("마을 탐험", "village" + village, () -> { category = village; target = null; refresh(); }), BorderLayout.SOUTH); villages.add(tile);
        }
        world.add(villages, BorderLayout.CENTER);
        JPanel challenge = new JPanel(new GridLayout(0, 1, 6, 8)); challenge.setOpaque(false);
        challenge.add(heading(category + " · 도전 준비", 24));
        JPanel selectors = new JPanel(new GridLayout(1, 3, 12, 0)); selectors.setOpaque(false);
        JComboBox<String> categories = new JComboBox<>(Application.CATEGORIES.toArray(String[]::new)); categories.setSelectedItem(category);
        JComboBox<Difficulty> difficulties = new JComboBox<>(Difficulty.values()); difficulties.setSelectedItem(difficulty);
        List<Person> candidates = game.getCandidates(category);
        if (!candidates.contains(target)) target = candidates.getFirst();
        JComboBox<Person> targets = new JComboBox<>(candidates.toArray(Person[]::new)); targets.setSelectedItem(target);
        for (JComboBox<?> box : new JComboBox<?>[]{categories, difficulties, targets}) box.setFont(UiStyle.FONT.deriveFont(19f));
        categories.addActionListener(e -> { category = (String)categories.getSelectedItem(); target = null; refresh(); });
        difficulties.addActionListener(e -> { difficulty = (Difficulty)difficulties.getSelectedItem(); refresh(); });
        targets.addActionListener(e -> { target = (Person)targets.getSelectedItem(); refresh(); });
        selectors.add(categories); selectors.add(difficulties); selectors.add(targets); challenge.add(selectors);
        String stage = "stage.normal." + category + "|" + target.getName() + "|" + difficulty.getLabel();
        JLabel stars = new JLabel("최고 기록: " + "★".repeat(game.profile().number(stage)) + "☆".repeat(3 - Math.min(3, game.profile().number(stage)))
            + "   |   별 조건: 승리 / 정답률 80% 이상 / 분석·힌트 미사용"); stars.setFont(UiStyle.FONT.deriveFont(17f)); challenge.add(stars);
        JPanel actions = new JPanel(new GridLayout(1, 3, 12, 0)); actions.setOpaque(false);
        actions.add(button("인물 도전 · 5문제", "startChallenge", () -> game.startChallenge(category, difficulty, target, false)));
        JButton boss = button("마을 보스 · 8문제", "startBoss", () -> game.startChallenge(category, difficulty, candidates.getLast(), true));
        boss.setEnabled(game.profile().bossUnlocked(category)); actions.add(boss);
        actions.add(button("연습으로 카드 얻기", "worldPractice", () -> game.startPractice(category, difficulty))); challenge.add(actions);
        JLabel goal = new JLabel("다음 목표: " + game.nextGoal()); goal.setFont(UiStyle.FONT.deriveFont(17f)); challenge.add(goal);
        world.add(challenge, BorderLayout.SOUTH);
    }
    private void buildShop() {
        shop.removeAll(); shop.add(info("전투 준비 상점", "게임으로 얻은 코인만 사용합니다. 각 아이템은 전투당 1개씩 사용 가능하며 구매 즉시 저장됩니다."));
        for (ShopItem item : ShopItem.values()) shop.add(card(item.label() + " · " + item.price() + " 코인", item.description() + " · 보유 " + game.profile().inventory(item),
            button("구매", "buy" + item.name(), () -> { game.progress().buy(item); notice.setText(item.label() + " 구매 완료"); refresh(); })));
        shop.add(Box.createVerticalGlue());
    }
    private void buildAchievements() {
        achievements.removeAll(); achievements.add(info("도전과제", "달성한 보상은 직접 수령하세요. 이미 받은 보상은 중복 지급되지 않습니다."));
        for (Achievement a : Achievement.values()) {
            PlayerProgress p = game.profile(); boolean claimed = a.claimed(p), unlocked = a.unlocked(p);
            JButton claim = button(claimed ? "수령 완료" : unlocked ? "보상 받기" : "도전 중", "claim" + a.name(), () -> { game.progress().claim(a); notice.setText(a.title() + " · 코인 +" + a.reward()); refresh(); });
            claim.setEnabled(unlocked && !claimed);
            achievements.add(card(a.title(), a.description() + " · 보상 " + a.reward() + " 코인", claim));
        }
        achievements.add(Box.createVerticalGlue());
    }
    private void buildDeck() {
        deck.removeAll(); deck.add(info("인물 카드 장착", "획득한 카드 1장을 지원 카드로 장착합니다. 높은 단계와 숙련도가 버프를 강화합니다. 승리 시 지원 카드와 보상 인물의 숙련도 +1, 3회마다 강화됩니다."));
        List<Person> owned = game.ownedCards();
        if (owned.isEmpty()) deck.add(info("아직 카드가 없습니다", "연습에서 정답을 맞히거나 인물 도전에서 승리하면 카드를 얻습니다. 카드가 없어도 모든 일반 도전에 참가할 수 있습니다."));
        for (Person person : owned) {
            CardPower power = game.cardPower(person); boolean equipped = game.profile().equipped().equals(CardPower.key(person));
            JButton equip = button(equipped ? "장착 중" : "카드 사용", "equip" + person.getName(), () -> { game.equip(person); notice.setText(power.title() + " 장착 완료"); refresh(); }); equip.setEnabled(!equipped);
            JPanel row = card(power.title(), power.description() + " · 숙련도 " + game.profile().mastery(CardPower.key(person)) + " (강화 " + game.profile().masteryLevel(CardPower.key(person)) + "/5)", equip);
            game.image("cards/" + person.getName() + "_1").ifPresent(image -> {
                JLabel icon = new JLabel(new ImageIcon(image.getScaledInstance(62, 84, Image.SCALE_SMOOTH))); row.add(icon, BorderLayout.WEST);
            });
            deck.add(row);
        }
        deck.add(Box.createVerticalGlue());
    }
    private void buildReview() {
        review.removeAll(); review.add(info("오답 재도전", "틀린 문제를 다시 풀면 오답 노트에서 제거됩니다. 최근 40문제는 일반 출제에서 우선적으로 피하며, 복습도 XP와 코인을 제공합니다."));
        review.add(card("저장된 오답 " + game.profile().wrongQuestions().size() + "개", "최대 10문제를 분야·난이도를 섞어서 다시 풀어봅니다.", button("오답 재도전", "startReview", game::startReview)));
        review.add(info("학습 기록", "누적 정답 " + game.profile().number("correct") + " / 제출 " + game.profile().number("attempts") + " · 최고 전투 콤보 " + game.profile().number("bestCombo") + " · 배틀 승리 " + game.profile().number("wins")));
        review.add(Box.createVerticalGlue());
    }
    @FunctionalInterface private interface Command { void run() throws Exception; }
    private JButton button(String text, String name, Command command) {
        JButton b = new JButton(text); b.setName(name); b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 18f));
        b.addActionListener(e -> { try { command.run(); } catch (Exception ex) { notice.setText(ex.getMessage()); JOptionPane.showMessageDialog(this, ex.getMessage(), "탐험가 길드", JOptionPane.WARNING_MESSAGE); } }); return b;
    }
    private static JPanel rows() { JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBackground(new Color(235, 244, 255)); return p; }
    private static JScrollPane scroll(JPanel panel) { JScrollPane scroll = new JScrollPane(panel); scroll.getVerticalScrollBar().setUnitIncrement(22); scroll.setBorder(BorderFactory.createEmptyBorder()); return scroll; }
    private static JLabel heading(String text, int size) { JLabel l = new JLabel(text); l.setFont(UiStyle.FONT.deriveFont(Font.BOLD, (float)size)); l.setForeground(UiStyle.NAVY); return l; }
    private static JPanel info(String title, String description) { return card(title, description, null); }
    private static JPanel card(String title, String description, JButton action) {
        JPanel p = new JPanel(new BorderLayout(14, 8)); p.setBackground(Color.WHITE); p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, new Color(235, 244, 255)), BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        JPanel text = new JPanel(new BorderLayout(0, 7)); text.setOpaque(false); text.add(heading(title, 21), BorderLayout.NORTH);
        JTextArea body = new JTextArea(description); body.setLineWrap(true); body.setWrapStyleWord(true); body.setEditable(false); body.setOpaque(false); body.setFont(UiStyle.FONT.deriveFont(17f)); body.setRows(2); text.add(body, BorderLayout.CENTER);
        p.add(text, BorderLayout.CENTER); if (action != null) p.add(action, BorderLayout.EAST);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); p.setPreferredSize(new Dimension(1000, 130)); return p;
    }
    @Override public ViewId getViewId() { return ViewId.ADVENTURE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
}
