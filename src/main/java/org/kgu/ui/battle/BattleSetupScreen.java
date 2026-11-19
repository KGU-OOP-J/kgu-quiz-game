package org.kgu.ui.battle;

import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.*;
import org.kgu.battle.BattleCharacter;
import org.kgu.battle.BattleQuestion;
import org.kgu.battle.BattleEngine;

/** Presents one encounter from the chosen category, without revealing the answer's name. */
public final class BattleSetupScreen extends JPanel {
    public record Mission(BattleCharacter target, List<BattleQuestion> questions, int requiredCorrect) {
        public Mission {
            questions = List.copyOf(questions);
            new BattleEngine(target, questions, requiredCorrect);
        }

        public Mission(BattleCharacter target, List<BattleQuestion> questions) {
            this(target, questions, BattleEngine.defaultRequiredCorrect(questions.size()));
        }
    }

    public BattleSetupScreen(List<Mission> missions, Consumer<BattleEngine> onStart, Runnable onMenu) {
        setPreferredSize(new Dimension(1280, 960));
        setLayout(new BorderLayout(25, 25));
        setBackground(new Color(231, 245, 255));
        setBorder(BorderFactory.createEmptyBorder(65, 100, 65, 100));
        Mission mission = missions.isEmpty() ? null : missions.get(new Random().nextInt(missions.size()));

        JLabel title = new JLabel("실종된 교수님을 찾아서", SwingConstants.CENTER);
        title.setFont(new Font("Malgun Gothic", Font.BOLD, 38));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(0, 1, 15, 25));
        center.setOpaque(false);
        JTextArea intro = new JTextArea("졸업을 앞둔 대학교 4학년. 어느 날 교수님들이 실종되었습니다.\n\n"
                + "여러 마을의 인물에게 도전하며 기억을 잃은 교수님들을 찾아주세요.\n"
                + "문제를 맞혀 한 명을 쓰러뜨리면 해당 난이도의 카드 한 장을 얻습니다.");
        intro.setFont(new Font("Malgun Gothic", Font.PLAIN, 24));
        intro.setLineWrap(true);
        intro.setWrapStyleWord(true);
        intro.setEditable(false);
        intro.setOpaque(false);
        center.add(intro);

        JLabel encounter = new JLabel(mission == null ? "도전 가능한 인물이 없습니다."
                : mission.target().category() + " · 새로운 인물이 기다리고 있습니다", SwingConstants.CENTER);
        encounter.setName("encounter");
        encounter.setFont(new Font("Malgun Gothic", Font.BOLD, 28));
        center.add(encounter);
        JLabel rule = new JLabel(mission == null ? "데이터를 확인해 주세요."
                : "난이도 " + mission.questions().get(0).difficulty() + " · 정답으로 공격 · HP 0이면 카드 획득",
                SwingConstants.CENTER);
        rule.setFont(new Font("Malgun Gothic", Font.PLAIN, 22));
        center.add(rule);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton menu = new JButton("메인으로");
        menu.addActionListener(e -> onMenu.run());
        JButton start = new JButton("도전 시작");
        start.setName("startBattle");
        start.setEnabled(mission != null);
        start.addActionListener(e -> {
            if (mission != null) onStart.accept(new BattleEngine(mission.target(), mission.questions(), mission.requiredCorrect()));
        });
        actions.add(menu);
        actions.add(start);
        add(actions, BorderLayout.SOUTH);
    }
}
