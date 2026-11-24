package org.kgu.game.preview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.application.Difficulty;
import org.kgu.factory.SaveFactory;
import org.kgu.game.feature.BattleEngine;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class BattleTestMainTest {
    @TempDir Path scratch;
    @Test void previewOpensProfessorWithAnIsolatedUnlockedProfile() throws Exception {
        var game = BattleTestMain.createGame(Path.of(System.getProperty("game.projectDir")), scratch);
        assertTrue(game.categoryUnlocked("교수님"));
        game.startBattle("교수님", Difficulty.LOW, game.getChallengeCandidates("교수님").getFirst());
        assertEquals(BattleEngine.Phase.QUESTION, game.getSession().getBattle().phase());
        assertEquals("교수님", game.getSession().getBattle().computer().getCategory());
        assertEquals(5, game.getSession().getQuestionCount());
        assertTrue(Files.exists(scratch.resolve("preview.txt.progress")));
        SaveFactory ordinary = new SaveFactory(scratch.resolve("ordinary.txt")); ordinary.open();
        assertFalse(ordinary.progress().snapshot().professorUnlocked());
    }
    @Test void launcherActuallyOpensTheProfessorBattleWindow() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeFalse(java.awt.GraphicsEnvironment.isHeadless());
        java.util.Set<java.awt.Frame> before = java.util.Set.of(java.awt.Frame.getFrames());
        try {
            BattleTestMain.main(new String[0]);
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                var opened = java.util.Arrays.stream(java.awt.Frame.getFrames())
                    .filter(frame -> !before.contains(frame) && frame.isVisible()).toList();
                assertEquals(1, opened.size());
                assertTrue(opened.getFirst().getTitle().contains("교수님"));
                javax.swing.JFrame frame = (javax.swing.JFrame) opened.getFirst();
                assertTrue(java.util.Arrays.stream(frame.getContentPane().getComponents())
                    .anyMatch(component -> component instanceof org.kgu.game.view.BattlePanel));
            });
        } finally {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                for (java.awt.Frame frame : java.awt.Frame.getFrames()) if (!before.contains(frame)) frame.dispose();
            });
        }
    }

}
