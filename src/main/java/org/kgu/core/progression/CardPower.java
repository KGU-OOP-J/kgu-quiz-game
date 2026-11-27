package org.kgu.core.progression;

import org.kgu.core.model.Person;
import java.util.Map;

/** Every owned person is an equipable support, with a reproducible, named affinity. */
public record CardPower(String title, Kind kind, int strength, String personKey) {
    public enum Kind {
        ANALYSIS("분석", "수집 카드의 분석 성향"),
        POWER("돌파", "수집 카드의 돌파 성향"),
        GUARD("수호", "수집 카드의 수호 성향"),
        FOCUS("집중", "전투 경험치 증가");
        private final String label, description;
        Kind(String label, String description) { this.label = label; this.description = description; }
        public String label() { return label; }
        public String description() { return description; }
    }
    private static final Map<String, Kind> SIGNATURES = Map.ofEntries(
        Map.entry("알베르트 아인슈타인", Kind.ANALYSIS), Map.entry("아이작 뉴턴", Kind.POWER),
        Map.entry("마리 퀴리", Kind.FOCUS), Map.entry("니콜라 테슬라", Kind.POWER),
        Map.entry("이순신", Kind.GUARD), Map.entry("세종대왕", Kind.ANALYSIS),
        Map.entry("세종", Kind.ANALYSIS), Map.entry("유관순", Kind.FOCUS),
        Map.entry("정약용", Kind.ANALYSIS), Map.entry("장영실", Kind.ANALYSIS));
    public static String key(Person person) { return person.getCategory() + "|" + person.getName(); }
    public static CardPower of(Person person, int stage, int mastery) {
        Kind kind = SIGNATURES.getOrDefault(person.getName(),
            Kind.values()[Math.floorMod(key(person).hashCode(), Kind.values().length)]);
        int strength = Math.max(1, Math.min(3, stage)) + Math.min(5, Math.max(0, mastery) / 3);
        return new CardPower(person.getName() + "의 " + kind.label(), kind, strength, key(person));
    }
    public static CardPower beginner() { return new CardPower("견습 탐험가 — 카드 획득 후 장착 가능", Kind.GUARD, 0, ""); }
    public String description() {
        if (strength == 0) return "카드를 장착하면 지원 능력을 사용할 수 있어요";
        return switch (kind) {
            case ANALYSIS -> "답안 제출 전 오답 " + (strength >= 3 ? 2 : 1) + "개 제거" + (strength >= 2 ? "·인물 힌트" : "") + " · 전투당 1회";
            case POWER -> "최고 3콤보 달성 시 코인 +" + Math.min(30, strength * 10) + "% · 자동";
            case GUARD -> strength >= 2 ? "실패 위기 시 새로운 보충 문제 1회" + (strength >= 3 ? "·힌트" : "") + " · 전투당 1회" : "답안 제출 전 인물 힌트 · 전투당 1회";
            case FOCUS -> "전투 XP +" + strength * 5 + "% · 자동";
        };
    }
}
