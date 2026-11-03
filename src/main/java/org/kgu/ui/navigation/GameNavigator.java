package org.kgu.ui.navigation;

import javax.swing.*;
import org.kgu.game.GameMenuItem;
import org.kgu.game.PlayerSession;
import org.kgu.game.GameOutcome;
import org.kgu.mgr.Manager;
import org.kgu.ui.collection.CollectionScreen;
import org.kgu.ui.question.QuestionScreen;
import org.kgu.ui.start.GameSetupScreen;
import org.kgu.ui.start.LoginScreen;
import org.kgu.ui.start.MenuScreen;
import org.kgu.ui.settings.SettingsScreen;
import org.kgu.ui.answer.AnswerResultScreen;
import org.kgu.ui.battle.BattleScreen;

/** 창과 화면 흐름만 담당하며, 메뉴 데이터 관리는 mgr.Manager에 위임한다. */
public final class GameNavigator {
    private final JFrame frame = new JFrame("교수님 이름 맞추기");
    private final Manager menuManager = new Manager();
    private final PlayerSession session = new PlayerSession();

    public GameNavigator() {
        menuManager.add(new GameMenuItem("game", "게임 시작"));
        menuManager.add(new GameMenuItem("collection", "카드 도감"));
        menuManager.add(new GameMenuItem("settings", "설정"));
    }
    public void open() { frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.setResizable(false); showLogin(); frame.setLocationRelativeTo(null); frame.setVisible(true); }
    public void showLogin() { show(new LoginScreen(this)); }
    public void showMenu() { show(new MenuScreen(this, menuManager)); }
    public PlayerSession getSession() { return session; }
    public void showGameSetup() { show(new GameSetupScreen(this)); }
    public void showQuestion() { show(new QuestionScreen(this)); }
    public void showBattle() { show(new BattleScreen(this)); }
    public void showCollection() { show(new CollectionScreen(this)); }
    public void showSettings() { show(new SettingsScreen(this)); }
    public void showAnswerResult(GameOutcome outcome) { show(new AnswerResultScreen(this, outcome)); }
    /** Each screen owns its preferred size; repack prevents unused blank canvas. */
    private void show(JPanel panel) {
        frame.setContentPane(panel);
        frame.pack();
        frame.revalidate();
        frame.repaint();
    }
}
