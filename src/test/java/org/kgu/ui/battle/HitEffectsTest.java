package org.kgu.domain.battle.view.component;

import org.kgu.battle.UiTestActions;

import static org.junit.jupiter.api.Assertions.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.junit.jupiter.api.Test;
import org.kgu.domain.battle.feature.*;

class HitEffectsTest {
    private BattleEngine engine() {
        var professor = new BattleCharacter("prof", "권기현", "교수님", "images/cards/권기현_2.png");
        var questions = new java.util.ArrayList<BattleQuestion>();
        for (int i = 0; i < 3; i++) {
            questions.add(new BattleQuestion("question-" + i, "prof", "중", "권기현 교수님의 저서가 아닌 것은?",
                    new String[]{"Discrete Mathematics", "형식언어와 오토마타", "컴퓨터 과학을 위한 논리", "퍼스널 소프트웨어 프로세스 입문"}, 3, "", ""));
        }
        return new BattleEngine(professor, questions, 2);
    }

    @Test void hitSplitsPortraitAndFinishesWithoutChangingEngine() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var engine = engine();
            var screen = new BattleScreen(engine, result -> { }, () -> { });
            screen.setSize(1280, 960);
            layout(screen);
            ImpactCard card = find(screen, ImpactCard.class);
            screen.getActionMap().get("choice3").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            UiTestActions.advanceTimer(screen).stop();
            BufferedImage before = render(screen);
            card.startHit(30, false, 0);
            card.updateHit(210_000_000);
            assertTrue(card.isAnimating());
            assertNotEquals(0, card.shakeOffset());
            BufferedImage hit = render(screen);
            assertNotEquals(imageHash(before), imageHash(hit));
            assertEquals(30, engine.remainingHp());
            save(hit, "-hit.png");
            card.updateHit(850_000_000);
            assertFalse(card.isAnimating());
            assertEquals(0, card.shakeOffset());
            assertEquals(imageHash(before), imageHash(render(screen)));
        });
    }

    @Test void knockoutHasLongerEffectAndReleaseBadge() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var screen = new BattleScreen(engine(), result -> { }, () -> { });
            screen.setSize(1280, 960);
            layout(screen);
            ImpactCard card = find(screen, ImpactCard.class);
            screen.getActionMap().get("choice3").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            UiTestActions.completeFeedback(screen);
            screen.getActionMap().get("choice3").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            UiTestActions.advanceTimer(screen).stop();
            card.startHit(30, true, 0);
            card.updateHit(350_000_000);
            save(render(screen), "-knockout.png");
            card.updateHit(900_000_000);
            assertTrue(card.isAnimating());
            card.updateHit(1_250_000_000);
            assertFalse(card.isAnimating());
            save(render(screen), "-released.png");
        });
    }

    @Test void missHasNoDamageEffect() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var engine = engine();
            var screen = new BattleScreen(engine, result -> { }, () -> { });
            screen.getActionMap().get("choice0").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            UiTestActions.advanceTimer(screen).stop();
            assertEquals(0, engine.lastDamage());
            assertEquals(60, engine.remainingHp());
            assertFalse(find(screen, ImpactCard.class).isAnimating());
        });
    }

    @Test void visibleHitThenAutomaticallyAdvancesWithoutEnter() throws Exception {
        var window = new java.util.concurrent.atomic.AtomicReference<JFrame>();
        var view = new java.util.concurrent.atomic.AtomicReference<BattleScreen>();
        var battle = engine();
        var completed = new java.util.concurrent.CountDownLatch(1);
        var poll = new java.util.concurrent.atomic.AtomicReference<Timer>();
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame frame = new JFrame();
                window.set(frame);
                BattleScreen screen = new BattleScreen(battle, result -> { }, frame::dispose);
                view.set(screen);
                frame.setContentPane(screen);
                frame.pack();
                frame.setLocation(-5000, -5000);
                frame.setVisible(true);
                screen.getActionMap().get("choice3").actionPerformed(null);
                screen.getActionMap().get("confirm").actionPerformed(null);
                assertEquals(30, battle.remainingHp());
                assertTrue(find(screen, ImpactCard.class).isAnimating());
                assertTrue(java.util.Arrays.stream(screen.getComponents()).noneMatch(c -> "pause".equals(c.getName())));
                screen.getActionMap().get("confirm").actionPerformed(null);
                assertEquals(0, battle.questionIndex());
                Timer completion = new Timer(20, e -> {
                    if (battle.questionIndex() == 1) {
                        ((Timer) e.getSource()).stop();
                        completed.countDown();
                    }
                });
                poll.set(completion);
                completion.start();
            });
            assertTrue(completed.await(5, java.util.concurrent.TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {
                assertFalse(find(view.get(), ImpactCard.class).isAnimating());
                assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
                assertEquals(-1, battle.selected());
                assertEquals(30, find(view.get(), JProgressBar.class).getValue());
                assertEquals(1, battle.correctCount());
            });
        } finally {
            SwingUtilities.invokeAndWait(() -> { if (poll.get() != null) poll.get().stop(); if (window.get() != null) window.get().dispose(); });
        }
    }

    @Test void automaticProgressContinuesAndExitCancelsItsTimer() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            var engine = engine();
            var screen = new BattleScreen(engine, result -> fail("unexpected finish"), () -> { });
            screen.getActionMap().get("choice0").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            assertTrue(UiTestActions.advanceTimer(screen).isRunning());
            UiTestActions.completeFeedback(screen);
            assertEquals(1, engine.questionIndex());
            screen.getActionMap().get("choice0").actionPerformed(null);
            screen.getActionMap().get("confirm").actionPerformed(null);
            button(screen, "menu").doClick();
            assertFalse(UiTestActions.advanceTimer(screen).isRunning());
            UiTestActions.completeFeedback(screen);
            assertEquals(1, engine.questionIndex());
        });
    }

    private static JButton button(Container screen, String name) {
        for (Component child : screen.getComponents()) {
            if (name.equals(child.getName())) return (JButton) child;
        }
        throw new AssertionError(name);
    }

    private static int imageHash(BufferedImage image) {
        return java.util.Arrays.hashCode(image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth()));
    }

    private static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) {
            if (child instanceof Container container) layout(container);
        }
    }

    private static BufferedImage render(JComponent component) {
        BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        component.paint(graphics);
        graphics.dispose();
        return image;
    }

    private static void save(BufferedImage image, String suffix) {
        try {
            Path path = Path.of(System.getProperty("battle.previewPath").replace(".png", suffix));
            ImageIO.write(image, "png", path.toFile());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static <T> T find(Container parent, Class<T> type) {
        if (type.isInstance(parent)) return type.cast(parent);
        for (Component child : parent.getComponents()) {
            if (child instanceof Container container) {
                T found = find(container, type);
                if (found != null) return found;
            }
        }
        return null;
    }
}
