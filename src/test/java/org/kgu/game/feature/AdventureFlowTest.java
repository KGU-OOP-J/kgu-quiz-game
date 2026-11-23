package org.kgu.game.feature;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.adventure.*;
import org.kgu.application.*;
import org.kgu.factory.*;
import org.kgu.game.event.*;
import org.kgu.game.view.*;
import org.kgu.manager.*;
import org.kgu.model.*;
import org.kgu.progression.*;
import org.kgu.window.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class AdventureFlowTest {
    @TempDir Path temp;
    private GameFeature game;
    private SaveFactory save;
    private final AtomicReference<ViewId> destination = new AtomicReference<>();
    private final Path project = Path.of(System.getProperty("game.projectDir"));
    @BeforeEach void loadRealGameData() throws Exception {
        PersonManager people = PersonManager.getInstance(); people.clear();
        ImageManager images = ImageManager.getInstance(); images.clear();
        for (String category : Application.CATEGORIES) {
            CategoryFileFactory file = new CategoryFileFactory(project.resolve("data/" + category + ".txt"), category); file.load();
            for (PersonFactory person : file.create()) {
                people.add(person);
                for (int i = 1; i <= 3; i++) images.register("cards/" + person.create().getName() + "_" + i,
                    project.resolve("images/cards/" + person.create().getName() + "_" + i + ".png"));
            }
        }
        save = new SaveFactory(temp.resolve("player.txt")); save.open();
        game = new GameFeature(new PlayerSession("테스터", save), people, images,
            new SettingFactory(temp.resolve("settings"), new Setting()), destination::set, new Random(17));
    }
    private void completeBattle() throws Exception {
        BattleEngine battle = game.getSession().getBattle();
        while (battle.phase() != BattleEngine.Phase.FINISHED) {
            battle.chooseAction(BattleEngine.Action.HEAVY); game.selectBattleAnswer(battle.current().getAnswer() - 1);
            game.submitBattleAnswer(); game.advanceBattle();
        }
        assertEquals(BattleEngine.Result.WIN, battle.result()); game.finishBattle(); game.saveRewards();
    }
    @Test void progressionLoopUnlocksBossLevelsUpAndSupportsRetryAndReload() throws Exception {
        List<Person> targets = game.getCandidates("과학자");
        assertThrows(IllegalArgumentException.class, () -> game.startChallenge("과학자", Difficulty.LOW, targets.getLast(), true));
        for (Person target : targets.subList(0, 3)) { game.startBattle("과학자", Difficulty.LOW, target); completeBattle(); }
        assertTrue(game.profile().bossUnlocked("과학자")); assertTrue(game.profile().level() > 1);
        assertEquals(3, game.profile().cleared("과학자")); assertEquals(3, game.ownedCards().size());
        game.equip(targets.getFirst()); assertEquals(CardPower.key(targets.getFirst()), game.equippedPower().personKey());
        game.startChallenge("과학자", Difficulty.LOW, targets.getLast(), true); assertEquals(8, game.getSession().getQuestionCount()); completeBattle();
        assertEquals(1, game.profile().bossWins()); assertTrue(Achievement.BOSS.unlocked(game.profile()));
        int xp = game.profile().experience(), coins = game.profile().coins(); game.saveRewards();
        assertEquals(xp, game.profile().experience()); assertEquals(coins, game.profile().coins());
        SaveFactory reloaded = new SaveFactory(save.getPath()); reloaded.open();
        assertEquals(xp, reloaded.progress().snapshot().experience()); assertEquals(1, reloaded.progress().snapshot().bossWins());
        assertEquals(game.profile().equipped(), reloaded.progress().snapshot().equipped());
    }
    @Test void practiceUsesAllPeopleAndVariedProblemsWithoutRecentRepeats() throws Exception {
        Set<String> people = new HashSet<>(), ids = new HashSet<>();
        for (int run = 0; run < 8; run++) {
            game.startPractice("과학자", Difficulty.LOW); Set<String> oneRun = new HashSet<>();
            while (game.getSession().getPhase() != GameSession.Phase.FINISHED) {
                GameSession session = game.getSession(); String name = session.getCurrentPerson().getName();
                assertTrue(oneRun.add(name)); people.add(name);
                if (run < 2) assertTrue(ids.add(session.currentEntry().id()), "recent question must not repeat while fresh choices exist");
                game.selectAnswer(session.getCurrentQuiz().getAnswer()); game.submitAnswer(); game.nextQuestion();
            }
            game.saveRewards();
        }
        assertEquals(20, people.size()); assertTrue(game.profile().experience() > 0); assertTrue(game.ownedCards().size() >= 10);
    }
    @Test void wrongAnswerReviewClearsTheNotebookAndPaysCompletionXp() throws Exception {
        game.startPractice("한국사", Difficulty.LOW); String wrong = game.getSession().currentEntry().id();
        game.selectAnswer(game.getSession().getCurrentQuiz().getAnswer() % 4 + 1); game.submitAnswer();
        assertTrue(game.profile().wrongQuestions().contains(wrong)); game.startReview();
        assertEquals(GameSession.Mode.REVIEW, game.getSession().getMode());
        game.selectAnswer(game.getSession().getCurrentQuiz().getAnswer()); game.submitAnswer(); game.nextQuestion(); game.saveRewards();
        assertTrue(game.profile().wrongQuestions().isEmpty()); assertEquals(6, game.profile().experience());
    }
    @Test void itemsChargeOnlyWhenUsableAndUnownedCardsCannotBeEquipped() throws Exception {
        Person target = game.getCandidates("과학자").getFirst();
        assertThrows(IllegalArgumentException.class, () -> game.equip(target)); game.progress().buy(ShopItem.TONIC);
        game.startBattle("과학자", Difficulty.LOW, target);
        assertThrows(IllegalArgumentException.class, () -> game.useItem(ShopItem.TONIC)); assertEquals(1, game.profile().inventory(ShopItem.TONIC));
        BattleEngine b = game.getSession().getBattle(); b.chooseAction(BattleEngine.Action.HEAVY); b.select(b.current().getAnswer() % 4); game.submitBattleAnswer(); game.advanceBattle();
        game.useItem(ShopItem.TONIC); assertEquals(0, game.profile().inventory(ShopItem.TONIC)); assertEquals(b.maxPlayerHp(), b.playerHp());
        assertThrows(IllegalArgumentException.class, () -> game.useItem(ShopItem.TONIC));
    }
    @Test void tutorialButtonsPersistCompletionWithoutDuplicatingRewards() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TutorialView view = new TutorialView(game); assertFalse(game.profile().flag("tutorial"));
            find(view, "tutorialNext").doClick(); find(view, "tutorialDemo1").doClick();
            for (int i = 0; i < 5; i++) find(view, "tutorialNext").doClick();
            assertTrue(game.profile().flag("tutorial")); assertEquals(ViewId.MENU, destination.get());
            assertEquals(120, game.profile().coins());
            view.reset(); for (int i = 0; i < 6; i++) find(view, "tutorialNext").doClick();
            assertEquals(120, game.profile().coins());
        });
        ProgressStore reloaded = new ProgressStore(save.progress().path()); reloaded.open(); assertTrue(reloaded.snapshot().flag("tutorial"));
    }
    @Test void actualHubButtonsBuyClaimEquipAndStartChallenges() throws Exception {
        Person target = game.getCandidates("과학자").getFirst(); game.startBattle("과학자", Difficulty.LOW, target); completeBattle(); game.progress().completeTutorial();
        SwingUtilities.invokeAndWait(() -> {
            AdventureView hub = new AdventureView(game); hub.refresh();
            int coins = game.profile().coins(); find(hub, "buySHIELD").doClick(); assertEquals(coins - 50, game.profile().coins());
            find(hub, "claimFIRST_STEPS").doClick(); assertTrue(Achievement.FIRST_STEPS.claimed(game.profile()));
            find(hub, "equip" + target.getName()).doClick(); assertEquals(CardPower.key(target), game.profile().equipped());
            find(hub, "startChallenge").doClick(); assertEquals(ViewId.QUESTION, destination.get());
        });
    }
    @Test void applicationSupportsReloginAndFirstUserTutorialRouting() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "requires a window system");
        SwingUtilities.invokeAndWait(() -> {
            Application app = null;
            try {
                PersonManager.getInstance().clear(); ImageManager.getInstance().clear(); CategoryFileManager.getInstance().clear();
                app = new Application(project); app.init(); app.load(); app.start();
                java.lang.reflect.Method login = Application.class.getDeclaredMethod("startPlayer", PlayerSession.class); login.setAccessible(true);
                java.lang.reflect.Field windowField = Application.class.getDeclaredField("mainWindow"); windowField.setAccessible(true);
                MainWindow window = (MainWindow)windowField.get(app);
                java.lang.reflect.Field frameField = MainWindow.class.getDeclaredField("frame"); frameField.setAccessible(true);
                JFrame frame = (JFrame)frameField.get(window); frame.setLocation(-5000, -5000);
                PlayerSession first = new PlayerSession("first", save);
                login.invoke(app, first); assertNotNull(find(frame, "tutorialNext"));
                for (int i = 0; i < 6; i++) find(frame, "tutorialNext").doClick();
                assertTrue(save.progress().snapshot().flag("tutorial")); find(frame, "returnToLogin").doClick();
                login.invoke(app, new PlayerSession("second", new SaveFactory(temp.resolve("second.txt"))));
                assertNotNull(find(frame, "tutorialNext")); find(frame, "tutorialLater").doClick(); find(frame, "returnToLogin").doClick();
                login.invoke(app, first);
                assertNull(findOrNull(frame, "tutorialNext")); assertNotNull(find(frame, "returnToLogin"));
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { if (app != null) app.shutdown(); }
        });
    }
    @Test void productionViewsRenderAndBattleControlsFinishAndSave() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                AdventureView hub = new AdventureView(game); hub.refresh(); render(hub, "adventure-preview.png");
                TutorialView tutorial = new TutorialView(game); render(tutorial, "tutorial-preview.png");
                game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
                BattleGUIEvent event = new BattleGUIEvent(game);
                BattlePanel panel = new BattlePanel(game.getSession().getBattle(), event, event::finish, event::returnToMenu);
                render(panel, "tactical-battle-preview.png");
                while (game.getSession().getBattle().phase() != BattleEngine.Phase.FINISHED) {
                    find(panel, "actionHEAVY").doClick(); find(panel, "choice" + (game.getSession().getBattle().current().getAnswer() - 1)).doClick();
                    find(panel, "submit").doClick(); find(panel, "next").doClick();
                }
                assertEquals(ViewId.RESULT, destination.get()); assertEquals(BattleEngine.Result.WIN, game.getSession().getBattle().result());
                ResultGUIEvent resultEvent = new ResultGUIEvent(game); ResultView result = new ResultView(resultEvent); resultEvent.refresh(result);
                render(result, "result-preview.png"); result.stopAnimations(); panel.stopAnimation();
                assertTrue(game.profile().experience() > 0);
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
    private void render(JComponent component, String name) throws Exception {
        component.setSize(1280, 960); layout(component); BufferedImage image = new BufferedImage(1280, 960, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics(); component.paint(graphics); graphics.dispose();
        Path output = project.resolve("build/" + name); ImageIO.write(image, "png", output.toFile());
        assertNotEquals(0, image.getRGB(640, 480));
    }
    private void layout(Container parent) { parent.doLayout(); for (Component c : parent.getComponents()) if (c instanceof Container child) layout(child); }
    private AbstractButton find(Container parent, String name) {
        AbstractButton b = findOrNull(parent, name); if (b == null) throw new AssertionError("Missing button " + name); return b;
    }
    private AbstractButton findOrNull(Container parent, String name) {
        for (Component c : parent.getComponents()) {
            if (name.equals(c.getName()) && c instanceof AbstractButton b) return b;
            if (c instanceof Container child) { AbstractButton b = findOrNull(child, name); if (b != null) return b; }
        }
        return null;
    }
}
