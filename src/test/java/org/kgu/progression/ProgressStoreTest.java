package org.kgu.core.progression;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.core.model.Person;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ProgressStoreTest {
    @TempDir Path directory;
    private ProgressStore store(String name) { return new ProgressStore(directory.resolve(name + ".progress")); }
    @Test void tutorialAndClaimsPersistAndCannotBeClaimedTwice() throws Exception {
        ProgressStore s = store("player"); s.open();
        assertEquals(120, s.snapshot().coins());
        assertThrows(IllegalArgumentException.class, () -> s.claim(Achievement.FIRST_STEPS));
        s.completeTutorial(); s.claim(Achievement.FIRST_STEPS);
        assertEquals(200, s.snapshot().coins());
        assertThrows(IllegalArgumentException.class, () -> s.claim(Achievement.FIRST_STEPS));
        ProgressStore reloaded = store("player"); reloaded.open();
        assertTrue(reloaded.snapshot().flag("tutorial"));
        assertTrue(Achievement.FIRST_STEPS.claimed(reloaded.snapshot()));
        assertFalse(store("other").snapshot().flag("tutorial"));
    }
    @Test void purchasesConsumeFundsAndInventoryExactlyOnce() throws Exception {
        ProgressStore s = store("shop");
        s.buy(ShopItem.TONIC); s.buy(ShopItem.TONIC);
        assertEquals(0, s.snapshot().coins()); assertEquals(2, s.snapshot().inventory(ShopItem.TONIC));
        assertThrows(IllegalArgumentException.class, () -> s.buy(ShopItem.SHIELD));
        s.consume(ShopItem.TONIC); s.consume(ShopItem.TONIC);
        assertThrows(IllegalArgumentException.class, () -> s.consume(ShopItem.TONIC));
        assertEquals(0, s.snapshot().inventory(ShopItem.TONIC));
    }
    @Test void rewardsAreIdempotentAndTrackLevelsMasteryStarsAndBosses() throws Exception {
        ProgressStore s = store("reward");
        var r = new ProgressStore.Reward("session", 350, 100, true, true, "normal.과학자|뉴턴|하", 3, 4, true, List.of("과학자|뉴턴"), 10);
        s.settle(r); s.settle(r);
        assertEquals(350, s.snapshot().experience()); assertEquals(3, s.snapshot().level());
        assertEquals(100, s.snapshot().levelExperience()); assertEquals(220, s.snapshot().coins());
        assertEquals(1, s.snapshot().mastery("과학자|뉴턴")); assertEquals(3, s.snapshot().totalStars());
        assertEquals(1, s.snapshot().cleared("과학자"));
        assertTrue(Achievement.COLLECTOR.unlocked(s.snapshot()));
        for (int i = 0; i < 2; i++) s.settle(new ProgressStore.Reward("extra" + i, 0, 0, true, true,
            "normal.과학자|인물" + i + "|하", 1, 0, false, List.of(), 10));
        assertTrue(s.snapshot().bossUnlocked("과학자"));
        ProgressStore reloaded = store("reward"); reloaded.open(); reloaded.settle(r);
        assertEquals(350, reloaded.snapshot().experience());
    }
    @Test void failedWritesDoNotGrantPurchasesOrTutorialCompletion() throws Exception {
        Path blocked = directory.resolve("blocked"); Files.createDirectory(blocked);
        ProgressStore s = new ProgressStore(blocked);
        assertThrows(IOException.class, () -> s.buy(ShopItem.INSIGHT));
        assertEquals(120, s.snapshot().coins()); assertEquals(0, s.snapshot().inventory(ShopItem.INSIGHT));
        assertFalse(Files.exists(directory.resolve("blocked.tmp")));
    }
    @Test void wrongAnswersAndRecentQuestionsSurviveReloadAndRecover() throws Exception {
        ProgressStore s = store("answers");
        s.answer("q1", false); s.answer("q2", true);
        assertTrue(s.snapshot().wrongQuestions().contains("q1"));
        s.answer("q1", true);
        assertTrue(s.snapshot().wrongQuestions().isEmpty());
        for (int i = 0; i < 50; i++) s.answer("question" + i, false);
        ProgressStore r = store("answers"); r.open();
        assertEquals(40, r.snapshot().recentQuestions().size());
        assertEquals(2, r.snapshot().number("correct"));
    }
    @Test void corruptAndUnknownProfilesFailWithoutResetting() throws Exception {
        Path p = directory.resolve("bad"); Files.writeString(p, "version=1\ncoins=-10\n");
        assertThrows(IOException.class, () -> new ProgressStore(p).open());
        Files.writeString(p, "version=99\n");
        assertThrows(IOException.class, () -> new ProgressStore(p).open());
    }
    @Test void signatureCardsAndMasteryHaveDistinctCombatEffects() {
        Person newton = new Person("과학자", "아이작 뉴턴", List.of("1", "2", "3", "4"));
        Person admiral = new Person("한국사", "이순신", List.of("1", "2", "3", "4"));
        assertEquals(CardPower.Kind.POWER, CardPower.of(newton, 1, 0).kind());
        assertEquals(CardPower.Kind.GUARD, CardPower.of(admiral, 1, 0).kind());
        assertEquals(5, CardPower.of(newton, 3, 6).strength());
    }
}
