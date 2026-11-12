package org.kgu.integration;

import javax.swing.SwingUtilities;
import org.kgu.integration.ui.navigation.GameNavigator;
import org.kgu.integration.battle.BattleRepository;
import org.kgu.integration.battle.BattleService;
import org.kgu.ui.battle.BattleScreen;

/** Manual integration harness; intentionally excluded from the production JAR. */
public final class IntegrationMain {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--smoke")) {
            var people = new BattleRepository().load("교수님", "중");
            if (people.size() != 20) throw new IllegalStateException("Integration data missing");
            SwingUtilities.invokeAndWait(() -> {
                var engine = new BattleService().start(people.get(0), people, new java.util.Random(7));
                new BattleScreen(engine, result -> {}, () -> {});
            });
            System.out.println("PASS: integration entry point, fixture data and production BattleScreen");
            return;
        }
        if (args.length > 0 && args[0].equals("--battle")) {
            SwingUtilities.invokeLater(IntegrationMain::openBattlePreview);
            return;
        }
        SwingUtilities.invokeLater(() -> new GameNavigator().open());
    }
    /** Direct preview bypasses unrelated team menus and does not write collection saves. */
    private static void openBattlePreview() {
        var frame = new javax.swing.JFrame("WHO’S NEXT? — 배틀 단독 확인 · Swing 컴포넌트 버전");
        frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        try {
            var people = new BattleRepository().load("교수님", "중");
            var engine = new BattleService().start(people.get(0), people, new java.util.Random());
            frame.setContentPane(new BattleScreen(engine, result -> {
                String verdict = switch (result.result()) { case WIN -> "승리"; case LOSE -> "패배"; case DRAW -> "무승부"; };
                javax.swing.JOptionPane.showMessageDialog(frame, "결과: " + verdict + "\n배틀 미리보기에서는 도감에 저장하지 않습니다.");
                frame.dispose();
            }, frame::dispose));
            frame.pack();
            var usable = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            frame.setSize(Math.min(frame.getWidth(), usable.width), Math.min(frame.getHeight(), usable.height));
            frame.setLocation(usable.x + (usable.width - frame.getWidth()) / 2,
                    usable.y + (usable.height - frame.getHeight()) / 2);
            frame.setVisible(true);
        } catch (RuntimeException ex) {
            javax.swing.JOptionPane.showMessageDialog(null, "배틀 실행 실패: " + ex.getMessage());
            frame.dispose();
        }
    }
}
