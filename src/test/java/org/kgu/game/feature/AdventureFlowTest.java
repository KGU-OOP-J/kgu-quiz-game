package org.kgu.domain.game.feature;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.domain.adventure.view.*;
import org.kgu.domain.adventure.feature.AdventureFeature;
import org.kgu.core.application.*;
import org.kgu.core.factory.*;
import org.kgu.domain.collection.feature.CollectionFeature;
import org.kgu.domain.collection.event.CollectionGUIEvent;
import org.kgu.domain.collection.view.CollectionView;
import org.kgu.domain.game.view.component.*;
import org.kgu.domain.game.event.*;
import org.kgu.domain.game.view.*;
import org.kgu.core.manager.*;
import org.kgu.core.model.*;
import org.kgu.core.progression.*;
import org.kgu.core.window.*;
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
            CategoryFileFactory file = new CategoryFileFactory("/questions/" + category + ".txt", category); file.load();
            for (PersonFactory person : file.create()) {
                people.add(person);
                for (int i = 1; i <= 3; i++) images.registerResource("cards/" + person.create().getName() + "_" + i,
                    "/images/" + person.create().getName() + "_" + i + ".png");
            }
        }
        save = new SaveFactory(temp.resolve("player.txt")); save.open();
        game = new GameFeature(new PlayerSession("테스터", save), people, images,
            new SettingFactory(temp.resolve("settings"), new Setting()), destination::set, new Random(17));
    }
    private void completeBattle() throws Exception {
        BattleEngine battle = game.getSession().getBattle();
        while (battle.phase() != BattleEngine.Phase.FINISHED) {
            game.selectBattleAnswer(battle.current().getAnswer() - 1);
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
        game.startChallenge("과학자", Difficulty.LOW, targets.getLast(), true); assertEquals(5, game.getSession().getQuestionCount()); completeBattle();
        assertEquals(1, game.profile().bossWins()); assertTrue(Achievement.BOSS.unlocked(game.profile()));
        int xp = game.profile().experience(), coins = game.profile().coins(); game.saveRewards();
        assertEquals(xp, game.profile().experience()); assertEquals(coins, game.profile().coins());
        SaveFactory reloaded = new SaveFactory(save.getPath()); reloaded.open();
        assertEquals(xp, reloaded.progress().snapshot().experience()); assertEquals(1, reloaded.progress().snapshot().bossWins());
        assertEquals(game.profile().equipped(), reloaded.progress().snapshot().equipped());
    }
    @Test void mergedResultKeepsNewCardAndSettlementAcrossRefreshAndRepeatWin() throws Exception {
        Person target = game.getCandidates("과학자").getFirst();
        game.startBattle("과학자", Difficulty.LOW, target);
        completeBattle();
        ResultPresentation first = ResultGUIEvent.presentation(game.getSession(), game.getSavedRewards(), "", false);
        assertEquals(ResultPresentation.CardStatus.NEW, first.cardRewards().getFirst().status());
        var settlement = first.settlement().orElseThrow();
        assertEquals(120, settlement.xpEarned());
        assertEquals(70, settlement.coinsEarned());
        int xp = game.profile().experience(), coins = game.profile().coins();
        game.saveRewards();
        ResultPresentation refreshed = ResultGUIEvent.presentation(game.getSession(), game.getSavedRewards(), "", false);
        assertEquals(first, refreshed);
        assertEquals(xp, game.profile().experience());
        assertEquals(coins, game.profile().coins());
        game.startBattle("과학자", Difficulty.LOW, target);
        completeBattle();
        ResultPresentation repeated = ResultGUIEvent.presentation(game.getSession(), game.getSavedRewards(), "", false);
        assertEquals(ResultPresentation.CardStatus.ALREADY_OWNED, repeated.cardRewards().getFirst().status());
        assertEquals(1, save.create().getCollectedCount());
    }

    @Test void collectionAndBattleShareUnlockAndAnimationIsOncePerAccount() throws Exception {
        PlayerSession player = new PlayerSession("테스터", save);
        CollectionFeature feature = new CollectionFeature(PersonManager.getInstance(), ImageManager.getInstance(), player, destination::set);
        CollectionGUIEvent event = new CollectionGUIEvent(feature);
        ImageManager.getInstance().registerResource("ui/map", "/images/ui/map.png");
        assertFalse(event.isVillageOpen("교수님"));
        assertFalse(game.categoryUnlocked("교수님"));
        assertFalse(event.takeUnlockMotion("교수님"));
        SwingUtilities.invokeAndWait(() -> {
            CollectionView view = new CollectionView(event);
            event.refresh(view); view.showMap();
            try { render(view, "collection-locked-preview.png"); }
            catch (Exception e) { throw new AssertionError(e); }
            finally { view.stopAnimations(); }
        });
        for (int i = 0; i < PlayerProgress.PROFESSOR_REQUIRED - 1; i++)
            game.progress().answer("unlock-test-" + i, "과학자", true);
        assertFalse(event.isVillageOpen("교수님"));
        game.progress().answer("unlock-test-last", "아이돌", true);
        assertTrue(game.categoryUnlocked("교수님"));
        assertTrue(event.isVillageOpen("교수님"));
        assertEquals(20, feature.getPersons("교수님").size());
        assertTrue(event.takeUnlockMotion("교수님"));
        assertFalse(event.takeUnlockMotion("교수님"));
        SaveFactory reloaded = new SaveFactory(save.getPath()); reloaded.open();
        CollectionGUIEvent reopened = new CollectionGUIEvent(new CollectionFeature(PersonManager.getInstance(),
            ImageManager.getInstance(), new PlayerSession("테스터", reloaded), destination::set));
        assertTrue(reopened.isVillageOpen("교수님"));
        assertFalse(reopened.takeUnlockMotion("교수님"));
        SaveFactory another = new SaveFactory(temp.resolve("another.txt")); another.open();
        CollectionGUIEvent other = new CollectionGUIEvent(new CollectionFeature(PersonManager.getInstance(),
            ImageManager.getInstance(), new PlayerSession("다른 사용자", another), destination::set));
        assertFalse(other.isVillageOpen("교수님"));
        assertFalse(other.takeUnlockMotion("교수님"));
        SwingUtilities.invokeAndWait(() -> {
            CollectionView view = new CollectionView(reopened);
            reopened.refresh(view); view.showMap();
            try { render(view, "collection-unlocked-preview.png"); }
            catch (Exception e) { throw new AssertionError(e); }
            finally { view.stopAnimations(); }
        });
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
        game.useItem(ShopItem.TONIC);
        assertEquals(0, game.profile().inventory(ShopItem.TONIC));
        assertTrue(game.getSession().getBattle().memoryVisible());
        assertThrows(IllegalArgumentException.class, () -> game.useItem(ShopItem.TONIC));
        assertThrows(IllegalArgumentException.class, () -> game.useItem(ShopItem.SHIELD));

    }
    @Test void retryCreditsWinButNeverDuplicatesLearningOrCurrency() throws Exception {
        Person target = game.getCandidates("과학자").getFirst();
        game.progress().buy(ShopItem.SHIELD);
        game.startBattle("과학자", Difficulty.LOW, target);
        game.useItem(ShopItem.SHIELD);
        BattleEngine battle = game.getSession().getBattle();
        String wrongId = "";
        for (int i = 0; i < 5; i++) {
            if (i == 2) wrongId = game.getSession().currentEntry().id();
            game.selectBattleAnswer(i < 2 ? battle.current().getAnswer() - 1 : battle.current().getAnswer() % 4);
            game.submitBattleAnswer();
            game.advanceBattle();
        }
        assertTrue(battle.isRetry());
        assertEquals(wrongId, game.getSession().currentEntry().id());
        assertSame(battle.current(), game.getSession().getCurrentQuiz());
        int before = game.profile().coins();
        game.selectBattleAnswer(battle.current().getAnswer() - 1);
        game.submitBattleAnswer();
        game.advanceBattle();
        assertEquals(2, game.profile().number("correct"));
        assertEquals(5, game.profile().number("attempts"));
        assertEquals(2, game.profile().rescueProgress());
        assertTrue(game.profile().wrongQuestions().contains(wrongId));
        game.saveRewards();
        game.saveRewards();
        assertEquals(84, game.profile().experience());
        assertEquals(before + 52, game.profile().coins());
        assertEquals(1, game.getSavedRewards().size());
        assertEquals(1, battle.stars());
        ResultPresentation result = ResultGUIEvent.presentation(game.getSession(), game.getSavedRewards(), "", false);
        assertEquals(2, result.correctCount());
        assertEquals(5, result.answeredCount());
        assertEquals(1, result.battle().orElseThrow().retryCorrectCount());
        assertTrue(result.battle().orElseThrow().victory());
        assertEquals(84, result.settlement().orElseThrow().xpEarned());
        assertEquals(52, result.settlement().orElseThrow().coinsEarned());
        SaveFactory reloaded = new SaveFactory(save.getPath());
        reloaded.open();
        assertEquals(0, reloaded.progress().snapshot().inventory(ShopItem.SHIELD));
    }

    @Test void unavailableItemDoesNotApplyAndFailedSubmissionKeepsQuestionState() throws Exception {
        game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
        BattleEngine battle = game.getSession().getBattle();
        assertThrows(IllegalArgumentException.class, () -> game.useItem(ShopItem.INSIGHT));
        assertTrue(battle.itemUsed().isEmpty());
        assertTrue(battle.eliminated().isEmpty());
        // Block the atomic temporary file without affecting the player's actual save.
        Path temporary = save.progress().path().resolveSibling(save.progress().path().getFileName() + ".tmp");
        Files.createDirectories(temporary);
        game.selectBattleAnswer(battle.current().getAnswer() - 1);
        assertThrows(java.io.UncheckedIOException.class, game::submitBattleAnswer);
        assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
        assertEquals(0, battle.correctCount());
    }

    @Test void tutorialButtonsPersistCompletionWithoutDuplicatingRewards() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TutorialView view = new TutorialView(new AdventureFeature(game)); assertFalse(game.profile().flag("tutorial"));
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
            AdventureView hub = new AdventureView(new AdventureFeature(game)); hub.refresh();
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
                // This test exercises login routing after the separate prologue flow.
                window.show(ViewId.LOGIN);
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
                AdventureView hub = new AdventureView(new AdventureFeature(game)); hub.refresh(); render(hub, "adventure-preview.png");
                TutorialView tutorial = new TutorialView(new AdventureFeature(game)); render(tutorial, "tutorial-preview.png");
                game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
                BattleGUIEvent event = new BattleGUIEvent(game);
                BattlePanelDriver driver = new BattlePanelDriver(game.getSession().getBattle(), event, event::finish, event::returnToMenu);
                BattlePanel panel = driver.panel();
                render(panel, "quiz-battle-preview.png");
                while (game.getSession().getBattle().phase() != BattleEngine.Phase.FINISHED) {
                    find(panel, "choice" + (game.getSession().getBattle().current().getAnswer() - 1)).doClick();
                    find(panel, "submit").doClick(); driver.finishAnimation();
                }
                assertEquals(ViewId.DIALOGUE, destination.get());
                assertEquals(game.getSession().getBattle().computer(), game.rescuePerson());
                game.finishDialogue();
                assertEquals(ViewId.RESULT, destination.get()); assertEquals(BattleEngine.Result.WIN, game.getSession().getBattle().result());
                ResultGUIEvent resultEvent = new ResultGUIEvent(game); ResultView result = new ResultView(resultEvent); resultEvent.refresh(result);
                render(result, "result-preview.png"); result.stopAnimations(); panel.stopAnimation();
                assertTrue(game.profile().experience() > 0);
            } catch (Exception ex) { throw new RuntimeException(ex); }
        });
    }
    @Test void wrongAnswerDoesNotHitPortraitAndEnterCannotSkipFeedback() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            var finishes = new java.util.concurrent.atomic.AtomicInteger();
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
            BattlePanel panel = driver.panel();
            try {
                assertFalse(find(panel, "submit").isEnabled());
                assertNull(findOrNull(panel, "next"));
                assertNull(findOrNull(panel, "actionHEAVY"));
                find(panel, "choice" + (battle.current().getAnswer() % 4)).doClick();
                find(panel, "submit").doClick();
                assertFalse(driver.portraitAnimating());
                panel.getActionMap().get("submit").actionPerformed(null);
                assertEquals(0, battle.turn());
                assertEquals(1, battle.answeredCount());
                assertEquals(BattleEngine.Phase.FEEDBACK, battle.phase());
                driver.finishAnimation();
                assertEquals(1, battle.turn());
                assertEquals(BattleEngine.Phase.QUESTION, battle.phase());
                assertEquals(0, finishes.get());
                find(panel, "choice" + (battle.current().getAnswer() - 1)).doClick();
                find(panel, "submit").doClick();
                assertTrue(driver.portraitAnimating());
                for (int i = 0; i < 5; i++) driver.tick();
                render(panel, "quiz-battle-hit-preview.png");
                panel.stopAnimation();
                driver.finishAnimation();
                panel.getActionMap().get("submit").actionPerformed(null);
                assertEquals(1, battle.turn());
                assertEquals(0, finishes.get());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { panel.stopAnimation(); }
        });
    }

    @Test void professorDefeatPlaysFBeforeOneResultCallback() throws Exception {
        for (int i = 0; i < 30; i++) game.progress().answer("unlock-" + i, "과학자", true);
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("교수님", Difficulty.LOW, game.getChallengeCandidates("교수님").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            var finishes = new java.util.concurrent.atomic.AtomicInteger();
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
            BattlePanel panel = driver.panel();
            try {
                for (int i = 0; i < 2; i++) {
                    find(panel, "choice" + (battle.current().getAnswer() % 4)).doClick();
                    find(panel, "submit").doClick();
                    driver.finishAnimation();
                }
                find(panel, "choice" + (battle.current().getAnswer() % 4)).doClick();
                find(panel, "submit").doClick();
                for (int i = 0; i < 65; i++) driver.tick();
                assertEquals(BattleEngine.Phase.FINISHED, battle.phase());
                assertEquals(BattleEngine.Result.LOSE, battle.result());
                assertTrue(driver.finalEffectRunning());
                assertEquals(0, finishes.get());
                for (int i = 0; i < 28; i++) driver.tick();
                render(panel, "quiz-battle-defeat-preview.png");
                driver.finishAnimation();
                assertEquals(1, finishes.get());
                driver.finishAnimation();
                panel.getActionMap().get("submit").actionPerformed(null);
                assertEquals(1, finishes.get());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { panel.stopAnimation(); }
        });
    }

    @Test void leavingDuringFinalEffectCancelsNavigation() throws Exception {
        for (int i = 0; i < 30; i++) game.progress().answer("unlock-" + i, "과학자", true);
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("교수님", Difficulty.LOW, game.getChallengeCandidates("교수님").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            var finishes = new java.util.concurrent.atomic.AtomicInteger();
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
            BattlePanel panel = driver.panel();
            for (int i = 0; i < 3; i++) {
                find(panel, "choice" + (battle.current().getAnswer() % 4)).doClick();
                find(panel, "submit").doClick();
                if (i < 2) driver.finishAnimation();
            }
            for (int i = 0; i < 65; i++) driver.tick();
            assertTrue(driver.finalEffectRunning());
            panel.removeNotify();
            assertFalse(driver.running());
            driver.finishAnimation();
            assertEquals(0, finishes.get());
        });
    }

    @Test void battleKeyboardSelectionRemainsUsableAtSmallWindowSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, () -> { }, () -> { });
            BattlePanel panel = driver.panel();
            try {
                panel.setSize(960, 720);
                layout(panel);
                for (int i = 0; i < 4; i++) {
                    AbstractButton choice = find(panel, "choice" + i);
                    assertTrue(panel.getBounds().contains(choice.getBounds()));
                    assertTrue(choice.getHeight() >= 44, "Answer targets remain usable after resizing");
                    assertFalse(choice.getBounds().intersects(find(panel, "submit").getBounds()));
                }
                panel.getActionMap().get("choice0").actionPerformed(null);
                assertEquals(0, battle.selected());
                assertTrue(find(panel, "submit").getText().contains("1번"));
                BufferedImage preview = new BufferedImage(960, 720, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = preview.createGraphics();
                panel.paint(graphics); graphics.dispose();
                ImageIO.write(preview, "png", project.resolve("build/quiz-battle-small-preview.png").toFile());
                panel.getActionMap().get("submit").actionPerformed(null);
                assertEquals(BattleEngine.Phase.FEEDBACK, battle.phase());
                assertFalse(find(panel, "choice0").isEnabled());
                driver.finishAnimation();
                assertEquals(1, battle.turn());
                assertFalse(find(panel, "submit").isEnabled());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { panel.stopAnimation(); }
        });
    }

    @Test void countdownPaintsShrinksAndResetsUnderTheInstalledTeamTheme() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameTheme.install();
            game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, () -> { }, () -> { });
            BattlePanel panel = driver.panel();
            try {
                panel.setSize(1280, 960); layout(panel);
                find(panel, "choice" + (battle.current().getAnswer() - 1)).doClick();
                find(panel, "submit").doClick();
                JProgressBar bar = driver.countdown();
                assertTrue(bar.isVisible()); assertEquals(1000, bar.getValue());
                BufferedImage full = new BufferedImage(bar.getWidth(), bar.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g = full.createGraphics(); bar.paint(g); g.dispose();
                int previous = bar.getValue();
                for (int i = 0; i < 24; i++) { driver.tick(); assertTrue(bar.getValue() <= previous); previous = bar.getValue(); }
                assertTrue(bar.getValue() > 0 && bar.getValue() < 500);
                BufferedImage half = new BufferedImage(bar.getWidth(), bar.getHeight(), BufferedImage.TYPE_INT_RGB);
                g = half.createGraphics(); bar.paint(g); g.dispose();
                int x = bar.getWidth() * 3 / 4, y = bar.getHeight() / 2;
                assertNotEquals(full.getRGB(x, y), half.getRGB(x, y), "Visible fill must shrink, not just its model value");
                driver.finishAnimation(); assertFalse(bar.isVisible());
                find(panel, "choice" + (battle.current().getAnswer() - 1)).doClick();
                find(panel, "submit").doClick();
                assertEquals(1000, bar.getValue()); assertTrue(bar.isVisible());
            } finally { panel.stopAnimation(); }
        });
    }

    @Test void victoryFinisherCompletesBeforeOneNavigationCallback() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            var finishes = new java.util.concurrent.atomic.AtomicInteger();
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
            BattlePanel panel = driver.panel();
            try {
                for (int i = 0; i < 5; i++) {
                    find(panel, "choice" + (battle.current().getAnswer() - 1)).doClick();
                    find(panel, "submit").doClick();
                    if (i < 4) driver.finishAnimation();
                }
                for (int i = 0; i < 45; i++) driver.tick();
                assertEquals(BattleEngine.Result.WIN, battle.result());
                assertTrue(driver.finishEffectRunning()); assertEquals(0, finishes.get());
                assertFalse(driver.countdown().isVisible());
                for (int i = 0; i < 34; i++) driver.tick();
                render(panel, "quiz-battle-victory-finisher-preview.png");
                assertEquals(0, finishes.get());
                driver.finishAnimation(); assertEquals(1, finishes.get());
                driver.finishAnimation(); assertEquals(1, finishes.get());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { panel.stopAnimation(); }
        });
    }

    @Test void nonProfessorAlsoFinishesThePlayerWhenVictoryBecomesImpossible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            game.startBattle("과학자", Difficulty.LOW, game.getCandidates("과학자").getFirst());
            BattleEngine battle = game.getSession().getBattle();
            BattleGUIEvent event = new BattleGUIEvent(game);
            var finishes = new java.util.concurrent.atomic.AtomicInteger();
            BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
            BattlePanel panel = driver.panel();
            try {
                for (int i = 0; i < 3; i++) {
                    find(panel, "choice" + (battle.current().getAnswer() % 4)).doClick(); find(panel, "submit").doClick();
                    if (i < 2) driver.finishAnimation();
                }
                for (int i = 0; i < 65; i++) driver.tick();
                assertEquals(3, battle.answeredCount()); assertEquals(BattleEngine.Result.LOSE, battle.result());
                assertTrue(driver.finishEffectRunning()); assertFalse(driver.finalEffectRunning());
                assertEquals(0, finishes.get());
                for (int i = 0; i < 32; i++) driver.tick();
                render(panel, "quiz-battle-enemy-finisher-preview.png");
                driver.finishAnimation(); assertEquals(1, finishes.get());
            } catch (Exception ex) { throw new RuntimeException(ex); }
            finally { panel.stopAnimation(); }
        });
    }

    @Test void allCategoryAttacksRenderThroughNormalHitsAndFinishers() throws Exception {
        for (int i = 0; i < 30; i++) game.progress().answer("category-unlock-" + i, "과학자", true);
        SwingUtilities.invokeAndWait(() -> {
            for (String category : Application.CATEGORIES) {
                game.startBattle(category, Difficulty.LOW, game.getChallengeCandidates(category).getFirst());
                BattleEngine battle = game.getSession().getBattle();
                BattleGUIEvent event = new BattleGUIEvent(game);
                var finishes = new java.util.concurrent.atomic.AtomicInteger();
                BattlePanelDriver driver = new BattlePanelDriver(battle, event, finishes::incrementAndGet, () -> { });
                BattlePanel panel = driver.panel();
                try {
                    for (int question = 0; question < 5; question++) {
                        find(panel, "choice" + (battle.current().getAnswer() - 1)).doClick();
                        find(panel, "submit").doClick();
                        if (question == 0) {
                            for (int i = 0; i < 4; i++) driver.tick();
                            render(panel, "attack-" + category + "-preview.png");
                        }
                        if (question < 4) driver.finishAnimation();
                    }
                    for (int i = 0; i < 53; i++) driver.tick();
                    render(panel, "charge-" + category + "-preview.png");
                    for (int i = 0; i < 15; i++) driver.tick();
                    assertTrue(driver.finishEffectRunning()); assertEquals(0, finishes.get());
                    render(panel, "finisher-" + category + "-preview.png");
                    driver.finishAnimation(); assertEquals(1, finishes.get());
                } catch (Exception ex) { throw new RuntimeException(ex); }
                finally { panel.stopAnimation(); }
            }
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
