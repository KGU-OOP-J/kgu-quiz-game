package org.kgu.domain.adventure.view;

import org.kgu.core.model.Categories;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.domain.adventure.view.component.AdventureCardRow;
import org.kgu.domain.adventure.view.component.Sigil;
import org.kgu.domain.adventure.view.component.ShopItemIcon;
import org.kgu.domain.adventure.view.component.VillageTile;
import org.kgu.domain.adventure.view.layout.AdventureLayout;
import org.kgu.core.application.Difficulty;
import org.kgu.domain.adventure.feature.AdventureFeature;
import org.kgu.domain.adventure.event.AdventureGUIEvent;
import org.kgu.core.model.Person;
import org.kgu.core.progression.*;
import org.kgu.core.window.*;
import org.kgu.domain.collection.view.component.CardArt;
import org.kgu.domain.collection.view.component.CardView;
import org.kgu.domain.collection.view.component.CardTile;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public final class AdventureView extends JPanel implements View {
    private final AdventureFeature feature;
    private final AdventureGUIEvent event = new AdventureGUIEvent();
    private final CardLayout pages = new CardLayout();
    private final JPanel content = GameTheme.clear(pages);
    private final Map<String, JButton> navigation = new LinkedHashMap<>();
    private final JLabel summary = new JLabel(), notice = new JLabel(" ");
    private final JPanel world = GameTheme.clear(new BorderLayout(12, 12));
    private final JPanel shop = new ShopRows(), achievements = rows(), deck = rows(), review = rows();
    private String activeTab = "world", category = "과학자";
    private Difficulty difficulty = Difficulty.LOW;
    private Person target;

    public AdventureView(AdventureFeature feature) {
        super(new BorderLayout());
        this.feature = feature;
        setBackground(GameTheme.PAGE);
        JPanel header = headerSection();
        JPanel navigation = navigationSection();
        pageContainer();
        notice.setFont(UiStyle.FONT.deriveFont(16f));
        notice.setForeground(GameTheme.BLUE_TEXT);
        add(new AdventureLayout(header, navigation, content, notice));
        selectTab("world");
    }

    private JPanel headerSection() {
        JPanel header = GameTheme.clear(new BorderLayout(12, 8));
        JPanel title = GameTheme.clear(new GridLayout(2, 1, 0, 2));
        title.add(heading("마을 탐험", 26));
        summary.setFont(UiStyle.FONT.deriveFont(17f));
        summary.setForeground(ComponentPainter.MUTED);
        title.add(summary);
        header.add(title, BorderLayout.CENTER);
        JPanel actions = GameTheme.clear(new GridBagLayout());
        JButton home = button("메인으로", "adventureMenu", feature::returnToMenu);
        home.putClientProperty("secondaryAction", true);
        home.setPreferredSize(new Dimension(108, 44));
        actions.add(home);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel navigationSection() {
        JPanel rail = GameTheme.lightPanel(new BorderLayout());
        rail.setPreferredSize(new Dimension(180, 0));
        rail.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));
        JPanel links = GameTheme.clear(new GridLayout(0, 1, 0, 12));
        String[][] entries = {{"world", "마을 지도"}, {"shop", "준비 상점"}, {"achievements", "도전과제"},
            {"deck", "카드 장착"}, {"review", "오답 노트"}};
        for (String[] entry : entries) {
            JButton link = button(entry[1], "tab" + entry[0], () -> selectTab(entry[0]));
            link.setPreferredSize(new Dimension(150, 55));
            link.putClientProperty("secondaryAction", true);
            navigation.put(entry[0], link);
            links.add(link);
        }
        rail.add(links, BorderLayout.NORTH);
        return rail;
    }

    private void pageContainer() {
        world.setPreferredSize(new Dimension(0, 534));
        JScrollPane mapPage = GameTheme.scroll(world);
        mapPage.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        content.add(mapPage, "world");
        content.add(GameTheme.scroll(shop), "shop");
        content.add(GameTheme.scroll(achievements), "achievements");
        content.add(GameTheme.scroll(deck), "deck");
        content.add(GameTheme.scroll(review), "review");
    }

    public void selectVillage(String village) {
        if (Categories.ALL.contains(village) && feature.categoryUnlocked(village)) { category = village; target = null; }
        else if (village.equals("교수님")) notice.setText("다른 분야의 서로 다른 문제 30개를 맞히면 교수님 마을이 열립니다.");
        selectTab("world");
    }
    public void selectTab(String tab) {
        activeTab = navigation.containsKey(tab) ? tab : "world"; pages.show(content, activeTab);
        navigation.forEach((key, b) -> { b.putClientProperty("selected", key.equals(activeTab)); b.repaint(); }); repaint();
    }
    public void refresh() {
        PlayerProgress p = feature.profile();
        summary.setText(feature.playerId() + "  /  Lv." + p.level() + "  /  XP " + p.levelExperience() + " · " + PlayerProgress.experienceForLevel(p.level())
            + "  /  " + p.coins() + " COIN  /  " + p.totalStars() + " STARS");
        buildWorld(); buildShop(); buildAchievements(); buildDeck(); buildReview(); revalidate(); repaint();
    }
    private void buildWorld() {
        world.removeAll();
        JPanel gate = GameTheme.lightPanel(new BorderLayout(15, 8)); gate.setBorder(BorderFactory.createEmptyBorder(14, 19, 14, 19));
        gate.add(heading(feature.profile().professorRescued() ? "졸업을 향한 마지막 여정 · 구출 완료" : "이은정 교수님 구출", 22), BorderLayout.NORTH);
        int solved = feature.profile().rescueProgress();
        JProgressBar progress = new JProgressBar(0, PlayerProgress.PROFESSOR_REQUIRED); progress.setValue(solved); progress.setStringPainted(true); progress.setFont(UiStyle.FONT.deriveFont(15f));
        progress.setString(feature.profile().professorUnlocked() ? "구출 경로 개방" : "다른 분야의 서로 다른 문제  " + solved + " / " + PlayerProgress.PROFESSOR_REQUIRED + "  정답");
        progress.setPreferredSize(new Dimension(350, 26)); gate.add(progress, BorderLayout.CENTER); world.add(gate, BorderLayout.NORTH);
        JPanel villages = GameTheme.clear(new GridLayout(2, 2, 14, 14));
        for (String village : List.of("과학자", "한국사", "아이돌", "교수님")) {
            boolean unlocked = feature.categoryUnlocked(village);
            JPanel tile = new VillageTile(villageArt(village), !unlocked, village.equals(category)); tile.setLayout(new BorderLayout());
            tile.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            JPanel controls = GameTheme.clear(new BorderLayout(0, 4));
            controls.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 88));
            controls.add(heading(village.equals("교수님") ? "교수님 구출" : village + " 마을", 19), BorderLayout.NORTH);
            String status = village.equals("교수님")
                ? (unlocked ? "최종 시련 개방" : "정답 " + Math.min(solved, 30) + " / 30")
                : "승리 " + feature.profile().cleared(village) + " / 20 · "
                    + (feature.profile().bossUnlocked(village) ? "보스 개방" : "보스 잠김");
            JLabel state = GameTheme.label(status, 14, ComponentPainter.MUTED);
            controls.add(state, BorderLayout.CENTER);
            JButton explore = button(unlocked ? village.equals("교수님") ? "최종 구출 준비" : "탐험하기" : "구출 경로 잠김",
                "village" + village, () -> {
                    if (!feature.categoryUnlocked(village)) {
                        showRescueRequirement();
                        return;
                    }
                    category = village;
                    target = null;
                    refresh();
                });
            explore.putClientProperty("secondaryAction", true);
            explore.setPreferredSize(new Dimension(0, 36));
            controls.add(explore, BorderLayout.SOUTH);
            tile.add(controls, BorderLayout.CENTER);
            villages.add(tile);
        }
        world.add(villages, BorderLayout.CENTER);
        if (!feature.categoryUnlocked(category)) { category = "과학자"; target = null; }
        JPanel challenge = GameTheme.lightPanel(new BorderLayout(0, 10));
        challenge.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        challenge.add(heading(category + " · 도전 준비", 20), BorderLayout.NORTH);
        JPanel fields = GameTheme.clear(new BorderLayout(0, 8));
        JPanel selectors = GameTheme.clear(new GridLayout(1, 3, 10, 0));
        JComboBox<String> categories = new JComboBox<>(new String[]{"과학자", "한국사", "아이돌", "교수님"}); categories.setSelectedItem(category);
        JComboBox<Difficulty> difficulties = new JComboBox<>(Difficulty.values()); difficulties.setSelectedItem(difficulty);
        List<Person> candidates = feature.getChallengeCandidates(category);
        if (!candidates.contains(target)) target = candidates.getFirst();
        JComboBox<Person> targets = new JComboBox<>(candidates.toArray(Person[]::new)); targets.setSelectedItem(target);
        for (JComboBox<?> box : new JComboBox<?>[]{categories, difficulties, targets}) box.setFont(UiStyle.FONT.deriveFont(17f));
        categories.addActionListener(e -> {
            String next = (String)categories.getSelectedItem();
            if (feature.categoryUnlocked(next)) { category = next; target = null; }
            else showRescueRequirement();
            refresh();
        });
        difficulties.addActionListener(e -> { difficulty = (Difficulty)difficulties.getSelectedItem(); refresh(); });
        targets.addActionListener(e -> { target = (Person)targets.getSelectedItem(); refresh(); });
        selectors.add(categories); selectors.add(difficulties); selectors.add(targets); fields.add(selectors, BorderLayout.NORTH);
        String stage = "stage." + (category.equals("교수님") ? "boss." : "normal.") + category + "|" + target.getName() + "|" + difficulty.getLabel();
        int best = Math.min(3, feature.profile().number(stage));
        fields.add(GameTheme.label("획득 별 " + best + " / 3  ·  승리 · 정답률 80% · 아이템 미사용", 14, GameTheme.BLUE_TEXT), BorderLayout.SOUTH);
        challenge.add(fields, BorderLayout.CENTER);
        JPanel actions = GameTheme.clear(new GridLayout(1, category.equals("교수님") ? 1 : 3, 10, 0));
        actions.add(button(category.equals("교수님") ? "이은정 교수님 구출 · 최종 3페이즈" : "인물 도전 · 5문제", "startChallenge", () -> feature.startChallenge(category, difficulty, target, false)));
        if (!category.equals("교수님")) {
            JButton boss = button("마을 보스 · 2페이즈", "startBoss", () -> feature.startChallenge(category, difficulty, candidates.stream().filter(p -> p.getName().equals(org.kgu.domain.game.feature.BattlePhases.bossName(category))).findFirst().orElseThrow(), true));
            boss.setEnabled(feature.profile().bossUnlocked(category)); actions.add(boss);
            JButton practice = button("연습하기", "worldPractice", () -> feature.startPractice(category, difficulty));
            practice.putClientProperty("secondaryAction", true);
            actions.add(practice);
        }
        actions.setPreferredSize(new Dimension(0, 44));
        challenge.add(actions, BorderLayout.SOUTH);
        world.add(challenge, BorderLayout.SOUTH);
    }
    private void showRescueRequirement() {
        String message = "과학자·한국사·아이돌 분야에서 서로 다른 문제 "
            + PlayerProgress.PROFESSOR_REQUIRED + "개를 맞히면 열립니다. ("
            + feature.profile().rescueProgress() + " / " + PlayerProgress.PROFESSOR_REQUIRED + ")";
        notice.setText(message);
        JOptionPane.showMessageDialog(this, message, "교수님 구출 조건", JOptionPane.INFORMATION_MESSAGE);
    }

    private Optional<BufferedImage> villageArt(String village) {
        String key = switch (village) {
            case "과학자" -> "science";
            case "한국사" -> "history";
            case "아이돌" -> "idol";
            default -> "professor";
        };
        return GameTheme.asset("village-" + key).or(() -> feature.image("ui/" + village));
    }

    private void buildShop() {
        shop.removeAll(); shop.add(info("상점", "승리로 얻은 코인으로 다음 시련을 준비하세요. 각 아이템은 전투마다 한 번씩 사용할 수 있습니다."));
        for (ShopItem item : ShopItem.values()) {
            JPanel row = card(item.label() + "  /  " + item.price() + " COIN", item.description() + " · 보유 " + feature.profile().inventory(item), button("구매하기", "buy" + item.name(), () -> { feature.buy(item); notice.setText(item.label() + " 구매 완료"); refresh(); }));
            row.add(new ShopItemIcon(item), BorderLayout.WEST);
            shop.add(row);
        }
        shop.add(Box.createVerticalGlue());
    }
    private void buildAchievements() {
        achievements.removeAll(); achievements.add(info("도전과제", "완료한 도전의 보상을 수령하세요. 수령한 보상은 다시 지급되지 않습니다."));
        for (Achievement achievement : Achievement.values()) {
            PlayerProgress p = feature.profile(); boolean claimed = achievement.claimed(p), unlocked = achievement.unlocked(p);
            JButton claim = button(claimed ? "수령 완료" : unlocked ? "보상 받기" : "도전 중", "claim" + achievement.name(), () -> { feature.claim(achievement); notice.setText(achievement.title() + " · 코인 +" + achievement.reward()); refresh(); });
            claim.setEnabled(unlocked && !claimed);
            JPanel row = card(achievement.title(), achievement.description() + "  /  " + achievement.reward() + " COIN", claim);
            row.add(new Sigil(null, unlocked ? 3 : 4), BorderLayout.WEST); achievements.add(row);
        }
        achievements.add(Box.createVerticalGlue());
    }
    private void buildDeck() {
        deck.removeAll(); deck.add(info("지원 카드", "수집한 카드 한 장을 장착하세요. 인물 고유 버프와 단계, 숙련도가 전투에 적용됩니다."));
        List<Person> owned = feature.ownedCards();
        if (owned.isEmpty()) deck.add(info("아직 동료가 없습니다", "인물 도전에서 승리해 첫 카드를 획득하세요. 연습에서는 카드나 성장 보상을 얻지 않습니다."));
        for (Person person : owned) {
            CardPower power = feature.cardPower(person); boolean equipped = feature.profile().equipped().equals(CardPower.key(person));
            JButton equip = button(equipped ? "함께하는 중" : "지원 카드 장착", "equip" + person.getName(), () -> { feature.equip(person); notice.setText(power.title() + " 장착 완료"); refresh(); }); equip.setEnabled(!equipped);
            JPanel row = card(power.title() + (feature.profile().keys("card.new." + CardPower.key(person) + "|").stream().anyMatch(feature.profile()::flag) ? " · NEW" : ""), power.description() + " · 숙련도 " + feature.profile().mastery(CardPower.key(person)) + " · 강화 " + feature.profile().masteryLevel(CardPower.key(person)) + "/5", equip);
            JPanel insignia = GameTheme.clear(new BorderLayout(8, 0));
            JPanel preview = GameTheme.clear(new BorderLayout(0, 4));
            CardTile tile = new CardTile(new CardView(person, feature.cardStage(person), new CardArt(feature::image)), () -> false, equipped);
            tile.setBadgesVisible(false); tile.setPreferredSize(new Dimension(70, 84));
            tile.setEnabled(!equipped); tile.setToolTipText(person.getName() + " · " + power.description());
            tile.getAccessibleContext().setAccessibleName(person.getName() + " 지원 카드 장착");
            tile.addActionListener(e -> equip.doClick()); preview.add(tile, BorderLayout.CENTER);
            JProgressBar mastery = new JProgressBar(0, 15);
            mastery.setValue(Math.min(15, feature.profile().mastery(CardPower.key(person))));
            mastery.setToolTipText("숙련도 강화 " + feature.profile().masteryLevel(CardPower.key(person)) + "/5");
            mastery.setPreferredSize(new Dimension(70, 7)); preview.add(mastery, BorderLayout.SOUTH);
            insignia.add(preview, BorderLayout.CENTER);
            insignia.add(new Sigil("buff-" + power.kind().name().toLowerCase(Locale.ROOT), 3), BorderLayout.EAST); row.add(insignia, BorderLayout.WEST); deck.add(row);
        }
        deck.add(Box.createVerticalGlue());
    }
    private void buildReview() {
        review.removeAll(); review.add(info("오답 노트", "틀린 문제를 맞히면 오답 노트에서 지워집니다. 복습 정답도 교수님 구출 해금에 반영되며 같은 문제는 중복 집계되지 않습니다."));
        review.add(card("남겨진 질문 " + feature.profile().wrongQuestions().size() + "개", "최대 10문제 · 잠긴 교수님 분야는 해금 이후에 출제됩니다.", button("오답 재도전", "startReview", feature::startReview)));
        review.add(info("학습 기록", "정답 " + feature.profile().number("correct") + " / " + feature.profile().number("attempts") + " · 최고 콤보 " + feature.profile().number("bestCombo") + " · 배틀 승리 " + feature.profile().number("wins")));
        review.add(Box.createVerticalGlue());
    }
    private JButton button(String text, String name, AdventureGUIEvent.Command command) {
        JButton button = new UiButton(text, GameTheme.TEAL);
        button.setName(name);
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 17f));
        button.addActionListener(event.action(notice, command));
        return button;
    }
    private static JPanel rows() { JPanel panel = GameTheme.clear(null); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); return panel; }
    private static final class ShopRows extends JPanel implements Scrollable {
        ShopRows() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle area, int orientation, int direction) { return 22; }
        @Override public int getScrollableBlockIncrement(Rectangle area, int orientation, int direction) {
            return Math.max(22, area.height - 22);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static JLabel heading(String text, float size) { JLabel label = GameTheme.label(text, size, ComponentPainter.TEXT); label.setFont(label.getFont().deriveFont(Font.BOLD)); return label; }
    private static JPanel info(String title, String description) { return card(title, description, null); }
    private static JPanel card(String title, String description, JButton action) {
        JPanel outer = GameTheme.clear(new BorderLayout()); outer.setAlignmentX(Component.LEFT_ALIGNMENT); outer.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JPanel surface = GameTheme.lightPanel(new BorderLayout(15, 10)); surface.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel text = GameTheme.clear(new BorderLayout(0, 8)); text.add(heading(title, 21), BorderLayout.NORTH);
        JTextArea body = new JTextArea(description); body.setLineWrap(true); body.setWrapStyleWord(true); body.setEditable(false); body.setFocusable(false); body.setOpaque(false); body.setForeground(ComponentPainter.MUTED); body.setFont(UiStyle.FONT.deriveFont(16f)); body.setRows(1); text.add(body);
        JPanel centered = GameTheme.clear(new GridBagLayout());
        GridBagConstraints position = new GridBagConstraints();
        position.weightx = 1;
        position.fill = GridBagConstraints.HORIZONTAL;
        centered.add(text, position);
        surface.add(centered, BorderLayout.CENTER);
        if (action != null) {
            JPanel buttonSlot = GameTheme.clear(new GridBagLayout());
            action.setPreferredSize(new Dimension(160, 44));
            buttonSlot.add(action);
            surface.add(buttonSlot, BorderLayout.EAST);
        }
        outer.add(surface); outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155)); outer.setPreferredSize(new Dimension(850, 145));
        return new AdventureCardRow(outer, surface);
    }
    @Override public ViewId getViewId() { return ViewId.ADVENTURE; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }
    @Override protected void paintComponent(Graphics source) {
        GameTheme.paintBackdrop(source, this, activeTab.equals("shop") ? "shop" : activeTab.equals("world") ? "adventure" : "guild", 0.4f);
    }
}
