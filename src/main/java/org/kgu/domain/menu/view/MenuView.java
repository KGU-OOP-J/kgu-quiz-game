package org.kgu.domain.menu.view;

import org.kgu.core.application.Difficulty;
import org.kgu.core.ui.ComponentPainter;
import org.kgu.core.progression.PlayerProgress;
import org.kgu.domain.game.feature.GameSession;
import org.kgu.core.model.Person;
import org.kgu.core.window.*;
import javax.swing.*;
import java.awt.*;
import org.kgu.domain.menu.view.component.MenuCard;
import org.kgu.domain.menu.view.layout.MenuHomeLayout;
import org.kgu.domain.menu.view.layout.MenuSetupLayout;
import java.util.ArrayList;
import java.util.List;

public final class MenuView extends JPanel implements View {
    private final CardLayout layout = new CardLayout();
    private final MenuHomeLayout home;
    private final List<MenuCard> menuCards = new ArrayList<>();
    private final JButton game = button("게임 시작", true), collection = button("카드 도감", false), setting = button("설정", false);
    private final JButton login = button("로그인으로", false), start = button("게임 시작", true), back = button("메뉴로", false);
    private final JComboBox<String> category = new JComboBox<>(new String[]{"과학자", "한국사", "아이돌", "교수님"});
    private final JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
    private final JComboBox<GameSession.Mode> mode = new JComboBox<>(GameSession.Mode.values());
    private final JComboBox<Person> person = new JComboBox<>();
    private final JLabel personLabel = GameTheme.label("배틀 상대 선택", 16, ComponentPainter.MUTED);
    private final JLabel welcome = GameTheme.label("", 16, GameTheme.BLUE_TEXT);
    private final JLabel goal = GameTheme.label("", 16, ComponentPainter.MUTED);
    private final JLabel gate = GameTheme.label("", 16, ComponentPainter.MUTED);
    private final List<JLabel> progress = List.of(metric(), metric(), metric(), metric());
    private final JButton world = button("마을 탐험", false);
    private final JButton tutorial = button("튜토리얼 다시 보기", false);

    public MenuView() {
        setLayout(layout);
        var primaryCards = List.of(
            new MenuCard("지식의 시련", "답을 고르고 전략을 세워\n사라진 교수님을 찾아가세요.", MenuCard.Symbol.QUIZ, game),
            new MenuCard("동료의 기록", "수집한 인물 카드를 펼치고\n함께할 동료를 확인하세요.", MenuCard.Symbol.CARDS, collection),
            new MenuCard("마을 탐험", "지도에서 다음 도전을 고르고\n상점과 오답 노트를 이용하세요.", MenuCard.Symbol.MAP, world));
        menuCards.addAll(primaryCards);
        world.setName("openWorld");
        login.setName("returnToLogin");
        home = new MenuHomeLayout(welcome, progress, goal, primaryCards, setting, tutorial, login);
        add(home, "home");
        add(new MenuSetupLayout(List.of(category, difficulty, mode, person), personLabel, gate, back, start), "setup");
        showHome();
    }

    private static JButton button(String text, boolean primary) {
        JButton button = new UiButton(text, GameTheme.TEAL);
        button.putClientProperty("secondaryAction", !primary);
        button.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 16f));
        return button;
    }

    private static JLabel metric() {
        JLabel label = GameTheme.label("", 20, ComponentPainter.TEXT);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        return label;
    }
    public void setPlayerId(String id) { welcome.setText(id + "님, 환영합니다."); }
    public void showHome() { layout.show(this, "home"); SwingUtilities.invokeLater(() -> { if (home.isVisible()) for (int i = 0; i < menuCards.size(); i++) menuCards.get(i).appear(i * 80); }); }
    public void showSetup() { layout.show(this, "setup"); updateMode(); }
    public void updateMode() { boolean battle = getMode() == GameSession.Mode.BATTLE; person.setVisible(battle); personLabel.setVisible(battle); }
    public void setPersons(List<Person> people) { person.setModel(new DefaultComboBoxModel<>(people.toArray(Person[]::new))); }
    public void setCategoryAccess(boolean available, int solved) {
        boolean professor = getCategory().equals("교수님"); mode.setEnabled(!professor);
        if (professor && getMode() != GameSession.Mode.BATTLE) mode.setSelectedItem(GameSession.Mode.BATTLE);
        start.setEnabled(available); start.setText(professor ? "이은정 교수님 구출" : "게임 시작");
        gate.setText(available ? professor ? "최종 구출 경로 개방 · 이은정 교수님" : "분야와 난이도, 플레이 방식을 선택하세요." : "다른 분야의 서로 다른 문제 " + solved + "/30개 정답으로 해금"); updateMode();
    }
    public void configureAdventure(java.util.function.Consumer<String> open, Runnable showTutorial) {
        world.addActionListener(e -> open.accept("world"));
        tutorial.addActionListener(e -> showTutorial.run());
    }
    public void showProgress(PlayerProgress p, String nextGoal) {
        progress.get(0).setText(Integer.toString(p.level()));
        progress.get(1).setText(p.levelExperience() + " / " + PlayerProgress.experienceForLevel(p.level()));
        progress.get(2).setText(Integer.toString(p.coins()));
        progress.get(3).setText(Integer.toString(p.totalStars()));
        goal.setText(nextGoal);
    }
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

}
