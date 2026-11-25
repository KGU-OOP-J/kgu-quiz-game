package org.kgu.game.feature;

import org.kgu.application.Difficulty;
import org.kgu.application.PlayerSession;
import org.kgu.factory.PersonFactory;
import org.kgu.factory.SettingFactory;
import org.kgu.manager.ImageManager;
import org.kgu.manager.PersonManager;
import org.kgu.model.Person;
import org.kgu.progression.*;
import org.kgu.window.ViewId;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Consumer;

public final class GameFeature {
    private final PlayerSession playerSession;
    private final PersonManager personManager;
    private final ImageManager imageManager;
    private final SettingFactory settingFactory;
    private final Consumer<ViewId> navigate;
    private final Random random;
    private GameSession gameSession;
    private Person rescuePerson;
    public GameFeature(PlayerSession playerSession, PersonManager personManager, ImageManager imageManager,
                       SettingFactory settingFactory, Consumer<ViewId> navigate) {
        this(playerSession, personManager, imageManager, settingFactory, navigate, new Random());
    }
    public GameFeature(PlayerSession playerSession, PersonManager personManager, ImageManager imageManager,
                       SettingFactory settingFactory, Consumer<ViewId> navigate, Random random) {
        this.playerSession = Objects.requireNonNull(playerSession); this.personManager = Objects.requireNonNull(personManager);
        this.imageManager = Objects.requireNonNull(imageManager); this.settingFactory = Objects.requireNonNull(settingFactory);
        this.navigate = Objects.requireNonNull(navigate); this.random = Objects.requireNonNull(random);
    }
    public ProgressStore progress() { return playerSession.saveFactory().progress(); }
    public PlayerProgress profile() { return progress().snapshot(); }
    public String playerId() { return playerSession.playerId(); }
    public List<Person> getCandidates(String category) { return factories(category).stream().map(PersonFactory::create).toList(); }
    public List<Person> getChallengeCandidates(String category) {
        return getCandidates(category).stream().filter(p -> !category.equals("교수님") || p.getName().equals("이은정")).toList();
    }
    public boolean categoryUnlocked(String category) { return !category.equals("교수님") || profile().professorUnlocked(); }
    private void requireCategory(String category) {
        if (!categoryUnlocked(category)) throw new IllegalArgumentException("교수님 구출 잠김 — 다른 분야의 서로 다른 문제 "
            + profile().rescueProgress() + "/" + PlayerProgress.PROFESSOR_REQUIRED + "개 정답이 필요합니다.");
    }
    public List<Person> ownedCards() {
        return personManager.getAll().stream().map(PersonFactory::create)
            .filter(p -> playerSession.saveFactory().create().isCollected(p.getCategory(), p.getName())).toList();
    }
    public CardPower cardPower(Person person) {
        int stage = playerSession.saveFactory().create().getStages(person.getCategory(), person.getName()).stream().mapToInt(Integer::intValue).max().orElse(0);
        return CardPower.of(person, stage, profile().mastery(CardPower.key(person)));
    }
    public Optional<Person> equippedCard() { return ownedCards().stream().filter(p -> CardPower.key(p).equals(profile().equipped())).findFirst(); }
    public CardPower equippedPower() { return equippedCard().map(this::cardPower).orElseGet(CardPower::beginner); }
    public void equip(Person person) throws IOException {
        if (!ownedCards().contains(person)) throw new IllegalArgumentException("획득한 카드만 장착할 수 있습니다.");
        progress().equip(CardPower.key(person));
    }
    public void startPractice(String category, Difficulty difficulty) {
        requireCategory(category);
        List<QuestionBank.Entry> pool = bank(category).stream().filter(e -> e.difficulty() == difficulty).toList();
        List<QuestionBank.Entry> entries = QuestionBank.choose(pool, 10, profile(), random, true);
        if (entries.size() < 10) throw new IllegalArgumentException("연습 모드에는 인물 10명 이상이 필요합니다.");
        gameSession = GameSession.practice(category, difficulty, entries, false); navigate.accept(ViewId.QUESTION);
    }
    public void startReview() {
        Set<String> wrongIds = profile().wrongQuestions();
        List<QuestionBank.Entry> wrong = QuestionBank.all(personManager.getAll()).stream()
            .filter(e -> wrongIds.contains(e.id()) && categoryUnlocked(e.person().getCategory())).toList();
        List<QuestionBank.Entry> entries = QuestionBank.choose(wrong, 10, profile(), random, false);
        if (entries.isEmpty()) throw new IllegalArgumentException("저장된 오답이 없습니다. 먼저 연습이나 배틀에 도전하세요.");
        gameSession = GameSession.practice("오답 노트", entries.getFirst().difficulty(), entries, true); navigate.accept(ViewId.QUESTION);
    }
    /** The selected person is now the challenge target, not an unowned playable character. */
    public void startBattle(String category, Difficulty difficulty, Person target) { startChallenge(category, difficulty, target, false); }
    public void startChallenge(String category, Difficulty difficulty, Person target, boolean boss) {
        requireCategory(category);
        if (!getChallengeCandidates(category).contains(target)) throw new IllegalArgumentException("도전할 인물을 선택하세요. 최종 교수님 구출 대상은 이은정 교수님입니다.");
        if (category.equals("교수님")) boss = true;
        if (boss && !profile().bossUnlocked(category)) throw new IllegalArgumentException("이 마을의 서로 다른 인물 3명에게 승리하면 보스가 열립니다.");

        List<QuestionBank.Entry> pool = bank(category).stream().filter(e -> e.difficulty() == difficulty && e.person() == target).toList();
        List<QuestionBank.Entry> entries = QuestionBank.choose(pool, BattleEngine.QUESTION_COUNT, profile(), random, false);
        if (entries.size() != BattleEngine.QUESTION_COUNT) {
            throw new IllegalArgumentException("선택한 인물과 난이도에 서로 다른 문제 5개가 필요합니다.");
        }
        BattleEngine battle = new BattleEngine(target, entries.stream().map(QuestionBank.Entry::quiz).toList(),
            equippedPower(), boss, random);
        gameSession = GameSession.battle(category, difficulty, entries, battle); navigate.accept(ViewId.QUESTION);
    }
    public GameSession getSession() {
        if (gameSession == null) throw new IllegalStateException("게임을 먼저 시작하세요.");
        return gameSession;
    }
    public void selectAnswer(int answer) { getSession().selectAnswer(answer); }
    private void recordAnswer(boolean correct) {
        try { progress().answer(getSession().currentEntry().id(), getSession().currentEntry().person().getCategory(), correct); }
        catch (IOException e) { throw new UncheckedIOException("답안 기록을 저장하지 못했습니다. 다시 제출하세요.", e); }
    }
    public boolean submitAnswer() {
        GameSession s = getSession();
        if (s.getPhase() != GameSession.Phase.QUESTION || s.getSelectedAnswer() < 1) throw new IllegalStateException("정답을 먼저 선택하세요.");
        boolean correct = s.getSelectedAnswer() == s.getCurrentQuiz().getAnswer(); recordAnswer(correct);
        return s.submitPractice();
    }
    public void nextQuestion() {
        getSession().nextPractice();
        if (getSession().getPhase() == GameSession.Phase.FINISHED) navigate.accept(ViewId.RESULT);
    }
    public void selectBattleAnswer(int index) { getSession().getBattle().select(index); }
    public void submitBattleAnswer() {
        BattleEngine b = getSession().getBattle();
        if (b.phase() != BattleEngine.Phase.QUESTION || b.selected() < 0) throw new IllegalStateException("정답을 먼저 선택하세요.");
        // A retry has already exposed the answer: do not award XP, unlock credit or clear the wrong-answer notebook.
        if (!b.isRetry()) recordAnswer(b.selected() + 1 == b.current().getAnswer());
        b.submit();
    }
    public void advanceBattle() { getSession().getBattle().advance(profile().inventory(ShopItem.SHIELD) > 0); }
    public void useItem(ShopItem item) throws IOException {
        BattleEngine b = getSession().getBattle(); b.validateItem(item);
        progress().consume(item); b.useItem(item);
    }
    public void finishBattle() {
        if (getSession().getBattle().phase() != BattleEngine.Phase.FINISHED) throw new IllegalStateException("전투를 먼저 끝내세요.");
        try {
            if (hasRescueDialogue()) {
                saveRewards();
                if (beginRescueDialogue()) return;
            }
        } catch (IOException ignored) { }
        navigate.accept(ViewId.RESULT);
    }
    private boolean hasRescueDialogue() {
        return gameSession != null && gameSession.getMode() == GameSession.Mode.BATTLE
            && gameSession.getPhase() == GameSession.Phase.FINISHED
            && gameSession.getBattle().result() == BattleEngine.Result.WIN
            && !gameSession.dialoguePlayed() && gameSession.getBattle().computer().hasDialogue();
    }
    public boolean beginRescueDialogue() {
        if (!hasRescueDialogue()) return false;
        rescuePerson = gameSession.getBattle().computer(); gameSession.markDialoguePlayed();
        navigate.accept(ViewId.DIALOGUE); return true;
    }
    public Person rescuePerson() { return Objects.requireNonNull(rescuePerson, "구출 대사가 없습니다."); }
    public void finishDialogue() { rescuePerson = null; navigate.accept(ViewId.RESULT); }
    /** Card persistence and profile settlement are independently idempotent, so retry is safe. */
    public List<Person> saveRewards() throws IOException {
        GameSession s = getSession(); List<Person> added = new ArrayList<>();
        for (Person person : s.getRewards()) {
            if (playerSession.saveFactory().record(person, s.getDifficulty())) {
                s.recordNewCard(person);
                added.add(person);
            }
        }
        progress().syncCollected(playerSession.saveFactory().create().getCollectedCount());
        if (s.getPhase() == GameSession.Phase.FINISHED) {
            boolean battle = s.getMode() == GameSession.Mode.BATTLE;
            boolean win = battle && s.getBattle().result() == BattleEngine.Result.WIN;
            boolean boss = battle && s.getBattle().boss();
            int xp = s.getCorrectCount() * (battle ? 12 : s.getMode() == GameSession.Mode.REVIEW ? 6 : 8) + (win ? boss ? 120 : 60 : 0);
            if (battle && s.getBattle().power().kind() == CardPower.Kind.FOCUS) xp += xp * s.getBattle().power().strength() * 5 / 100;
            int coins = s.getCorrectCount() * (battle ? 6 : 4) + (win ? boss ? 100 : 40 : 0);
            String stage = battle ? (boss ? "boss." : "normal.") + s.getCategory() + "|" + s.getBattle().computer().getName() + "|" + s.getDifficulty().getLabel() : "";
            List<String> mastery = new ArrayList<>(s.getRewards().stream().map(CardPower::key).toList());
            if (win && !s.getBattle().power().personKey().isBlank()) mastery.add(s.getBattle().power().personKey());
            PlayerProgress before = profile();
            progress().settle(new ProgressStore.Reward(s.id(), xp, coins, battle, win, stage,
                battle ? s.getBattle().stars() : 0, battle ? s.getBattle().bestCombo() : 0,
                win && s.getCorrectCount() == s.getAnsweredCount() && s.hintsUsed() == 0,
                mastery, playerSession.saveFactory().create().getCollectedCount()));
            PlayerProgress after = profile();
            s.recordRewardSettlement(before, after);
            if (s.receipt().isBlank()) s.receipt("XP +" + xp + " · 코인 +" + coins + " · Lv." + after.level()
                + (after.level() > before.level() ? " 레벨 업!" : "") + (win ? " · " + "★".repeat(s.getBattle().stars()) : ""));
        }
        return List.copyOf(added);
    }
    public List<Person> getSavedRewards() {
        return getSession().getRewards().stream().filter(person -> playerSession.saveFactory().create()
            .contains(person.getCategory(), person.getName(), getSession().getDifficulty())).toList();
    }
    public String nextGoal() {
        if (!profile().professorUnlocked()) return "최종 구출 해금 · 다른 분야 문제 " + profile().rescueProgress() + "/" + PlayerProgress.PROFESSOR_REQUIRED + "개 정답";
        if (!profile().professorRescued()) return "최종 구출 해금 완료! 교수님 마을에서 이은정 교수님을 구출하세요.";
        for (String category : org.kgu.application.Application.CATEGORIES.stream().filter(c -> !c.equals("교수님")).toList()) {
            if (!profile().bossUnlocked(category)) return category + " 마을: 서로 다른 인물 " + profile().cleared(category) + "/3 승리 → 보스 해금";
            if (profile().keys("stage.boss." + category + "|").isEmpty()) return category + " 마을 보스에 도전하세요!";
        }
        return "모든 마을 보스 격파! 높은 난이도와 별 3개, 카드 숙련도에 도전하세요.";
    }
    public boolean isSoundEnabled() { return settingFactory.create().isSoundEnabled(); }
    public Optional<BufferedImage> image(String key) { return imageManager.findImage(key); }
    public void returnToMenu() { navigate.accept(ViewId.MENU); }
    public void openCollection() { navigate.accept(ViewId.COLLECTION); }
    public void openAdventure() { navigate.accept(ViewId.ADVENTURE); }
    private List<PersonFactory> factories(String category) { return personManager.getAll().stream().filter(p -> p.getCategory().equals(category)).toList(); }
    private List<QuestionBank.Entry> bank(String category) { return QuestionBank.all(factories(category)); }
}
