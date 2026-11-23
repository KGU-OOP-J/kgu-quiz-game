package org.kgu.menu.view;

import org.kgu.application.Difficulty;
import org.kgu.game.feature.GameSession;
import org.kgu.model.Person;
import org.kgu.window.UiButton;
import org.kgu.window.UiStyle;
import org.kgu.window.View;
import org.kgu.window.ViewId;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 원본의 메인 메뉴와 게임 준비를 하나의 MENU View 안에서 전환한다.
 */
public final class MenuView extends JPanel implements View {
    private final CardLayout layout = new CardLayout();
    private final JPanel home = new JPanel(null);
    private final JPanel setup = new JPanel(null);
    private final JButton game = button("게임 시작", new Color(35, 133, 246));
    private final JButton collection = button("카드 도감", new Color(239, 151, 43));
    private final JButton setting = button("설정", new Color(30, 172, 111));
    private final JButton start = button("게임 시작", new Color(33, 127, 239));
    private final JButton back = button("메인으로", new Color(112, 128, 144));
    private final JComboBox<String> category = new JComboBox<>(new String[]{"과학자", "교수님", "한국사", "아이돌"});
    private final JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());
    private final JComboBox<GameSession.Mode> mode = new JComboBox<>(GameSession.Mode.values());
    private final JComboBox<Person> person = new JComboBox<>();
    private final JLabel personLabel = new JLabel("배틀 인물");
    private final JLabel welcome = new JLabel("무엇을 해볼까요?", SwingConstants.CENTER);

    public MenuView() {
        setLayout(layout);
        home.setBackground(new Color(239, 249, 250));
        setup.setBackground(new Color(245, 248, 252));
        welcome.setBounds(0, 70, 780, 55);
        welcome.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 30f));
        home.add(welcome);
        game.setBounds(55, 210, 200, 255);
        collection.setBounds(290, 210, 200, 255);
        setting.setBounds(525, 210, 200, 255);
        home.add(game);
        home.add(collection);
        home.add(setting);
        JLabel title = new JLabel("게임 준비", SwingConstants.CENTER);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 34f));
        title.setBounds(0, 35, 780, 60);
        setup.add(title);
        addChoice("분야 선택", category, 115);
        addChoice("난이도 선택", difficulty, 185);
        addChoice("게임 모드", mode, 255);
        personLabel.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f));
        personLabel.setBounds(180, 325, 180, 42);
        setup.add(personLabel);
        person.setFont(UiStyle.FONT);
        person.setBounds(350, 325, 250, 42);
        setup.add(person);
        JLabel rules = new JLabel("대진: 6턴 · 공격 정답 -20 / 방어 오답 -20", SwingConstants.CENTER);
        rules.setBounds(90, 390, 600, 30);
        rules.setFont(UiStyle.FONT.deriveFont(16f));
        setup.add(rules);
        start.setBounds(395, 465, 240, 58);
        back.setBounds(145, 465, 220, 58);
        setup.add(start);
        setup.add(back);
        add(home, "home");
        add(setup, "setup");
        showHome();
    }

    private static JButton button(String text, Color color) {
        UiButton b = new UiButton(text, color);
        b.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 24f));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void addChoice(String text, JComboBox<?> box, int y) {
        JLabel l = new JLabel(text);
        l.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 19f));
        l.setBounds(180, y, 180, 42);
        setup.add(l);
        box.setBounds(350, y, 250, 42);
        box.setFont(UiStyle.FONT);
        setup.add(box);
    }

    public void setPlayerId(String id) {
        welcome.setText(id + "님, 무엇을 해볼까요?");
    }

    public void showHome() {
        layout.show(this, "home");
    }

    public void showSetup() {
        layout.show(this, "setup");
        updateMode();
    }

    public void updateMode() {
        boolean battle = getMode() == GameSession.Mode.BATTLE;
        person.setVisible(battle);
        personLabel.setVisible(battle);
    }

    public void setPersons(List<Person> persons) {
        person.setModel(new DefaultComboBoxModel<>(persons.toArray(new Person[0])));
    }

    public String getCategory() {
        return (String) category.getSelectedItem();
    }

    public Difficulty getDifficulty() {
        return (Difficulty) difficulty.getSelectedItem();
    }

    public GameSession.Mode getMode() {
        return (GameSession.Mode) mode.getSelectedItem();
    }

    public Person getSelectedPerson() {
        return (Person) person.getSelectedItem();
    }

    public JButton getGameButton() {
        return game;
    }

    public JButton getCollectionButton() {
        return collection;
    }

    public JButton getSettingButton() {
        return setting;
    }

    public JButton getStartButton() {
        return start;
    }

    public JButton getBackButton() {
        return back;
    }

    public JComboBox<String> getCategoryBox() {
        return category;
    }

    public JComboBox<GameSession.Mode> getModeBox() {
        return mode;
    }

    @Override
    public ViewId getViewId() {
        return ViewId.MENU;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Dimension getViewSize() {
        return new Dimension(UiStyle.WIDTH, UiStyle.HEIGHT);
    }
}
