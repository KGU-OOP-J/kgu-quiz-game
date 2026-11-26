package org.kgu.application;

import org.kgu.collection.event.CollectionGUIEvent;
import org.kgu.collection.event.CollectionLifecycleEvent;
import org.kgu.collection.feature.CollectionFeature;
import org.kgu.collection.view.CollectionView;
import org.kgu.factory.CategoryFileFactory;
import org.kgu.factory.PersonFactory;
import org.kgu.factory.SettingFactory;
import org.kgu.game.event.*;
import org.kgu.game.feature.GameFeature;
import org.kgu.game.view.QuestionView;
import org.kgu.game.view.ResultView;
import org.kgu.login.event.LoginGUIEvent;
import org.kgu.login.event.LoginLifecycleEvent;
import org.kgu.login.feature.LoginFeature;
import org.kgu.login.view.LoginView;
import org.kgu.manager.CategoryFileManager;
import org.kgu.manager.ImageManager;
import org.kgu.manager.PersonManager;
import org.kgu.menu.event.MenuGUIEvent;
import org.kgu.menu.event.MenuLifecycleEvent;
import org.kgu.menu.feature.MenuFeature;
import org.kgu.menu.view.MenuView;
import org.kgu.model.Setting;
import org.kgu.setting.event.SettingGUIEvent;
import org.kgu.setting.event.SettingLifecycleEvent;
import org.kgu.setting.feature.SettingFeature;
import org.kgu.setting.view.SettingView;
import org.kgu.window.MainWindow;
import org.kgu.window.ViewId;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;


public class Application {
    public static final List<String> CATEGORIES = List.of("과학자", "교수님", "한국사", "아이돌");

    private final Path dataDirectory;
    private final Path imageDirectory;
    private final Path prologuePath;
    private org.kgu.story.feature.PrologueFeature prologue;

    private final CategoryFileManager categoryManager = CategoryFileManager.getInstance();
    private final PersonManager personManager = PersonManager.getInstance();
    private final ImageManager imageManager = ImageManager.getInstance();
    private final SettingFactory settingFactory;
    private final MainWindow mainWindow = new MainWindow(this::shutdown);

    public Application() { this(Path.of(".")); }
    public Application(Path runtimeDirectory) {
        dataDirectory = runtimeDirectory.resolve("data");
        imageDirectory = runtimeDirectory.resolve("images");
        prologuePath = runtimeDirectory.resolve("prologue-dialog.txt");
        settingFactory = new SettingFactory(runtimeDirectory.resolve("settings.properties"), new Setting());
    }

    public void init() {
        // 분야별 인물 데이터/사진 사전에 로드
        for (String category : CATEGORIES) {
            categoryManager.add(new CategoryFileFactory(dataDirectory.resolve(category + ".txt"), category));
            imageManager.register("ui/" + category, imageDirectory.resolve("ui").resolve(category + ".png"));
        }

        // 전체/배틀 UI에 필요한 이미지 사전에 로드
        imageManager.register("ui/전체", imageDirectory.resolve("ui/전체.png"));
        imageManager.register("ui/map", imageDirectory.resolve("ui/map.png"));
        imageManager.register("ui/map_ruin", imageDirectory.resolve("ui/map_ruin.png"));
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
                    imageManager.register(key, imageDirectory.resolve(key + ".png"));
                }
            }
        }
        prologue = new org.kgu.story.feature.PrologueFeature(prologuePath, () -> mainWindow.show(ViewId.LOGIN));
    }

    public void start() {
        org.kgu.story.event.PrologueGUIEvent prologueEvent = new org.kgu.story.event.PrologueGUIEvent(java.util.Objects.requireNonNull(prologue));
        org.kgu.story.view.PrologueView prologueView = new org.kgu.story.view.PrologueView(prologueEvent);
        mainWindow.register(prologueView, new org.kgu.story.event.PrologueLifecycleEvent(prologueView, prologueEvent));
        LoginView loginView = new LoginView();
        LoginFeature loginFeature = new LoginFeature(this::startPlayer);
        mainWindow.register(loginView, new LoginLifecycleEvent(loginView, new LoginGUIEvent(loginView, loginFeature)));
        mainWindow.show(ViewId.PROLOGUE);
    }

    private void startPlayer(PlayerSession playerSession) {
        mainWindow.clearPlayerViews();
        Consumer<ViewId> navigate = mainWindow::show;
        GameFeature gameFeature = new GameFeature(playerSession, personManager, imageManager, settingFactory, navigate);
        org.kgu.story.feature.DialogueFeature dialogueFeature = new org.kgu.story.feature.DialogueFeature(gameFeature::rescuePerson, gameFeature::finishDialogue);
        org.kgu.story.event.DialogueGUIEvent dialogueEvent = new org.kgu.story.event.DialogueGUIEvent(dialogueFeature, gameFeature::image);
        org.kgu.story.view.DialogueView dialogueView = new org.kgu.story.view.DialogueView(dialogueEvent);
        mainWindow.register(dialogueView, new org.kgu.story.event.DialogueLifecycleEvent(dialogueView, dialogueEvent));
        MenuFeature menuFeature = new MenuFeature(gameFeature, navigate);
        CollectionFeature collectionFeature = new CollectionFeature(personManager, imageManager, playerSession, navigate);
        SettingFeature settingFeature = new SettingFeature(settingFactory, navigate);

        org.kgu.adventure.AdventureView adventure = new org.kgu.adventure.AdventureView(gameFeature);
        mainWindow.register(adventure, new org.kgu.window.LifecycleEvent() { @Override public void onEnter() { adventure.refresh(); } });
        org.kgu.adventure.TutorialView tutorial = new org.kgu.adventure.TutorialView(gameFeature);
        mainWindow.register(tutorial, new org.kgu.window.LifecycleEvent() { @Override public void onEnter() { tutorial.reset(); } });
        MenuView menuView = new MenuView();
        menuView.configureAdventure(tab -> { adventure.selectTab(tab); navigate.accept(ViewId.ADVENTURE); }, () -> navigate.accept(ViewId.TUTORIAL));
        menuView.setPlayerId(playerSession.playerId());
        mainWindow.register(menuView, new MenuLifecycleEvent(menuView, new MenuGUIEvent(menuView, menuFeature)));
        QuestionGUIEvent questionEvent = new QuestionGUIEvent(gameFeature);
        BattleGUIEvent battleEvent = new BattleGUIEvent(gameFeature);
        QuestionView questionView = new QuestionView(questionEvent, battleEvent);
        mainWindow.register(questionView, new QuestionLifecycleEvent(questionView));
        ResultGUIEvent resultEvent = new ResultGUIEvent(gameFeature);
        ResultView resultView = new ResultView(resultEvent);
        mainWindow.register(resultView, new ResultLifecycleEvent(resultView, resultEvent));
        CollectionGUIEvent collectionEvent = new CollectionGUIEvent(collectionFeature);
        CollectionView collectionView = new CollectionView(collectionEvent);
        collectionView.configureAdventure(category -> { adventure.selectVillage(category); navigate.accept(ViewId.ADVENTURE); });
        mainWindow.register(collectionView, new CollectionLifecycleEvent(collectionView, collectionEvent));
        SettingView settingView = new SettingView();
        mainWindow.register(settingView, new SettingLifecycleEvent(settingView, new SettingGUIEvent(settingView, settingFeature)));
        mainWindow.show(gameFeature.profile().flag("tutorial") ? ViewId.MENU : ViewId.TUTORIAL);
    }

    public void run() {
        start();
    }

    public void shutdown() {
        mainWindow.close();
    }
}
