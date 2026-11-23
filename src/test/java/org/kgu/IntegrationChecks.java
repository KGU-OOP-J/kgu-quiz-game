package org.kgu;

import org.kgu.application.*;
import org.kgu.collection.event.*;
import org.kgu.collection.feature.CollectionFeature;
import org.kgu.collection.view.*;
import org.kgu.factory.*;
import org.kgu.game.event.*;
import org.kgu.game.feature.*;
import org.kgu.game.view.*;
import org.kgu.login.event.*;
import org.kgu.login.feature.LoginFeature;
import org.kgu.login.view.LoginView;
import org.kgu.manager.*;
import org.kgu.menu.event.*;
import org.kgu.menu.feature.MenuFeature;
import org.kgu.menu.view.MenuView;
import org.kgu.model.*;
import org.kgu.setting.event.*;
import org.kgu.setting.feature.SettingFeature;
import org.kgu.setting.view.SettingView;
import org.kgu.window.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.*;

/** 외부 라이브러리 없이 실행 가능하며 모든 저장 쓰기는 MemoryFileSystem에 한정한다. */
public final class IntegrationChecks {
    private static int checks;
    private static final PersonManager personManager = PersonManager.getInstance();
    private static final CategoryFileManager categoryManager = CategoryFileManager.getInstance();
    private static final ImageManager imageManager = ImageManager.getInstance();
    private static final MemoryFileSystem memory = new MemoryFileSystem();
    private static final AtomicReference<ViewId> destination = new AtomicReference<>();
    private static final SettingFactory settingFactory = new SettingFactory(memory.getPath("/settings.properties"), new Setting());
    private static final SaveFactory saveFactory = new SaveFactory(memory.getPath("/saves/player.txt"));
    private static final PlayerSession playerSession = new PlayerSession("player", saveFactory);

    public static void main(String[] args) throws Exception {
        check(GraphicsEnvironment.isHeadless(), "UI checks render to memory in headless mode");
        loadAndCheckFactories();
        checkPersistence();
        checkPractice();
        checkBattles();
        SwingUtilities.invokeAndWait(() -> {
            try { checkViews(); }
            catch (Exception e) { throw new AssertionError("View checks", e); }
        });
        System.out.println("PASS: " + checks + " integration checks; all test writes stayed in memory.");
    }

    private static void loadAndCheckFactories() throws Exception {
        String managerId = personManager.getId();
        for (String category : Application.CATEGORIES) {
            CategoryFileFactory factory = new CategoryFileFactory(Path.of("data", category + ".txt"), category);
            String id = factory.getId();
            factory.load();
            check(factory.create().size() == 20, "20 persons per category");
            for (PersonFactory person : factory.create()) {
                check(person.create() == person.create(), "Person identity");
                check(person.create().getCategory().equals(category), "category metadata");
                check(person.getQuizFactory().create().size() == 15, "fifteen quizzes per person");
                for (Difficulty difficulty : Difficulty.values()) {
                    check(person.getQuizFactory().get(difficulty).size() == 5, "five quizzes per difficulty");
                }
            }
            factory.reload();
            check(id.equals(factory.getId()), "same CategoryFileFactory ID on reload");
            categoryManager.add(factory);
            for (PersonFactory person : factory.create()) {
                personManager.add(person);
                check(personManager.get(person.getId()) == person, "shared PersonFactory reference");
                for (int stage = 1; stage <= 3; stage++) {
                    String key = "cards/" + person.create().getName() + "_" + stage;
                    Path path = Path.of("images", key + ".png");
                    check(Files.isRegularFile(path), "every person stage has an image");
                    imageManager.register(key, path);
                }
            }
            imageManager.register("ui/" + category, Path.of("images/ui", category + ".png"));
        }
        imageManager.register("ui/전체", Path.of("images/ui/전체.png"));
        imageManager.registerResource("battle/background", "/org/kgu/battle/campus_background.png");
        check(categoryManager.size() == 4 && personManager.size() == 80, "four category managers / 80 people");
        check(imageManager.size() == 246, "246 lazy image factories");
        var imageField = ImageFileFactory.class.getDeclaredField("image");
        imageField.setAccessible(true);
        for (ImageFileFactory factory : imageManager.getAll()) {
            check(((Optional<?>) imageField.get(factory)).isEmpty(), "images remain lazy after registration");
        }
        ImageFileFactory image = imageManager.getAll().get(0);
        String id = image.getId();
        BufferedImage loaded = image.create();
        check(loaded == image.create(), "decoded image reused");
        image.reload(); check(id.equals(image.getId()), "image reload retains factory identity");
        check(managerId.equals(personManager.getId()), "manager ID does not depend on contents");
        Path badImage=memory.getPath("/bad-image.png");Files.writeString(badImage,"not an image");
        expect(java.io.UncheckedIOException.class,()->new ImageFileFactory(badImage).create());
        expect(IllegalArgumentException.class, () -> personManager.add(personManager.getAll().get(0)));
        Scanner in = new Scanner("이름\t소개\t정보1\t정보2\t정보3\n하\t질문\t1\t2\t3\t4\t1\n");
        Person.read(in); Quiz.read(in); check(!in.hasNextLine(), "models consume only their block and leave Scanner open"); in.close();
        for (Difficulty difficulty : Difficulty.values()) {
            check(Difficulty.fromLabel(difficulty.getLabel()) == difficulty, "difficulty round trip");
        }
        CategoryFileFactory malformed = new CategoryFileFactory(memory.getPath("/malformed.txt"), "과학자");
        Files.writeString(malformed.getPath(), "이름\t소개\t정보1\t정보2\t정보3\n", StandardCharsets.UTF_8);
        expect(IOException.class, malformed::load);
        check(malformed.create().isEmpty(), "partial category not published");
    }

    private static void checkPersistence() throws Exception {
        saveFactory.open(); check(saveFactory.create().getCollectedCount() == 0, "new user has a real empty save");
        check(!Files.exists(saveFactory.getPath()), "new user file not created before first reward");
        Person person = personManager.getAll().get(0).create();
        check(saveFactory.record(person, Difficulty.HIGH), "first reward persisted");
        long length = Files.size(saveFactory.getPath());
        check(!saveFactory.record(person, Difficulty.HIGH), "duplicate reward ignored");
        check(Files.size(saveFactory.getPath()) == length, "duplicate does not append");
        check(saveFactory.create().getStages(person.getCategory(), person.getName()).equals(List.of(3)), "high only does not grant lower stages");
        check(saveFactory.record(person, Difficulty.LOW), "independent low stage");
        check(saveFactory.create().getCollectedCount() == 1, "count unique persons, not stages");
        String id = saveFactory.getId(); saveFactory.reload();
        check(id.equals(saveFactory.getId()) && saveFactory.create().getCollectedCount() == 1, "reload save in place");
        SaveFactory other = new SaveFactory(memory.getPath("/saves/other.txt")); other.open();
        check(!other.create().isCollected(person.getCategory(), person.getName()), "per-user isolation");
        Person second = personManager.getAll().get(1).create();
        memory.failWrites = true;
        expect(IOException.class, () -> saveFactory.record(second, Difficulty.MEDIUM));
        check(!saveFactory.create().isCollected(second.getCategory(), second.getName()), "write failure cannot grant a ghost reward");
        memory.failWrites = false;
        check(saveFactory.record(second, Difficulty.MEDIUM), "failed reward can be retried");
        Path noNewline = memory.getPath("/saves/no-newline.txt");
        Files.writeString(noNewline, "과학자\t알베르트 아인슈타인\t하", StandardCharsets.UTF_8);
        SaveFactory append = new SaveFactory(noNewline); append.load(); append.record(second, Difficulty.HIGH); append.reload();
        check(append.create().getCollectedCount() == 2, "append repairs missing final newline");
        Path badSave = memory.getPath("/bad-save.txt");Files.writeString(badSave, "not a save");
        expect(IOException.class, () -> new SaveFactory(badSave).load());
        expect(IllegalArgumentException.class, () -> Save.read(new Scanner("과학자\t이름\t미정")));
        long validLength=Files.size(saveFactory.getPath());
        Person invalidPerson=new Person("bad\tcategory","이름",List.of("소개","1","2","3"));
        expect(IllegalArgumentException.class,()->saveFactory.record(invalidPerson,Difficulty.LOW));
        check(Files.size(saveFactory.getPath())==validLength,"invalid cells cannot corrupt the save");
        List<String> savedLines=saveFactory.create().getLines();saveFactory.save();saveFactory.reload();
        check(saveFactory.create().getLines().equals(savedLines),"explicit save round trip");
        check(SaveFactory.forPlayer("../../name").getPath().getParent().equals(Path.of("saves")), "ID cannot escape saves directory");
        expect(IllegalArgumentException.class, () -> new PlayerSession("  "));
        check(new PlayerSession("player", other).equals(playerSession), "player identity independent of mutable save");
        int hash = playerSession.hashCode();saveFactory.record(second, Difficulty.LOW);
        check(hash == playerSession.hashCode(), "player hash remains stable");
        settingFactory.open();check(settingFactory.create().isSoundEnabled(), "default sound setting");
        settingFactory.saveSoundEnabled(false);settingFactory.create().setSoundEnabled(true);settingFactory.reload();
        check(!settingFactory.create().isSoundEnabled(), "settings round trip");
        memory.failWrites = true;expect(IOException.class, () -> settingFactory.saveSoundEnabled(true));
        check(!settingFactory.create().isSoundEnabled(), "failed settings save does not alter applied setting");memory.failWrites = false;
        Path badSettings=memory.getPath("/bad-settings.properties");Files.writeString(badSettings,"soundEnabled=perhaps\n");
        expect(IOException.class, () -> new SettingFactory(badSettings,new Setting()).open());
    }

    private static GameFeature gameFeature() {
        return new GameFeature(playerSession, personManager, imageManager, settingFactory, destination::set, new Random(7));
    }

    private static void checkPractice() throws Exception {
        GameFeature game = gameFeature();game.startPractice("과학자", Difficulty.MEDIUM);
        check(destination.get() == ViewId.QUESTION, "practice navigation");
        GameSession session = game.getSession();Set<String> names = new HashSet<>();
        expect(IllegalStateException.class, game::submitAnswer);
        for (int i = 0; i < 10; i++) {
            check(names.add(session.getCurrentPerson().getName()), "one question per person");
            check(session.getQuestionIndex() == i, "practice question index");
            int answer = session.getCurrentQuiz().getAnswer();
            game.selectAnswer(i % 2 == 0 ? answer : answer % 4 + 1);
            check(game.submitAnswer() == (i % 2 == 0), "practice grading");
            expect(IllegalStateException.class, game::submitAnswer);
            game.saveRewards();game.nextQuestion();
        }
        check(session.getPhase() == GameSession.Phase.FINISHED && destination.get() == ViewId.RESULT, "practice completes after ten");
        check(session.getCorrectCount() == 5 && session.getRewards().size() == 5, "practice correct count and earned persons");
        check(game.getSavedRewards().size() == 5, "practice rewards really saved");
        check(game.saveRewards().isEmpty(), "reward retry is idempotent");
        game.returnToMenu();check(destination.get() == ViewId.MENU && game.getSession() == session, "menu return does not null/reset session");
    }

    private static void checkBattles() throws Exception {
        for (String category : Application.CATEGORIES) {
            for (Difficulty difficulty : Difficulty.values()) {
                for (int scenario = 0; scenario < 3; scenario++) {
                    GameFeature game = gameFeature();game.startBattle(category,difficulty,game.getCandidates(category).get(0));
                    BattleEngine battle = game.getSession().getBattle();
                    check(battle.player() != battle.computer(), "random opponent differs from player");
                    expect(IllegalStateException.class,battle::result);
                    expect(IllegalStateException.class,battle::submit);
                    expect(IllegalStateException.class,battle::feedback);
                    for (int turn = 0; turn < 6; turn++) {
                        check(battle.turn() == turn && battle.attackTurn() == (turn % 2 == 0), "alternating six turns");
                        int answer=battle.current().getAnswer()-1;
                        // 0: all correct -> win; 1: all wrong -> lose; 2: attacks wrong, defenses correct -> draw.
                        boolean correct=scenario==0||(scenario==2&&!battle.attackTurn());
                        battle.select(correct?answer:(answer+1)%4);
                        check(battle.submit().correct()==correct,"battle grading");
                        expect(IllegalStateException.class,battle::submit);
                        battle.advance();
                        check(game.getSession().getPhase()==(turn==5?GameSession.Phase.FINISHED:GameSession.Phase.QUESTION),"session exposes actual battle phase");
                        if(turn<5)expect(IllegalStateException.class,battle::feedback);
                    }
                    BattleEngine.Result expected=scenario==0?BattleEngine.Result.WIN:scenario==1?BattleEngine.Result.LOSE:BattleEngine.Result.DRAW;
                    check(battle.result()==expected,"win / lose / draw");
                    check(battle.rewards().size()==(scenario==0?2:0),"two rewards only on win");
                    check(battle.correctCount()==(scenario==0?6:scenario==1?0:3),"correct count includes defense");
                    check(battle.playerHp()==(scenario==1?0:60)&&battle.computerHp()==(scenario==0?0:60),"authoritative HP");
                    if(scenario==0){game.saveRewards();check(game.getSavedRewards().size()==2,"battle rewards use real save records");}
                    game.finishBattle();check(destination.get()==ViewId.RESULT,"battle result navigation");
                }
            }
        }
        GameFeature game=gameFeature();game.startBattle("과학자",Difficulty.LOW,game.getCandidates("과학자").get(0));
        BattleEngine battle=game.getSession().getBattle();
        for(int i=0;i<6;i++){int answer=battle.current().getAnswer()-1;battle.select(battle.attackTurn()?answer:(answer+1)%4);battle.submit();battle.advance();}
        check(battle.playerHp()==0&&battle.computerHp()==0&&battle.result()==BattleEngine.Result.DRAW,"simultaneous zero HP is draw after six turns");
    }

    private static void checkViews() throws Exception {
        GameFeature game=gameFeature();
        MenuView menu=new MenuView();MenuGUIEvent menuEvent=new MenuGUIEvent(menu,new MenuFeature(game,destination::set));
        MenuLifecycleEvent menuLifecycle=new MenuLifecycleEvent(menu,menuEvent);
        menuLifecycle.onEnter();check(menu.getGameButton().getActionListeners().length==1,"menu input bound");
        menu.getGameButton().doClick();menu.getCategoryBox().setSelectedItem("교수님");
        menu.getStartButton().doClick();check(game.getSession().getCategory().equals("교수님"),"setup inputs reach Feature");
        paint(menu,780,560);menuLifecycle.onExit();menuLifecycle.onEnter();
        check(menu.getGameButton().getActionListeners().length==1,"no duplicate input on reentry");menuLifecycle.onExit();
        QuestionGUIEvent questionEvent=new QuestionGUIEvent(game);BattleGUIEvent battleEvent=new BattleGUIEvent(game);
        QuestionView question=new QuestionView(questionEvent,battleEvent);QuestionLifecycleEvent lifecycle=new QuestionLifecycleEvent(question);
        lifecycle.onEnter();paint(question,780,560);
        game.selectAnswer(game.getSession().getCurrentQuiz().getAnswer());game.submitAnswer();question.showPractice();paint(question,780,560);lifecycle.onExit();
        game.startBattle("과학자",Difficulty.HIGH,game.getCandidates("과학자").get(0));lifecycle.onEnter();paint(question,1280,960);
        BattlePanel panel=(BattlePanel)question.getComponent(0);
        var timerField=BattlePanel.class.getDeclaredField("animation");timerField.setAccessible(true);javax.swing.Timer timer=(javax.swing.Timer)timerField.get(panel);
        timer.start();lifecycle.onExit();check(!timer.isRunning(),"battle timer stopped by lifecycle");
        for(int i=0;i<6;i++){
            findNamed(panel,"choice"+(game.getSession().getBattle().current().getAnswer()-1),AbstractButton.class).doClick();
            findNamed(panel,"submit",AbstractButton.class).doClick();
            findNamed(panel,"next",AbstractButton.class).doClick();
        }
        check(destination.get()==ViewId.RESULT&&game.getSession().getBattle().result()==BattleEngine.Result.WIN,"real battle buttons finish the game");
        ResultGUIEvent resultEvent=new ResultGUIEvent(game);ResultView result=new ResultView(resultEvent);resultEvent.refresh(result);paint(result,1280,960);
        CollectionFeature collectionFeature=new CollectionFeature(personManager,imageManager,playerSession,destination::set);
        CollectionGUIEvent collectionEvent=new CollectionGUIEvent(collectionFeature);CollectionView collection=new CollectionView(collectionEvent);
        collectionEvent.refresh(collection);paint(collection,780,560);
        check(collectionFeature.getCollectedCount()>0&&collectionFeature.getCollectedCount()<=80,"collection unique person count");
        collectionEvent.selectCategory(collection,"과학자");paint(collection,780,560);
        Person collected=collectionFeature.getPersons("과학자").stream().filter(collectionFeature::isCollected).findFirst().orElseThrow();
        collectionEvent.openPerson(collection,collected);paint(collection,780,560);collection.showList();
        AbstractButton nextPage=findText(collection,"2");nextPage.doClick();paint(collection,780,560);
        SettingView setting=new SettingView();SettingGUIEvent settingEvent=new SettingGUIEvent(setting,new SettingFeature(settingFactory,destination::set));
        SettingLifecycleEvent settingLifecycle=new SettingLifecycleEvent(setting,settingEvent);settingLifecycle.onEnter();
        setting.getApplyButton().doClick();paint(setting,780,560);settingLifecycle.onExit();
        AtomicReference<PlayerSession> loggedIn=new AtomicReference<>();LoginView login=new LoginView();
        LoginFeature loginFeature=new LoginFeature(loggedIn::set);LoginLifecycleEvent loginLifecycle=new LoginLifecycleEvent(login,new LoginGUIEvent(login,loginFeature));
        loginLifecycle.onEnter();login.getIdField().setText("integration-check-no-disk-save");login.getLoginButton().doClick();
        check(loggedIn.get().playerId().equals("integration-check-no-disk-save"),"ID login creates player session");
        check(!Files.exists(loggedIn.get().saveFactory().getPath()),"login alone never writes a save");paint(login,780,560);loginLifecycle.onExit();
        check(login.getLoginButton().getActionListeners().length==0,"login inputs unbound");
        checkResultRetry();
    }

    private static void checkResultRetry() throws Exception {
        SaveFactory retrySave=new SaveFactory(memory.getPath("/saves/retry.txt"));
        GameFeature game=new GameFeature(new PlayerSession("retry",retrySave),personManager,imageManager,settingFactory,destination::set,new Random(9));
        game.startBattle("한국사",Difficulty.HIGH,game.getCandidates("한국사").get(0));BattleEngine battle=game.getSession().getBattle();
        for(int i=0;i<6;i++){battle.select(battle.current().getAnswer()-1);battle.submit();battle.advance();}
        ResultGUIEvent event=new ResultGUIEvent(game);ResultView view=new ResultView(event);
        memory.failWrites=true;event.refresh(view);
        check(retrySave.create().getCollectedCount()==0,"result cannot fake successful save");
        AbstractButton retry=findText(view,"보상 저장 재시도");check(retry.isVisible(),"save failure exposes retry");
        memory.failWrites=false;retry.doClick();check(retrySave.create().getCollectedCount()==2,"result retry persists both rewards");
        check(!retry.isVisible(),"retry hidden only after successful persistence");paint(view,1280,960);
    }

    private static void paint(JComponent component,int width,int height){
        component.setSize(width,height);layout(component);
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics=image.createGraphics();component.paint(graphics);graphics.dispose();
        check(image.getRGB(width/2,height/2)!=0,"View paints in memory");
    }
    private static void layout(Container container){container.doLayout();for(Component component:container.getComponents())if(component instanceof Container)layout((Container)component);}
    private static <T extends Component> T findNamed(Container root,String name,Class<T> type){
        for(Component component:root.getComponents()){
            if(name.equals(component.getName())&&type.isInstance(component))return type.cast(component);
            if(component instanceof Container){T found=findNamedOrNull((Container)component,name,type);if(found!=null)return found;}
        }throw new AssertionError("Missing component "+name);
    }
    private static <T extends Component> T findNamedOrNull(Container root,String name,Class<T> type){
        for(Component component:root.getComponents()){
            if(name.equals(component.getName())&&type.isInstance(component))return type.cast(component);
            if(component instanceof Container){T found=findNamedOrNull((Container)component,name,type);if(found!=null)return found;}
        }return null;
    }
    private static AbstractButton findText(Container root,String text){
        AbstractButton result=findTextOrNull(root,text);if(result==null)throw new AssertionError("Missing button "+text);return result;
    }
    private static AbstractButton findTextOrNull(Container root,String text){
        for(Component component:root.getComponents()){
            if(component instanceof AbstractButton&&text.equals(((AbstractButton)component).getText()))return (AbstractButton)component;
            if(component instanceof Container){AbstractButton found=findTextOrNull((Container)component,text);if(found!=null)return found;}
        }return null;
    }
    private static void check(boolean condition,String description){if(!condition)throw new AssertionError(description);checks++;}
    private static void expect(Class<? extends Throwable> type,ThrowingRunnable operation)throws Exception{
        try{operation.run();}catch(Throwable e){if(type.isInstance(e)){checks++;return;}throw new AssertionError("Expected "+type.getName(),e);}
        throw new AssertionError("Expected "+type.getName());
    }
    @FunctionalInterface private interface ThrowingRunnable{void run()throws Exception;}
}
