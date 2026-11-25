package org.kgu.menu.view;

import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public final class MenuView extends JPanel implements View {
    private final CardLayout layout = new CardLayout();
    private final JPanel home = new JPanel(new BorderLayout(24, 24)) {
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            UiStyle.prepare(g); GameTheme.backdrop(g, getWidth(), getHeight(), "menu", 0.38f); g.dispose();
        }
    };
    private final JPanel setup = GameTheme.background("menu", new GridBagLayout());
    private final JButton game = button("게임 시작", GameTheme.TEAL), collection = button("카드 도감", new Color(133, 101, 52)), setting = button("설정", new Color(52, 93, 98));
    private final JButton login = button("로그인으로", new Color(44, 55, 72)), start = button("게임 시작", GameTheme.TEAL), back = button("메뉴로", new Color(44, 55, 72));
    private final JComboBox<String> category = new JComboBox<>(new String[]{"과학자", "한국사", "아이돌", "교수님"});
    private final JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
    private final JComboBox<GameSession.Mode> mode = new JComboBox<>(GameSession.Mode.values());
    private final JComboBox<Person> person = new JComboBox<>();
    private final JLabel personLabel = GameTheme.label("배틀 상대 선택", 19, GameTheme.MUTED);
    private final JLabel welcome = GameTheme.label("마지막 학기의 모험", 38, GameTheme.PAPER);
    private final JLabel progress = GameTheme.label("", 21, GameTheme.GOLD), goal = GameTheme.label("", 18, GameTheme.MUTED), gate = GameTheme.label("", 17, GameTheme.GOLD);
    private final JButton world = button("도전 준비", GameTheme.TEAL), shop = button("상점", new Color(133, 101, 52)), achievements = button("도전과제", new Color(96, 72, 120)), deck = button("카드 장착", GameTheme.TEAL), review = button("오답 노트", new Color(49, 77, 103)), tutorial = button("튜토리얼", new Color(44, 55, 72));

    public MenuView() { setLayout(layout); buildHome(); buildSetup(); add(home, "home"); add(setup, "setup"); showHome(); }
    private void buildHome() {
        home.setBorder(BorderFactory.createEmptyBorder(48, 64, 42, 64));
        JPanel account = GameTheme.clear(new BorderLayout());
        welcome.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 22f));
        account.add(welcome); account.add(quiet(login), BorderLayout.EAST);
        login.setName("returnToLogin"); home.add(account, BorderLayout.NORTH);

        JPanel anchor = GameTheme.clear(new GridBagLayout());
        JPanel story = GameTheme.panel(new BorderLayout(0, 28));
        story.setBorder(BorderFactory.createEmptyBorder(36, 36, 32, 36));
        story.setPreferredSize(new Dimension(540, 600));
        JPanel heading = GameTheme.clear(null);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
        heading.add(GameTheme.label("인물 퀴즈 어드벤처", 18, GameTheme.MUTED));
        heading.add(Box.createVerticalStrut(16));
        JLabel name = GameTheme.label("WHO’S NEXT?", 48, GameTheme.PAPER);
        name.setFont(name.getFont().deriveFont(Font.BOLD)); heading.add(name); heading.add(Box.createVerticalStrut(12));
        heading.add(GameTheme.label("사라진 교수님을 찾아서", 25, GameTheme.PAPER));
        story.add(heading, BorderLayout.NORTH);
        JPanel objective = GameTheme.clear(new BorderLayout(0, 14));
        JTextArea premise = new JTextArea("졸업을 앞두고 교수님이 사라졌다.\n마을의 인물들과 퀴즈로 겨루고,\n교수님에게 가는 길을 열어 보세요.");
        premise.setEditable(false); premise.setFocusable(false); premise.setOpaque(false);
        premise.setForeground(GameTheme.PAPER); premise.setFont(UiStyle.FONT.deriveFont(21f));
        objective.add(premise, BorderLayout.NORTH);
        goal.setFont(UiStyle.FONT.deriveFont(17f)); objective.add(goal, BorderLayout.SOUTH);
        story.add(objective);
        JPanel choices = GameTheme.clear(new BorderLayout(0, 12));
        collection.setText("모험 시작  →"); collection.setName("startAdventure");
        collection.putClientProperty("selectionTint", new Color(43, 104, 165));
        collection.setPreferredSize(new Dimension(420, 58)); choices.add(collection, BorderLayout.NORTH);
        JPanel secondary = GameTheme.clear(new GridLayout(1, 2, 16, 0));
        world.setText("도전 준비 · 상점"); world.setName("openWorld"); secondary.add(quiet(world));
        game.setText("빠른 퀴즈"); secondary.add(quiet(game)); choices.add(secondary, BorderLayout.SOUTH);
        story.add(choices, BorderLayout.SOUTH);
        GridBagConstraints position = new GridBagConstraints(); position.weightx = 1; position.weighty = 1;
        position.anchor = GridBagConstraints.WEST; anchor.add(story, position); home.add(anchor);

        JPanel footer = GameTheme.clear(new BorderLayout(20, 0));
        progress.setFont(UiStyle.FONT.deriveFont(17f)); progress.setForeground(GameTheme.PAPER); footer.add(progress);
        JPanel utilities = GameTheme.clear(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        tutorial.setText("도움말"); tutorial.setName("openTutorial");
        utilities.add(quiet(tutorial)); utilities.add(quiet(setting)); footer.add(utilities, BorderLayout.EAST);
        home.add(footer, BorderLayout.SOUTH);
    }
    private static JButton quiet(JButton original) {
        // Preserve the existing event-bound button and give secondary navigation a neutral tint.
        original.putClientProperty("selectionTint", new Color(35, 52, 73));
        original.setPreferredSize(new Dimension(180, 42));
        original.setFont(UiStyle.FONT.deriveFont(16f));
        return original;
    }
    private void buildSetup() {
        JPanel frame = GameTheme.panel(new BorderLayout(24, 28)); frame.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));
        frame.setPreferredSize(new Dimension(760, 570));
        JPanel heading = GameTheme.clear(new GridLayout(2, 1, 0, 12)); heading.add(GameTheme.label("분야와 난이도를 선택하세요", 17, GameTheme.GOLD)); heading.add(GameTheme.label("빠른 퀴즈", 34, GameTheme.PAPER)); frame.add(heading, BorderLayout.NORTH);
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
    public void setPlayerId(String id) { welcome.setText(id + "님, 안녕하세요"); }
    public void showHome() { layout.show(this, "home"); }
    public void showSetup() { layout.show(this, "setup"); updateMode(); }
    public void updateMode() { boolean battle = getMode() == GameSession.Mode.BATTLE; person.setVisible(battle); personLabel.setVisible(battle); }
    public void setPersons(List<Person> people) { person.setModel(new DefaultComboBoxModel<>(people.toArray(Person[]::new))); }
    public void setCategoryAccess(boolean available, int solved) {
        boolean professor = getCategory().equals("교수님"); mode.setEnabled(!professor);
        if (professor && getMode() != GameSession.Mode.BATTLE) mode.setSelectedItem(GameSession.Mode.BATTLE);
        start.setEnabled(available); start.setText(professor ? "이은정 교수님 구출" : "게임 시작");
        gate.setText(available ? professor ? "최종 구출 경로 개방 · 이은정 교수님" : "연습으로 배우고, 배틀에서 5문제에 도전하세요." : "다른 분야의 서로 다른 문제 " + solved + "/30개 정답으로 해금"); updateMode();
    }
    public void configureAdventure(java.util.function.Consumer<String> open, Runnable showTutorial) {
        world.addActionListener(e -> open.accept("world")); shop.addActionListener(e -> open.accept("shop")); achievements.addActionListener(e -> open.accept("achievements")); deck.addActionListener(e -> open.accept("deck")); review.addActionListener(e -> open.accept("review")); tutorial.addActionListener(e -> showTutorial.run());
    }
    public void showProgress(org.kgu.progression.PlayerProgress p, String nextGoal) { progress.setText("Lv." + p.level() + "  /  XP " + p.levelExperience() + " · " + org.kgu.progression.PlayerProgress.experienceForLevel(p.level()) + "  /  " + p.coins() + " COIN  /  " + p.totalStars() + " STARS"); goal.setText("<html>" + nextGoal + "</html>"); }
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
    public void stopAnimations() { }
    @Override public void removeNotify() { stopAnimations(); super.removeNotify(); }
    @Override public ViewId getViewId() { return ViewId.MENU; }
    @Override public JComponent getComponent() { return this; }
    @Override public Dimension getViewSize() { return new Dimension(1280, 960); }

}
