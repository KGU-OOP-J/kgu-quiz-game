package org.kgu.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.kgu.ui.battle.BattlePanel;
import org.kgu.ui.battle.BattleScreen;

/** Exercises the production boundary with in-memory inputs, without any team fixture. */
class BattleScreenTest {
    private BattleEngine engine() {
        var a = new BattleCharacter("a", "A", "test", "");
        var b = new BattleCharacter("b", "B", "test", "");
        var attack = List.of(question("a", 1), question("a", 2), question("a", 3));
        var defense = List.of(question("b", 1), question("b", 2), question("b", 3));
        return new BattleEngine(a, b, attack, defense);
    }
    private BattleQuestion question(String id, int number) {
        return new BattleQuestion(id + number, id, "중", "Question " + number,
                new String[] {"A", "B", "C", "D"}, 0, "", "A");
    }
    @Test void preparedBattleReturnsOneFinishedResultToCaller() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            var engine = engine();
            var screen = new BattleScreen(engine, result -> {
                assertSame(engine, result);
                assertEquals(BattleEngine.Result.WIN, result.result());
                calls.incrementAndGet();
            }, () -> {});
            var panel = (BattlePanel) screen.getComponent(0);
            for (int i = 0; i < 6; i++) {
                panel.getActionMap().get("choice0").actionPerformed(null);
                panel.getActionMap().get("confirm").actionPerformed(null);
                panel.getActionMap().get("confirm").actionPerformed(null);
            }
            panel.getActionMap().get("confirm").actionPerformed(null);
        });
        assertEquals(1, calls.get());
    }
    @Test void exitOnlyNotifiesCallerWithoutFinishingOrAwarding() throws Exception {
        AtomicInteger exits = new AtomicInteger();
        SwingUtilities.invokeAndWait(() -> {
            var engine = engine();
            var screen = new BattleScreen(engine, result -> fail("Unexpected result"), exits::incrementAndGet);
            var panel = (BattlePanel) screen.getComponent(0);
            panel.setSize(960, 720);
            named(panel, "menu", javax.swing.JButton.class).doClick();
            assertEquals(BattleEngine.Phase.QUESTION, engine.phase());
            assertTrue(engine.rewardIds().isEmpty());
        });
        assertEquals(1, exits.get());
    }
    @Test void backgroundIsPackagedAsProductionResource() {
        assertNotNull(BattleScreen.class.getResource("/org/kgu/battle/campus_background.png"));
    }

    private <T> T named(BattlePanel panel, String name, Class<T> type) {
        for (var component : panel.getComponents()) {
            if (name.equals(component.getName())) return type.cast(component);
        }
        throw new AssertionError("Component not found: " + name);
    }
    @Test void actualButtonsSelectLockAndAdvanceWithoutCoordinateHitTesting() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var battle = engine();
            var panel = new BattlePanel(battle, () -> {}, () -> {});
            var first = named(panel, "choice0", javax.swing.JToggleButton.class);
            var second = named(panel, "choice1", javax.swing.JToggleButton.class);
            var submit = named(panel, "submit", javax.swing.JButton.class);
            var next = named(panel, "next", javax.swing.JButton.class);
            assertFalse(submit.isEnabled()); assertFalse(next.isEnabled());
            first.doClick(); second.doClick();
            assertFalse(first.isSelected()); assertTrue(second.isSelected());
            assertEquals(1, battle.selected());
            submit.doClick();
            assertFalse(first.isEnabled()); assertFalse(second.isEnabled());
            assertFalse(submit.isEnabled()); assertTrue(next.isEnabled());
            assertTrue(named(panel, "feedback", javax.swing.JTextArea.class).getText().contains("정답: 1번"));
            first.doClick(); assertEquals(1, battle.selected());
            next.doClick();
            assertEquals(1, battle.turn()); assertTrue(first.isEnabled());
            assertFalse(first.isSelected()); assertFalse(second.isSelected());
            assertFalse(submit.isEnabled());
        });
    }
    @Test void missingHintIsExplicitAndDoesNotChangeBattle() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var battle = engine(); var panel = new BattlePanel(battle, () -> {}, () -> {});
            named(panel, "hint", javax.swing.JButton.class).doClick();
            assertTrue(named(panel, "feedback", javax.swing.JTextArea.class).getText().contains("등록된 힌트가 없습니다"));
            assertEquals(0, battle.turn()); assertEquals(60, battle.playerHp());
            named(panel, "hint", javax.swing.JButton.class).doClick();
            assertEquals("", named(panel, "feedback", javax.swing.JTextArea.class).getText());
        });
    }
}
