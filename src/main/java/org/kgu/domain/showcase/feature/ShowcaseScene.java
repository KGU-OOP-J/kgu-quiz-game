package org.kgu.domain.showcase.feature;

public enum ShowcaseScene {
    MENU("메인 메뉴"), TUTORIAL("튜토리얼"),
    MAP("마을 지도", "world"), SHOP("상점", "shop"),
    ACHIEVEMENTS("도전과제", "achievements"), DECK("덱 구성", "deck"), REVIEW("오답 노트", "review"),
    COLLECTION("카드 도감"), SETTINGS("설정"), PRACTICE("연습 문제"),
    BATTLE("전투 시작"), HIT("첫 정답 · 공격 연출"), MISS("첫 오답 · 피드백"),
    VICTORY("자동 승리 · 전투 마무리"), DEFEAT("자동 패배 · 교수님 최종 공격"),
    RESCUE("교수님 구출 · 대화"), RESULT("승리 결과 · 보상 바로 보기");

    private final String label;
    private final String tab;

    ShowcaseScene(String label) { this(label, null); }

    ShowcaseScene(String label, String tab) {
        this.label = label;
        this.tab = tab;
    }

    public String tab() { return tab; }
    @Override public String toString() { return label; }
}
