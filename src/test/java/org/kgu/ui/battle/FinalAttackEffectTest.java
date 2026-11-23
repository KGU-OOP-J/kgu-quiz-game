package org.kgu.ui.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.kgu.battle.*;

class FinalAttackEffectTest {
    private BattleEngine mission(String category) {
        var person = new BattleCharacter("p", "권기현", category, "images/cards/권기현_2.png");
        var question = new BattleQuestion("q", "p", "중", "교수님에 관한 기억을 떠올려보세요.",
                new String[]{"정답", "보기 2", "보기 3", "보기 4"}, 0, "", "");
        return new BattleEngine(person, List.of(question), 1);
    }

    @Test void rendersChargeImpactAndDefeatWithoutChangingGameState() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var engine = mission("교수님");
            var screen = new BattleScreen(engine, result -> { }, () -> { });
            screen.setSize(1280, 960);
            screen.doLayout();
            var effect = new FinalAttackEffect();
            effect.start(0);
            long[] times = {550_000_000L, 1_300_000_000L, 2_300_000_000L};
            String[] phases = {"charge", "impact", "defeat"};
            for (int i = 0; i < times.length; i++) {
                effect.update(times[i]);
                assertTrue(effect.isRunning());
                BufferedImage image = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                screen.paint(g);
                effect.paint(g, 1280, 960);
                g.dispose();
                try {
                    Path file = Path.of(System.getProperty("battle.previewPath").replace(".png", "-final-" + phases[i] + ".png"));
                    ImageIO.write(image, "png", file.toFile());
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }
            effect.update(2_800_000_000L);
            assertFalse(effect.isRunning());
            assertEquals(60, engine.remainingHp());
            assertEquals(BattleEngine.Phase.QUESTION, engine.phase());
        });
    }

    @Test void failedProfessorBattleWaitsForFinisherAndNotifiesOnce() throws Exception {
        var frame = new AtomicReference<JFrame>();
        var callbacks = new AtomicInteger();
        var completed = new CountDownLatch(1);
        var engine = mission("교수님");
        try {
            SwingUtilities.invokeAndWait(() -> {
                var screen = new BattleScreen(engine, result -> {
                    assertFalse(result.won());
                    assertTrue(result.rewardIds().isEmpty());
                    callbacks.incrementAndGet();
                    completed.countDown();
                }, () -> { });
                frame.set(showOffscreen(screen));
                screen.getActionMap().get("choice1").actionPerformed(null);
                screen.getActionMap().get("confirm").actionPerformed(null);
                UiTestActions.completeFeedback(screen);
                assertEquals(BattleEngine.Phase.FINISHED, engine.phase());
                assertTrue(effect(screen).isRunning());
                assertEquals(0, callbacks.get());
                // Another queued event must not complete the cutscene early.
                UiTestActions.completeFeedback(screen);
                assertEquals(0, callbacks.get());
            });
            assertTrue(completed.await(6, TimeUnit.SECONDS));
            assertEquals(1, callbacks.get());
        } finally {
            SwingUtilities.invokeAndWait(() -> { if (frame.get() != null) frame.get().dispose(); });
        }
    }

    @Test void menuExitCancelsFinisherAndResultCallback() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger callbacks = new AtomicInteger();
            AtomicInteger exits = new AtomicInteger();
            var screen = new BattleScreen(mission("교수님"), result -> callbacks.incrementAndGet(), exits::incrementAndGet);
            JFrame frame = showOffscreen(screen);
            try {
                screen.getActionMap().get("choice1").actionPerformed(null);
                screen.getActionMap().get("confirm").actionPerformed(null);
                UiTestActions.completeFeedback(screen);
                assertTrue(effect(screen).isRunning());
                for (Component child : screen.getComponents()) {
                    if ("menu".equals(child.getName())) ((JButton) child).doClick();
                }
                assertEquals(1, exits.get());
                assertFalse(timer(screen).isRunning());
                effect(screen).update(Long.MAX_VALUE);
                for (var listener : timer(screen).getActionListeners()) listener.actionPerformed(null);
                assertEquals(0, callbacks.get());
            } finally {
                frame.dispose();
            }
        });
    }

    @Test void otherCategoriesDoNotUseProfessorFinisher() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AtomicInteger callbacks = new AtomicInteger();
            var screen = new BattleScreen(mission("과학자"), result -> callbacks.incrementAndGet(), () -> { });
            JFrame frame = showOffscreen(screen);
            try {
                screen.getActionMap().get("choice1").actionPerformed(null);
                screen.getActionMap().get("confirm").actionPerformed(null);
                UiTestActions.completeFeedback(screen);
                assertFalse(effect(screen).isVisible());
                assertEquals(1, callbacks.get());
            } finally {
                frame.dispose();
            }
        });
    }

    private JFrame showOffscreen(BattleScreen screen) {
        JFrame frame = new JFrame();
        frame.setContentPane(screen);
        frame.pack();
        frame.setLocation(-5000, -5000);
        frame.setVisible(true);
        return frame;
    }

    private FinalAttackEffect effect(BattleScreen screen) { return field(screen, "finalAttack", FinalAttackEffect.class); }
    private Timer timer(BattleScreen screen) { return field(screen, "animation", Timer.class); }

    private <T> T field(BattleScreen screen, String name, Class<T> type) {
        try {
            var field = BattleScreen.class.getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(screen));
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }
}
