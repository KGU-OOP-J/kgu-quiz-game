package org.kgu.integration.ui.battle;

import org.kgu.integration.battle.BattleService;

import java.awt.*;
import javax.swing.*;
import org.kgu.battle.*;
import org.kgu.integration.ui.common.UiStyle;
import org.kgu.integration.ui.navigation.GameNavigator;

/** Battle-specific results leave the answer team's existing correct/incorrect screen unchanged. */
public final class BattleResultScreen extends JPanel {
    public BattleResultScreen(GameNavigator navigator, BattleEngine engine) {
        setPreferredSize(new Dimension(960, 720));
        setLayout(new GridBagLayout());
        setBackground(new Color(237, 247, 255));
        JPanel content = new JPanel(new GridLayout(0, 1, 12, 24));
        content.setOpaque(false);
        String result = switch (engine.result()) { case WIN -> "승리!"; case LOSE -> "패배"; case DRAW -> "무승부"; };
        JLabel title = new JLabel("배틀 결과 · " + result, SwingConstants.CENTER);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 36f));
        content.add(title);
        content.add(new JLabel("PLAYER " + engine.playerHp() + " HP   /   COMPUTER " + engine.computerHp() + " HP", SwingConstants.CENTER));
        content.add(new JLabel("정답 " + engine.correctCount() + " / 6", SwingConstants.CENTER));
        JLabel status = new JLabel("획득한 카드가 없습니다.", SwingConstants.CENTER);
        content.add(status);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        JButton retrySave = new JButton("보상 저장 다시 시도");
        retrySave.setVisible(false);
        Runnable save = () -> {
            try {
                new BattleService().saveRewards(navigator.getSession().getPlayerId(), engine);
                if (engine.result() == BattleEngine.Result.WIN) {
                    status.setText(engine.player().name() + " · " + engine.computer().name()
                            + " [" + engine.current().difficulty() + "] 카드 등록 완료 (중복 제외)");
                }
                retrySave.setVisible(false);
            } catch (RuntimeException ex) {
                status.setText("보상을 저장하지 못했습니다. 저장 경로를 확인한 뒤 다시 시도해 주세요.");
                retrySave.setVisible(true);
            }
        };
        retrySave.addActionListener(e -> save.run());
        buttons.add(retrySave);
        JButton collection = new JButton("도감 확인");
        collection.addActionListener(e -> navigator.showCollection());
        buttons.add(collection);
        JButton again = new JButton("다시 플레이");
        again.addActionListener(e -> navigator.showBattle());
        buttons.add(again);
        JButton menu = new JButton("메인으로");
        menu.addActionListener(e -> navigator.showMenu());
        buttons.add(menu);
        content.add(buttons);
        add(content);
        save.run();
    }
}
