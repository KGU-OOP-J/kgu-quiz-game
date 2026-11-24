package org.kgu.game.preview;

import org.kgu.application.*;
import org.kgu.factory.*;
import org.kgu.game.event.BattleGUIEvent;
import org.kgu.game.feature.GameFeature;
import org.kgu.game.view.BattlePanel;
import org.kgu.manager.*;
import org.kgu.model.*;
import org.kgu.progression.ShopItem;
import org.kgu.window.GameTheme;
import javax.swing.*;
import java.awt.*;
import java.nio.file.*;
import java.util.Comparator;

/** Test-source-only launcher. Real data, isolated temporary profile, production battle UI. */
public final class BattleTestMain {
    static GameFeature createGame(Path project, Path scratch) throws Exception {
        PersonManager people = PersonManager.getInstance(); people.clear();
        ImageManager images = ImageManager.getInstance(); images.clear();
        for (String category : Application.CATEGORIES) {
            CategoryFileFactory file = new CategoryFileFactory(project.resolve("data/" + category + ".txt"), category);
            file.load();
            for (PersonFactory person : file.create()) {
                people.add(person);
                for (int stage = 1; stage <= 3; stage++) images.register("cards/" + person.create().getName() + "_" + stage,
                    project.resolve("images/cards/" + person.create().getName() + "_" + stage + ".png"));
            }
        }
        SaveFactory save = new SaveFactory(scratch.resolve("preview.txt"));
        if (!Files.exists(save.progress().path())) {
            java.util.Properties seed = new java.util.Properties();
            seed.setProperty("version", "1"); seed.setProperty("coins", "120");
            for (ShopItem item : ShopItem.values()) seed.setProperty("item." + item.name(), "5");
            try (var writer = Files.newBufferedWriter(save.progress().path())) {
                seed.store(writer, "Temporary battle preview inventory only");
            }
        }
        save.open();
        GameFeature game = new GameFeature(new PlayerSession("배틀 미리보기", save), people, images,
            new SettingFactory(scratch.resolve("settings.properties"), new Setting()), view -> { });
        // Seed only the throwaway profile; no production unlock API or real user save is changed.
        for (int i = 0; i < 30; i++) game.progress().answer("preview-unlock-" + i, "과학자", true);
        return game;
    }

    public static void main(String[] args) throws Exception {
        Path scratch = Files.createTempDirectory("kgu-battle-preview-");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try (var paths = Files.walk(scratch)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            } catch (Exception ignored) { /* OS temporary storage remains isolated even on cleanup failure. */ }
        }));
        GameFeature game = createGame(Path.of(System.getProperty("game.projectDir", ".")), scratch);
        SwingUtilities.invokeLater(() -> {
            new PreviewWindow(game).showBattle();
            System.out.println("BATTLE_PREVIEW_READY: professor battle, five of each item, temporary profile");
        });
    }

    private static final class PreviewWindow {
        private final GameFeature game;
        private final JFrame frame = new JFrame("배틀 테스트 — 교수님 잠금 해제 / 아이템 각 5개 / 임시 프로필");
        private final JComboBox<String> categories = new JComboBox<>(Application.CATEGORIES.toArray(String[]::new));
        private final JComboBox<Difficulty> difficulties = new JComboBox<>(Difficulty.values());
        private BattlePanel battle;
        PreviewWindow(GameFeature game) {
            this.game = game;
            GameTheme.install();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            categories.setSelectedItem("교수님");
            difficulties.setRenderer(new DefaultListCellRenderer() {
                @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    return super.getListCellRendererComponent(list, value instanceof Difficulty difficulty ? difficulty.getLabel() : value,
                            index, selected, focus);
                }
            });
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            toolbar.add(new JLabel("테스트 전용 · 실제 세이브에 저장하지 않음"));
            toolbar.add(categories); toolbar.add(difficulties);
            JButton restart = new JButton("선택한 분야로 다시 시작"); restart.addActionListener(e -> showBattle()); toolbar.add(restart);
            for (boolean correct : new boolean[]{true, false}) {
                JButton answer = new JButton(correct ? "정답으로 공격" : "오답으로 진행");
                answer.addActionListener(e -> {
                    int index = correct ? game.getSession().getBattle().current().getAnswer() - 1
                            : game.getSession().getBattle().current().getAnswer() % 4;
                    battle.getActionMap().get("choice" + index).actionPerformed(null);
                    battle.getActionMap().get("submit").actionPerformed(null);
                }); toolbar.add(answer);
            }
            frame.add(toolbar, BorderLayout.NORTH);
        }
        void showBattle() {
            if (battle != null) { battle.stopAnimation(); frame.remove(battle); }
            String category = (String) categories.getSelectedItem();
            game.startBattle(category, (Difficulty) difficulties.getSelectedItem(), game.getChallengeCandidates(category).getFirst());
            BattleGUIEvent event = new BattleGUIEvent(game);
            battle = new BattlePanel(game.getSession().getBattle(), event, () -> {
                JOptionPane.showMessageDialog(frame, game.getSession().getBattle().result() + " · 테스트 종료. 다시 시작할 수 있어요.");
            }, this::showBattle);
            frame.add(battle, BorderLayout.CENTER);
            if (!frame.isVisible()) { frame.pack(); frame.setLocationRelativeTo(null); frame.setVisible(true); }
            frame.revalidate(); frame.repaint();
        }
    }
}
