package org.kgu.battle;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kgu.integration.battle.BattleRepository;

class BattleDataExpansionTest {
    @TempDir Path data;

    @Test void fixtureAdapterPreservesAllNewQuestions() throws Exception {
        StringBuilder text = new StringBuilder("교수A\t소개\t정보1\t정보2\t정보3\n");
        for (int i = 0; i < 8; i++) text.append("중\t질문").append(i).append("\tA\tB\tC\tD\t1\n");
        Files.writeString(data.resolve("교수님.txt"), text);
        var people = new BattleRepository(data).load("교수님", "중");
        assertEquals(1, people.size());
        assertEquals(8, people.get(0).questions().size());
        var engine = new BattleEngine(people.get(0).character(), people.get(0).questions(), 5);
        assertEquals(8, engine.questionCount());
    }
}
