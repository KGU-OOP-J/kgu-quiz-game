package org.kgu.domain.game.feature;

import org.kgu.core.model.Person;
import java.util.List;
import java.util.Map;

public final class BattlePhases {
    public record Stage(String title, String dialogue, int requiredCorrect, String background) { }
    private static final Map<String, String> BOSSES = Map.of("과학자", "앨런 튜링", "한국사", "김구", "아이돌", "영케이", "교수님", "이은정");
    private BattlePhases() { }
    public static String bossName(String category) {
        String name = BOSSES.get(category);
        if (name == null) throw new IllegalArgumentException("마을 보스가 지정되지 않았습니다: " + category);
        return name;
    }
    public static List<Stage> forBattle(Person person, boolean boss) {
        if (!boss) return List.of(new Stage("인물 도전", "", 3, "battle"));
        if (person.getCategory().equals("교수님")) return List.of(
            new Stage("기억의 봉인", "희미해진 기억 속에서 첫 번째 단서를 찾아 주세요.", 3, "final-phase-1"),
            new Stage("봉인 붕괴", "기억이 돌아오기 시작합니다. 봉인을 함께 깨뜨려요.", 3, "final-phase-2"),
            new Stage("최후의 시련", "마지막 시련입니다. 다섯 문제 중 네 문제를 풀어 주세요!", 4, "final-phase-3"));
        return List.of(new Stage("마을의 시련", "다섯 문제 중 세 문제를 풀면 다음 시련이 열립니다.", 3, "boss-phase-1"),
            new Stage("마지막 관문", "이제 더 깊은 지식을 보여 주세요. 네 문제를 맞혀야 합니다.", 4, "boss-phase-2"));
    }
}
