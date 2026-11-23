package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.application.PlayerSession;
import org.kgu.factory.PersonFactory;
import org.kgu.factory.SettingFactory;
import org.kgu.manager.ImageManager;
import org.kgu.manager.PersonManager;
import org.kgu.model.Person;
import org.kgu.model.Quiz;
import org.kgu.window.ViewId;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class GameFeature {
    private final PlayerSession playerSession;
    private final PersonManager personManager;
    private final ImageManager imageManager;
    private final SettingFactory settingFactory;
    private final Consumer<ViewId> navigate;
    private final Random random;
    private Optional<GameSession> gameSession = Optional.empty();

    public GameFeature(PlayerSession playerSession, PersonManager personManager, ImageManager imageManager,
                       SettingFactory settingFactory, Consumer<ViewId> navigate) {
        this(playerSession, personManager, imageManager, settingFactory, navigate, new Random());
    }
    public GameFeature(PlayerSession playerSession, PersonManager personManager, ImageManager imageManager,
                       SettingFactory settingFactory, Consumer<ViewId> navigate, Random random) {
        this.playerSession = Objects.requireNonNull(playerSession);
        this.personManager = Objects.requireNonNull(personManager);
        this.imageManager = Objects.requireNonNull(imageManager);
        this.settingFactory = Objects.requireNonNull(settingFactory);
        this.navigate = Objects.requireNonNull(navigate);
        this.random = Objects.requireNonNull(random);
    }

    public List<Person> getCandidates(String category) {
        return factories(category).stream().map(PersonFactory::create).collect(Collectors.toUnmodifiableList());
    }

    public void startPractice(String category, Difficulty difficulty) {
        gameSession = Optional.of(GameSession.practice(category, difficulty, factories(category)));
        navigate.accept(ViewId.QUESTION);
    }

    public void startBattle(String category, Difficulty difficulty, Person selectedPerson) {
        List<PersonFactory> candidates = factories(category);
        PersonFactory player = candidates.stream().filter(p -> p.create() == selectedPerson).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("배틀에 사용할 인물을 선택하세요."));
        List<PersonFactory> opponents = candidates.stream().filter(p -> p != player).collect(Collectors.toList());
        if (opponents.isEmpty()) { throw new IllegalArgumentException("대진 가능한 인물이 2명 이상 필요합니다."); }
        PersonFactory computer = opponents.get(random.nextInt(opponents.size()));
        List<Quiz> attack = new ArrayList<>(player.getQuizFactory().get(difficulty));
        List<Quiz> defense = new ArrayList<>(computer.getQuizFactory().get(difficulty));
        Collections.shuffle(attack, random);
        Collections.shuffle(defense, random);
        BattleEngine battle = new BattleEngine(player.create(), computer.create(), attack, defense);
        gameSession = Optional.of(GameSession.battle(category, difficulty, battle));
        navigate.accept(ViewId.QUESTION);
    }

    public GameSession getSession() {
        return gameSession.orElseThrow(() -> new IllegalStateException("게임을 먼저 시작하세요."));
    }
    public void selectAnswer(int answer) { getSession().selectAnswer(answer); }
    public boolean submitAnswer() { return getSession().submitPractice(); }
    public void nextQuestion() {
        getSession().nextPractice();
        if (getSession().getPhase() == GameSession.Phase.FINISHED) { navigate.accept(ViewId.RESULT); }
    }
    public void selectBattleAnswer(int index) { getSession().getBattle().select(index); }
    public void submitBattleAnswer() { getSession().getBattle().submit(); }
    public void advanceBattle() { getSession().getBattle().advance(); }
    public void finishBattle() {
        if (getSession().getBattle().phase() != BattleEngine.Phase.FINISHED) {
            throw new IllegalStateException("6턴을 모두 진행한 뒤 결과를 확인하세요.");
        }
        navigate.accept(ViewId.RESULT);
    }

    /** 재시도해도 이미 저장한 단계는 중복되지 않는다. 오류를 성공으로 바꾸지 않는다. */
    public List<Person> saveRewards() throws IOException {
        List<Person> added = new ArrayList<>();
        for (Person person : getSession().getRewards()) {
            if (playerSession.saveFactory().record(person, getSession().getDifficulty())) { added.add(person); }
        }
        return List.copyOf(added);
    }
    public List<Person> getSavedRewards() {
        return getSession().getRewards().stream().filter(person -> playerSession.saveFactory().create()
                .contains(person.getCategory(), person.getName(), getSession().getDifficulty()))
                .collect(Collectors.toUnmodifiableList());
    }
    public boolean isSoundEnabled() { return settingFactory.create().isSoundEnabled(); }
    public Optional<BufferedImage> image(String key) { return imageManager.findImage(key); }
    public void returnToMenu() { navigate.accept(ViewId.MENU); }
    public void openCollection() { navigate.accept(ViewId.COLLECTION); }

    private List<PersonFactory> factories(String category) {
        return personManager.getAll().stream().filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toUnmodifiableList());
    }
}
