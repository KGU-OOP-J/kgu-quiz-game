package org.kgu.battle;

import javax.swing.Timer;
import org.kgu.ui.battle.BattleScreen;

/** Advances the feedback timer deterministically; live timer behavior is tested separately. */
public final class UiTestActions {
    private UiTestActions() { }

    public static Timer advanceTimer(BattleScreen screen) {
        try {
            var field = BattleScreen.class.getDeclaredField("autoAdvance");
            field.setAccessible(true);
            return (Timer) field.get(screen);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    public static void completeFeedback(BattleScreen screen) {
        Timer timer = advanceTimer(screen);
        timer.stop();
        for (var listener : timer.getActionListeners()) listener.actionPerformed(null);
    }
}
