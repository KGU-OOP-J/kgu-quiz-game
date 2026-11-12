package org.kgu.ui.battle;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.JPanel;
import org.kgu.battle.BattleEngine;

/**
 * Production battle page. The caller supplies a prepared battle and owns navigation/rewards.
 * No dependency on login, collection, question repositories or result screens.
 */
public final class BattleScreen extends JPanel {
    public BattleScreen(BattleEngine engine, Consumer<BattleEngine> onFinished, Runnable onExit) {
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(onFinished, "onFinished");
        Objects.requireNonNull(onExit, "onExit");
        if (engine.phase() != BattleEngine.Phase.QUESTION || engine.turn() != 0) {
            throw new IllegalArgumentException("새로 준비한 전투를 전달해 주세요.");
        }
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1280, 960));
        add(new BattlePanel(engine, () -> onFinished.accept(engine), onExit), BorderLayout.CENTER);
    }
}
