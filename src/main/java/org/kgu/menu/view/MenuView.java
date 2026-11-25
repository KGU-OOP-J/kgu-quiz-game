package org.kgu.menu.view;

import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public final class MenuView extends JPanel implements View {
    private final CardLayout layout = new CardLayout();
    private final JPanel home = GameTheme.background("menu", new BorderLayout(24, 24));
    private final JPanel setup = GameTheme.background("menu", new GridBagLayout());
    private final List<MenuCard> menuCards = new ArrayList<>();
    private final JButton game = button("게임 시작", GameTheme.TEAL), collection = button("카드 도감", new Color(133, 101, 52)), setting = button("설정", new Color(52, 93, 98));
    private final JButton login = button("로그인으로", new Color(44, 55, 72)), start = button("게임 시작", GameTheme.TEAL), back = button("메뉴로", new Color(44, 55, 72));
    private final JComboBox<String> category = new JComboBox<>(new String[]{"과학자", "한국사", "아이돌", "교수님"});
    private final JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
    private final JComboBox<GameSession.Mode> mode = new JComboBox<>(GameSession.Mode.values());
    private final JComboBox<Person> person = new JComboBox<>();
    private final JLabel personLabel = GameTheme.label("배틀 상대 선택", 19, GameTheme.MUTED);
    private final JLabel welcome = GameTheme.label("마지막 학기의 모험", 38, GameTheme.PAPER);
    private final JLabel progress = GameTheme.label("", 21, GameTheme.GOLD), goal = GameTheme.label("", 18, GameTheme.MUTED), gate = GameTheme.label("", 17, GameTheme.GOLD);
    private final JButton world = button("마을 지도", GameTheme.TEAL), shop = button("상점", new Color(133, 101, 52)), achievements = button("도전과제", new Color(96, 72, 120)), deck = button("카드 장착", GameTheme.TEAL), review = button("오답 노트", new Color(49, 77, 103)), tutorial = button("튜토리얼", new Color(44, 55, 72));

    public MenuView() { setLayout(layout); buildHome(); buildSetup(); add(home, "home"); add(setup, "setup"); showHome(); }
    private void buildHome() {
        home.setBorder(BorderFactory.createEmptyBorder(45, 65, 40, 65));
        JPanel header = GameTheme.clear(new GridLayout(4, 1, 0, 9));
        header.add(GameTheme.label("THE LAST SEMESTER  /  졸업을 향한 마지막 여정", 17, GameTheme.GOLD));
        header.add(welcome); header.add(progress); header.add(goal); home.add(header, BorderLayout.NORTH);
        JPanel cards = GameTheme.clear(new GridLayout(1, 3, 23, 0));
        cards.add(tile(game, "지식의 시련", "답을 고르고 전략을 세워\n사라진 교수님을 찾아가세요.", "⚔"));
        cards.add(tile(collection, "동료의 기록", "수집한 인물 카드를 펼치고\n함께할 동료를 확인하세요.", "★"));
        cards.add(tile(setting, "여정의 설정", "효과음과 게임 환경을\n원하는 대로 준비하세요.", "⚙")); home.add(cards);
        JPanel footer = GameTheme.clear(new GridLayout(2, 1, 0, 20));
        JPanel quick = GameTheme.clear(new GridLayout(1, 6, 12, 0));
        JButton[] buttons = {world, shop, achievements, deck, review, tutorial};
        String[] names = {"openWorld", "openShop", "openAchievements", "openDeck", "openReview", "openTutorial"};
        for (int i = 0; i < buttons.length; i++) { buttons[i].setName(names[i]); buttons[i].setPreferredSize(new Dimension(145, 50)); quick.add(buttons[i]); }
        footer.add(quick); JPanel account = GameTheme.clear(new FlowLayout(FlowLayout.LEFT, 0, 0)); login.setName("returnToLogin"); login.setPreferredSize(new Dimension(180, 44)); account.add(login); footer.add(account); home.add(footer, BorderLayout.SOUTH);
    }
    private MenuCard tile(JButton button, String title, String description, String icon) { MenuCard card = new MenuCard(title, description, icon, button); menuCards.add(card); return card; }
    private void buildSetup() {
        JPanel frame = GameTheme.panel(new BorderLayout(24, 28)); frame.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));
        frame.setPreferredSize(new Dimension(760, 570));
        JPanel heading = GameTheme.clear(new GridLayout(2, 1, 0, 12)); heading.add(GameTheme.label("PREPARE FOR THE TRIAL", 17, GameTheme.GOLD)); heading.add(GameTheme.label("게임 준비", 34, GameTheme.PAPER)); frame.add(heading, BorderLayout.NORTH);
        JPanel fields = GameTheme.clear(new GridLayout(4, 2, 20, 18));
        fields.add(GameTheme.label("분야 선택", 19, GameTheme.MUTED)); fields.add(category);
        fields.add(GameTheme.label("난이도 선택", 19, GameTheme.MUTED)); fields.add(difficulty);
        fields.add(GameTheme.label("게임 모드", 19, GameTheme.MUTED)); fields.add(mode);
        fields.add(personLabel); fields.add(person);
        for (JComboBox<?> box : new JComboBox<?>[]{category, difficulty, mode, person}) box.setFont(UiStyle.FONT.deriveFont(19f)); frame.add(fields);
        JPanel footer = GameTheme.clear(new BorderLayout(0, 20)); gate.setHorizontalAlignment(SwingConstants.CENTER); footer.add(gate, BorderLayout.NORTH);
        JPanel actions = GameTheme.clear(new GridLayout(1, 2, 20, 0)); back.setPreferredSize(new Dimension(220, 54)); actions.add(back); actions.add(start); footer.add(actions, BorderLayout.SOUTH); frame.add(footer, BorderLayout.SOUTH); setup.add(frame);
    }
    private static JButton button(String text, Color color) { JButton button = new UiButton(text, color); button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f)); return button; }
    public void setPlayerId(String id) { welcome.setText(id + "님, 여정을 이어가세요."); }
    public void showHome() { layout.show(this, "home"); SwingUtilities.invokeLater(() -> { if (home.isVisible()) for (int i = 0; i < menuCards.size(); i++) menuCards.get(i).appear(i * 80); }); }
    public void showSetup() { layout.show(this, "setup"); updateMode(); }
    public void updateMode() { boolean battle = getMode() == GameSession.Mode.BATTLE; person.setVisible(battle); personLabel.setVisible(battle); }
    public void setPersons(List<Person> people) { person.setModel(new DefaultComboBoxModel<>(people.toArray(Person[]::new))); }
    public void setCategoryAccess(boolean available, int solved) {
        boolean professor = getCategory().equals("교수님"); mode.setEnabled(!professor);
        if (professor && getMode() != GameSession.Mode.BATTLE) mode.setSelectedItem(GameSession.Mode.BATTLE);
        start.setEnabled(available); start.setText(professor ? "이은정 교수님 구출" : "게임 시작");
        gate.setText(available ? professor ? "최종 구출 경로 개방 · 이은정 교수님" : "상대의 다음 행동을 보고 전략을 선택하세요." : "다른 분야의 서로 다른 문제 " + solved + "/30개 정답으로 해금"); updateMode();
    }
    public void configureAdventure(java.util.function.Consumer<String> open, Runnable showTutorial) {
        world.addActionListener(e -> open.accept("world")); shop.addActionListener(e -> open.accept("shop")); achievements.addActionListener(e -> open.accept("achievements")); deck.addActionListener(e -> open.accept("deck")); review.addActionListener(e -> open.accept("review")); tutorial.addActionListener(e -> showTutorial.run());
    }
    public void showProgress(org.kgu.progression.PlayerProgress p, String nextGoal) { progress.setText("Lv." + p.level() + "  /  XP " + p.levelExperience() + " · " + org.kgu.progression.PlayerProgress.experienceForLevel(p.level()) + "  /  " + p.coins() + " COIN  /  " + p.totalStars() + " STARS"); goal.setText(nextGoal); }
    public String getCategory() { return (String)category.getSelectedItem(); }
    public Difficulty getDifficulty() { return (Difficulty)difficulty.getSelectedItem(); }
    public GameSession.Mode getMode() { return (GameSession.Mode)mode.getSelectedItem(); }
    public Person getSelectedPerson() { return (Person)person.getSelectedItem(); }
    public JButton getGameButton() { return game; }
    public JButton getCollectionButton() { return collection; }
    public JButton getSettingButton() { return setting; }
    public JButton getLoginButton() { return login; }
    public JButton getStartButton() { return start; }
    public JButton getBackButton() { return back; }
    public JComboBox<String> getCategoryBox() { return category; }
    public JComboBox<GameSession.Mode> getModeBox() { return mode; }
    public void stopAnimations() { for (MenuCard card : menuCards) card.stop(); }
    @Override public void removeNotify() { stopAnimations(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.MENU; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }

    private static final class MenuCard extends JPanel {
        private final String title, description, icon;
        private final JButton button;
        private float alpha = 1;
        private boolean hover;
        private Timer timer;
        MenuCard(String title, String description, String icon, JButton button) {
            this.title = title; this.description = description; this.icon = icon; this.button = button; setLayout(null); setOpaque(false); add(button);
            MouseAdapter mouse = new MouseAdapter() { @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); } @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); } };
            addMouseListener(mouse); button.addMouseListener(mouse);
        }
        @Override public void doLayout() { button.setBounds(25, getHeight() - 80, Math.max(30, getWidth() - 50), 52); }
        void appear(int delay) { stop(); alpha = 0; timer = new Timer(16, e -> { alpha = Math.min(1, alpha + 0.075f); repaint(); if (alpha >= 1) ((Timer)e.getSource()).stop(); }); timer.setInitialDelay(delay); timer.start(); }
        void stop() { if (timer != null) timer.stop(); }
        @Override protected void paintComponent(Graphics source) {
            Graphics2D g = (Graphics2D)source.create(); UiStyle.prepare(g); g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            GameTheme.paintSurface(g, 0, 0, getWidth(), getHeight(), 24, hover); int cx = getWidth() / 2;
            g.setColor(GameTheme.GOLD); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 60f)); UiStyle.center(g, icon, cx, Math.min(115, getHeight() / 3));
            g.setColor(GameTheme.PAPER); g.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 27f)); UiStyle.center(g, title, cx, Math.min(190, getHeight() / 2));
            g.setColor(GameTheme.MUTED); g.setFont(UiStyle.FONT.deriveFont(16f)); String[] lines = description.split("\n");
            for (int i = 0; i < lines.length; i++) UiStyle.center(g, lines[i], cx, Math.min(235, getHeight() / 2 + 40) + i * 26); g.dispose();
        }
    }
}
