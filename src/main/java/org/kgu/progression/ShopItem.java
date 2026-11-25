package org.kgu.progression;

/** Keep enum keys stable so existing inventories remain readable. */
public enum ShopItem {
    INSIGHT("분석 렌즈", 40, "현재 문제의 오답 보기 1개 제거 · 한 전투에 아이템 하나"),
    SHIELD("보호 부적", 50, "5문제 뒤 첫 오답 1개 재도전 · 정답 시 성공 조건만 +1 · 오답이 없으면 발동 없음"),
    TONIC("정보 물약", 60, "이번 전투 동안 인물 정보 열람 · 한 전투에 아이템 하나");

    private final String label;
    private final String description;
    private final int price;

    ShopItem(String label, int price, String description) {
        this.label = label;
        this.price = price;
        this.description = description;
    }

    public String label() { return label; }
    public int price() { return price; }
    public String description() { return description; }
    @Override public String toString() { return label; }
}
