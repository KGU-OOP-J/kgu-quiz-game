package org.kgu.progression;

import org.kgu.model.Person;
import java.util.Map;

/** Every owned person is an equipable support, with a reproducible, named affinity. */
public record CardPower(String title, Kind kind, int strength, String personKey) {
    public enum Kind {
        ANALYSIS("분석", "분석 스킬 비용 감소, 시작 자원 증가"),
        POWER("돌파", "정답 공격 피해 증가"),
        GUARD("수호", "최대 HP 증가, 받는 피해 감소"),
        FOCUS("집중", "콤보 자원과 전투 경험치 증가");
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
        return switch (kind) {
            case ANALYSIS -> "시작 자원 +" + strength + " · 분석 비용 " + (strength > 0 ? 1 : 2);
            case POWER -> "정답 공격 피해 +" + strength * 2;
            case GUARD -> "최대 HP +" + strength * 4 + " · 반격 피해 -" + strength;
            case FOCUS -> "연속 정답 추가 자원 +1 · 전투 XP +" + strength * 5 + "%";
        };
    }
}
