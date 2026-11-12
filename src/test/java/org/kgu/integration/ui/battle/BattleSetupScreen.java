package org.kgu.integration.ui.battle;

import org.kgu.ui.battle.BattleScreen;
import org.kgu.integration.battle.*;

import java.awt.*;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import org.kgu.battle.*;
import org.kgu.integration.game.PlayerSession;
import org.kgu.integration.ui.common.UiStyle;
import org.kgu.integration.ui.navigation.GameNavigator;

/** The team's existing battle entry point, hosted in GameNavigator's single frame. */
public final class BattleSetupScreen extends JPanel {
    public BattleSetupScreen(GameNavigator navigator) {
        setPreferredSize(new Dimension(1280, 960));
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(237, 247, 255));
        setBorder(BorderFactory.createEmptyBorder(45, 55, 45, 55));
        PlayerSession session = navigator.getSession();
        JLabel title = new JLabel("대진 준비 · " + session.getField() + " · " + session.getDifficulty(), SwingConstants.CENTER);
        title.setFont(UiStyle.FONT.deriveFont(Font.BOLD, 30f));
        add(title, BorderLayout.NORTH);
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        add(center, BorderLayout.CENTER);
        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton back = new JButton("메인으로");
        back.addActionListener(e -> navigator.showMenu());
        actions.add(back);
        add(actions, BorderLayout.SOUTH);
        try {
            List<BattleRepository.Entry> people = new BattleRepository().load(session.getField(), session.getDifficulty());
            if (people.size() < 2) throw new IllegalStateException("선택한 분야·난이도에 3문제씩 있는 인물이 2명 이상 필요합니다.");
            JPanel form = new JPanel(new GridLayout(0, 1, 10, 20));
            form.setOpaque(false);
            form.add(new JLabel("학습할 인물을 선택하세요. 상대는 다른 인물 중에서 무작위로 정해집니다."));
            JComboBox<BattleRepository.Entry> choice = new JComboBox<>(people.toArray(BattleRepository.Entry[]::new));
            choice.setFont(UiStyle.FONT.deriveFont(20f));
            form.add(choice);
            form.add(new JLabel("공격 정답: 상대 HP -20 / 방어 오답: 내 HP -20"));
            center.add(form);
            JButton start = new JButton("배틀 시작");
            start.addActionListener(e -> {
                BattleEngine engine = new BattleService().start((BattleRepository.Entry) choice.getSelectedItem(), people, new Random());
                removeAll();
                setBorder(BorderFactory.createEmptyBorder());
                add(new BattleScreen(engine, navigator::showBattleResult, navigator::showMenu), BorderLayout.CENTER);
                revalidate();
                repaint();
            });
            actions.add(start);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JTextArea message = new JTextArea(ex.getMessage() + "\n데이터 파일을 확인한 뒤 다시 시작해 주세요.");
            message.setEditable(false);
            message.setOpaque(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setColumns(36);
            center.add(message);
        }
    }
}
