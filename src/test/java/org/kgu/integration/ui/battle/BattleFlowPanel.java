package org.kgu.integration.ui.battle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.*;
import org.kgu.domain.battle.feature.BattleEngine;
import org.kgu.domain.battle.view.component.BattleSetupScreen;
import org.kgu.domain.battle.view.component.BattleScreen;
import org.kgu.domain.battle.view.component.BattleResultScreen;
import org.kgu.integration.ui.navigation.GameNavigator;
import org.kgu.integration.battle.BattleService;

/** Connects team data and saving to the independent battle screens. */
public final class BattleFlowPanel extends JPanel {
    private final GameNavigator navigator;

    public BattleFlowPanel(GameNavigator navigator) {
        this.navigator = navigator;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 960));
        showSetup();
    }

    private void showSetup() {
        var session = navigator.getSession();
        try {
            String field = session.getField();
            if ("무작위".equals(field)) {
                var categories = java.util.List.of("교수님", "과학자", "아이돌", "한국사");
                field = categories.get(new java.util.Random().nextInt(categories.size()));
            }
            var missions = new org.kgu.integration.battle.BattleRepository()
                    .load(field, session.getDifficulty()).stream()
                    .map(person -> new BattleSetupScreen.Mission(person.character(), person.questions()))
                    .toList();
            showScreen(new BattleSetupScreen(missions, this::startBattle, navigator::showMenu));
        } catch (RuntimeException ex) {
            showDataError(ex);
        }
    }

    private void startBattle(BattleEngine engine) {
        showScreen(new BattleScreen(engine, this::showResult, navigator::showMenu));
    }

    private void showResult(BattleEngine engine) {
        showScreen(new BattleResultScreen(engine, () -> saveTargetCard(engine),
                this::showSetup, navigator::showMenu, navigator::showCollection));
    }

    private boolean saveTargetCard(BattleEngine engine) {
        return new BattleService().saveReward(navigator.getSession().getPlayerId(), engine);
    }

    private void showDataError(RuntimeException error) {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea message = new JTextArea("인물 데이터를 불러오지 못했습니다.\n" + error.getMessage());
        message.setEditable(false);
        panel.add(message, BorderLayout.CENTER);
        JButton menu = new JButton("메인으로");
        menu.addActionListener(e -> navigator.showMenu());
        panel.add(menu, BorderLayout.SOUTH);
        showScreen(panel);
    }

    private void showScreen(JPanel screen) {
        removeAll();
        add(screen, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
}
