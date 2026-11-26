package org.kgu.core.progression;

import java.util.function.Predicate;

public enum Achievement {
    FIRST_STEPS("첫걸음", "튜토리얼 완료", 80, p -> p.flag("tutorial")),
    FIRST_WIN("첫 승리", "배틀 1회 승리", 100, p -> p.number("wins") >= 1),
    VETERAN("지식의 전사", "배틀 10회 승리", 180, p -> p.number("wins") >= 10),
    SCHOLAR("꾸준한 탐구", "문제 30개 정답", 120, p -> p.number("correct") >= 30),
    COMBO("연속 집중", "배틀 3연속 정답", 100, p -> p.number("bestCombo") >= 3),
    LEVEL_FIVE("성장하는 탐험가", "레벨 5 달성", 150, p -> p.level() >= 5),
    COLLECTOR("작은 박물관", "서로 다른 인물 카드 10명 수집", 150, p -> p.number("collected") >= 10),
    BOSS("마을의 영웅", "마을 보스 1회 격파", 200, p -> p.bossWins() >= 1),
    CONSTELLATION("별을 모으는 자", "도전 별 15개 획득", 150, p -> p.totalStars() >= 15),
    PERFECT("완벽한 도전", "힌트 없이 전부 정답으로 승리", 200, p -> p.number("perfectWins") >= 1);
    private final String title, description;
    private final int reward;
    private final Predicate<PlayerProgress> condition;
    Achievement(String title, String description, int reward, Predicate<PlayerProgress> condition) {
        this.title = title; this.description = description; this.reward = reward; this.condition = condition;
    }
    public String title() { return title; }
    public String description() { return description; }
    public int reward() { return reward; }
    public boolean unlocked(PlayerProgress progress) { return condition.test(progress); }
    public boolean claimed(PlayerProgress progress) { return progress.flag("achievement." + name()); }
}
