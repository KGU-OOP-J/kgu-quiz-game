package org.kgu.integration;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.*;
import org.kgu.domain.battle.feature.BattleEngine;
import org.kgu.integration.battle.BattleRepository;
import org.kgu.integration.ui.navigation.GameNavigator;
import org.kgu.domain.battle.view.component.BattleScreen;
import org.kgu.domain.battle.view.component.BattleSetupScreen;

/** Manual fixture runner, excluded from the production JAR. */
public final class IntegrationMain {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--smoke")) {
            smokeTest();
        } else if (args.length > 0 && args[0].equals("--battle")) {
            SwingUtilities.invokeLater(IntegrationMain::openBattlePreview);
        } else {
            SwingUtilities.invokeLater(() -> new GameNavigator().open());
        }
    }

    private static List<BattleSetupScreen.Mission> loadMissions() {
        return new BattleRepository().load("교수님", "중").stream()
                .map(person -> new BattleSetupScreen.Mission(person.character(), person.questions()))
                .toList();
    }

    private static void smokeTest() throws Exception {
        var missions = loadMissions();
        if (missions.size() != 20) throw new IllegalStateException("Integration data missing");
        SwingUtilities.invokeAndWait(() -> {
            var mission = missions.get(0);
            var engine = new BattleEngine(mission.target(), mission.questions(), mission.requiredCorrect());
            new BattleScreen(engine, result -> { }, () -> { });
        });
        System.out.println("PASS: fixture data and production BattleScreen");
    }

    /** Direct preview does not change account data or card saves. */
    private static void openBattlePreview() {
        JFrame frame = new JFrame("WHO’S NEXT? — 교수님 구출 · 단독 미리보기");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        try {
            showSetup(frame, loadMissions());
            frame.pack();
            var usable = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            frame.setSize(Math.min(frame.getWidth(), usable.width), Math.min(frame.getHeight(), usable.height));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null, "구출 화면 실행 실패: " + ex.getMessage());
            frame.dispose();
        }
    }

    private static void showSetup(JFrame frame, List<BattleSetupScreen.Mission> missions) {
        frame.setContentPane(new BattleSetupScreen(missions, engine -> startBattle(frame, missions, engine), frame::dispose));
        frame.revalidate();
        frame.repaint();
    }

    private static void startBattle(JFrame frame, List<BattleSetupScreen.Mission> missions, BattleEngine engine) {
        frame.setContentPane(new BattleScreen(engine, result -> {
            String message = result.won() ? "교수님 구출 성공!" : "아직 기억이 흐릿합니다. 다시 도전해 보세요.";
            JOptionPane.showMessageDialog(frame, message + "\n정답 " + result.correctCount()
                    + " / " + result.questionCount() + "\n단독 미리보기에서는 도감에 저장하지 않습니다.");
            showSetup(frame, missions);
        }, frame::dispose));
        frame.revalidate();
        frame.repaint();
    }
}
