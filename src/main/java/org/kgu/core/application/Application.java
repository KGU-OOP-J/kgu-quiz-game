package org.kgu.core.application;

import org.kgu.domain.adventure.feature.AdventureFeature;
import org.kgu.domain.collection.event.CollectionGUIEvent;
import org.kgu.domain.collection.event.CollectionLifecycleEvent;
import org.kgu.domain.collection.feature.CollectionFeature;
import org.kgu.domain.collection.view.CollectionView;
import org.kgu.core.factory.CategoryFileFactory;
import org.kgu.core.factory.PersonFactory;
import org.kgu.core.factory.SettingFactory;
import org.kgu.domain.game.event.*;
import org.kgu.domain.game.feature.GameFeature;
import org.kgu.domain.game.feature.QuestionBank;
import org.kgu.domain.game.view.QuestionView;
import org.kgu.domain.game.view.ResultView;
import org.kgu.domain.login.event.LoginGUIEvent;
import org.kgu.domain.login.event.LoginLifecycleEvent;
import org.kgu.domain.login.feature.LoginFeature;
import org.kgu.domain.login.view.LoginView;
import org.kgu.core.manager.CategoryFileManager;
import org.kgu.core.manager.ImageManager;
import org.kgu.core.manager.PersonManager;
import org.kgu.domain.menu.event.MenuGUIEvent;
import org.kgu.domain.menu.event.MenuLifecycleEvent;
import org.kgu.domain.menu.feature.MenuFeature;
import org.kgu.domain.menu.view.MenuView;
import org.kgu.core.model.Setting;
import org.kgu.domain.setting.event.SettingGUIEvent;
import org.kgu.domain.setting.event.SettingLifecycleEvent;
import org.kgu.domain.setting.feature.SettingFeature;
import org.kgu.domain.setting.view.SettingView;
import org.kgu.core.window.MainWindow;
import org.kgu.core.window.ViewId;

import org.kgu.core.factory.SaveFactory;
import org.kgu.domain.showcase.feature.ShowcaseFeature;
import org.kgu.domain.showcase.feature.ShowcaseScene;
import org.kgu.domain.showcase.view.ShowcaseView;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;


public class Application {
    public static final List<String> CATEGORIES = org.kgu.core.model.Categories.ALL;

    private org.kgu.domain.story.feature.PrologueFeature prologue;

    private final CategoryFileManager categoryManager = CategoryFileManager.getInstance();
    private final PersonManager personManager = PersonManager.getInstance();
    private final ImageManager imageManager = ImageManager.getInstance();
    private final SettingFactory settingFactory;
    private MainWindow showcaseWindow;
    private final MainWindow mainWindow = new MainWindow(this::shutdown);

    public Application() { this(Path.of(".")); }
    public Application(Path runtimeDirectory) {
        settingFactory = new SettingFactory(runtimeDirectory.resolve("settings.properties"), new Setting());
    }

    public void init() {
        // 분야별 인물 데이터/사진 사전에 로드
        for (String category : CATEGORIES) {
            categoryManager.add(new CategoryFileFactory("/questions/" + category + ".txt", category));
            imageManager.registerResource("ui/" + category, "/images/ui/" + category + ".png");
        }

        // 전체/배틀 UI에 필요한 이미지 사전에 로드
        imageManager.registerResource("ui/전체", "/images/ui/전체.png");
        imageManager.registerResource("ui/map", "/images/ui/map.png");
        imageManager.registerResource("battle/background", "/org/kgu/battle/campus_background.png");
    }

    public void load() throws IOException {
        int expectedCount = -1;
        for (CategoryFileFactory categoryFactory : categoryManager.getAll()) {
            categoryFactory.load();
            int count = categoryFactory.create().size();
            if (count == 0 || (expectedCount != -1 && count != expectedCount)) {
                throw new IOException("카테고리별 인물 수는 동일하고 0보다 커야 합니다: " + categoryFactory.getPath());
            }
            expectedCount = count;
        }
        settingFactory.open();
        for (CategoryFileFactory categoryFactory : categoryManager.getAll()) {
            for (PersonFactory personFactory : categoryFactory.create()) {
                personManager.add(personFactory);
                for (int stage = 1; stage <= 3; stage++) {
                    String key = "cards/" + personFactory.create().getName() + "_" + stage;
                    imageManager.registerResource(key, "/images/" + personFactory.create().getName() + "_" + stage + ".png");
                }
            }
        }
        prologue = new org.kgu.domain.story.feature.PrologueFeature(() -> mainWindow.show(ViewId.LOGIN));
    }

    public void start() {
        org.kgu.domain.story.event.PrologueGUIEvent prologueEvent = new org.kgu.domain.story.event.PrologueGUIEvent(java.util.Objects.requireNonNull(prologue));
        org.kgu.domain.story.view.PrologueView prologueView = new org.kgu.domain.story.view.PrologueView(prologueEvent);
        mainWindow.register(prologueView, new org.kgu.domain.story.event.PrologueLifecycleEvent(prologueView, prologueEvent));
        LoginView loginView = new LoginView();
        LoginFeature loginFeature = new LoginFeature(this::startPlayer);
        mainWindow.register(loginView, new LoginLifecycleEvent(loginView, new LoginGUIEvent(loginView, loginFeature)));
        mainWindow.bindShowcase(() -> openShowcase(mainWindow));
        mainWindow.show(ViewId.PROLOGUE);
    }

    private void startPlayer(PlayerSession playerSession) {
        mainWindow.clearPlayerViews();
        var views = registerPlayer(mainWindow, playerSession, settingFactory, mainWindow::show, new Random());
        mainWindow.show(views.game().profile().flag("tutorial") ? ViewId.MENU : ViewId.TUTORIAL);
    }

    private record PlayerViews(GameFeature game, QuestionView question,
                               org.kgu.domain.adventure.view.AdventureView adventure) { }

    private PlayerViews registerPlayer(MainWindow window, PlayerSession playerSession,
                                       SettingFactory settings, Consumer<ViewId> navigate, Random random) {
        GameFeature gameFeature = new GameFeature(playerSession, personManager, imageManager, settings, navigate, random);
        org.kgu.domain.story.feature.DialogueFeature dialogueFeature = new org.kgu.domain.story.feature.DialogueFeature(gameFeature::rescuePerson, gameFeature::finishDialogue);
        org.kgu.domain.story.event.DialogueGUIEvent dialogueEvent = new org.kgu.domain.story.event.DialogueGUIEvent(dialogueFeature, gameFeature::image);
        org.kgu.domain.story.view.DialogueView dialogueView = new org.kgu.domain.story.view.DialogueView(dialogueEvent);
        window.register(dialogueView, new org.kgu.domain.story.event.DialogueLifecycleEvent(dialogueView, dialogueEvent));
        MenuFeature menuFeature = new MenuFeature(gameFeature, navigate);
        CollectionFeature collectionFeature = new CollectionFeature(personManager, imageManager, playerSession, navigate);
        SettingFeature settingFeature = new SettingFeature(settings, navigate);

        var adventureFeature = new AdventureFeature(gameFeature);
        org.kgu.domain.adventure.view.AdventureView adventure = new org.kgu.domain.adventure.view.AdventureView(adventureFeature);
        window.register(adventure, new org.kgu.core.window.LifecycleEvent() { @Override public void onEnter() { adventure.refresh(); } });
        org.kgu.domain.adventure.view.TutorialView tutorial = new org.kgu.domain.adventure.view.TutorialView(adventureFeature);
        window.register(tutorial, new org.kgu.core.window.LifecycleEvent() { @Override public void onEnter() { tutorial.reset(); } });
        MenuView menuView = new MenuView();
        menuView.configureAdventure(tab -> { adventure.selectTab(tab); navigate.accept(ViewId.ADVENTURE); }, () -> navigate.accept(ViewId.TUTORIAL));
        menuView.setPlayerId(playerSession.playerId());
        window.register(menuView, new MenuLifecycleEvent(menuView, new MenuGUIEvent(menuView, menuFeature)));
        QuestionGUIEvent questionEvent = new QuestionGUIEvent(gameFeature);
        BattleGUIEvent battleEvent = new BattleGUIEvent(gameFeature);
        QuestionView questionView = new QuestionView(questionEvent, battleEvent);
        window.register(questionView, new QuestionLifecycleEvent(questionView));
        ResultGUIEvent resultEvent = new ResultGUIEvent(gameFeature);
        ResultView resultView = new ResultView(resultEvent);
        window.register(resultView, new ResultLifecycleEvent(resultView, resultEvent));
        CollectionGUIEvent collectionEvent = new CollectionGUIEvent(collectionFeature);
        CollectionView collectionView = new CollectionView(collectionEvent);
        collectionView.configureAdventure(category -> { adventure.selectVillage(category); navigate.accept(ViewId.ADVENTURE); });
        window.register(collectionView, new CollectionLifecycleEvent(collectionView, collectionEvent));
        SettingView settingView = new SettingView();
        window.register(settingView, new SettingLifecycleEvent(settingView, new SettingGUIEvent(settingView, settingFeature)));
        return new PlayerViews(gameFeature, questionView, adventure);
    }

    private void openShowcase(MainWindow owner) {
        ShowcaseView.choose(owner.dialogOwner()).ifPresent(scene -> {
            closeShowcase();
            try {
                launchShowcase(scene);
            } catch (IOException | RuntimeException ex) {
                closeShowcase();
                JOptionPane.showMessageDialog(mainWindow.dialogOwner(), ex.getMessage(), "시연 시작 실패", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void launchShowcase(ShowcaseScene scene) throws IOException {
        Path directory = Files.createTempDirectory("kgu-showcase-");
        directory.toFile().deleteOnExit();
        Path savePath = directory.resolve("showcase.txt");
        Path settingsPath = directory.resolve("settings.properties");
        var save = new SaveFactory(savePath);
        var settings = new SettingFactory(settingsPath, new Setting());
        var window = new MainWindow(this::closeShowcase);
        showcaseWindow = window;
        window.setTitle("WHO’S NEXT? — 시연 · " + scene);
        window.bindShowcase(() -> openShowcase(window));
        var views = registerPlayer(window, new PlayerSession("시연 플레이어", save), settings, id -> {
            if (id == ViewId.LOGIN) closeShowcase();
            else window.show(id);
        }, new Random(0));
        savePath.toFile().deleteOnExit();
        save.progress().path().toFile().deleteOnExit();
        settingsPath.toFile().deleteOnExit();

        new ShowcaseFeature(views.game(), save, QuestionBank.all(personManager.getAll()), window::show, views.adventure()::selectTab,
            views.question()::playShowcaseAnswers).open(scene);
    }

    private void closeShowcase() {
        if (showcaseWindow == null) return;
        showcaseWindow.close();
        showcaseWindow = null;
    }

    public void run() {
        start();
    }

    public void shutdown() {
        closeShowcase();
        mainWindow.close();
    }
}
