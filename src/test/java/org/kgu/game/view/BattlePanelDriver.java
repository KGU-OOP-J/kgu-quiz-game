package org.kgu.domain.game.view;

import org.kgu.domain.game.view.component.BattleFinishEffect;

import org.kgu.domain.game.event.BattleGUIEvent;
import org.kgu.domain.game.feature.BattleEngine;
import javax.swing.Timer;
import java.util.concurrent.atomic.AtomicLong;

/** Runs real Swing timer callbacks on the EDT with a virtual clock. */
public final class BattlePanelDriver {
    private final AtomicLong clock = new AtomicLong();
    private final BattlePanel panel;
    private final Timer timer;

    public BattlePanelDriver(BattleEngine battle, BattleGUIEvent event, Runnable finish, Runnable menu) {
        panel = new BattlePanel(battle, event, finish, menu, clock::get);
        try {
            var field = BattlePanel.class.getDeclaredField("animation");
            field.setAccessible(true);
            timer = (Timer) field.get(panel);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
    public BattlePanel panel() { return panel; }
    public boolean running() { return timer.isRunning(); }
    public boolean portraitAnimating() { return field("portrait", org.kgu.domain.battle.view.component.ImpactCard.class).isAnimating(); }
    public javax.swing.JProgressBar countdown() { return field("countdown", javax.swing.JProgressBar.class); }
    public boolean finishEffectRunning() { return field("finishEffect", BattleFinishEffect.class).isRunning(); }
    public boolean finalEffectRunning() { return field("finalAttack", org.kgu.domain.battle.view.component.FinalAttackEffect.class).isRunning(); }
    private <T> T field(String name, Class<T> type) {
        try {
            var field = BattlePanel.class.getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(panel));
        } catch (ReflectiveOperationException ex) { throw new AssertionError(ex); }
    }
    public void tick() {
        clock.addAndGet(50_000_000L);
        if (timer.isRunning()) for (var listener : timer.getActionListeners()) listener.actionPerformed(null);
    }
    public void finishAnimation() {
        for (int i = 0; i < 200 && timer.isRunning(); i++) tick();
        if (timer.isRunning()) throw new AssertionError("Animation did not finish");
    }
}
