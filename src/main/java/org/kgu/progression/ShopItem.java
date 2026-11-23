package org.kgu.progression;

public enum ShopItem {
    INSIGHT("분석 렌즈", 40, "전투당 1개: 스킬 자원 없이 오답 보기 1개 제거"),
    SHIELD("보호 부적", 50, "전투당 1개: 다음 반격 피해 15 감소"),
    TONIC("회복 물약", 60, "전투당 1개: HP 25 회복 (최대 HP에서는 사용 불가)");
    private final String label, description;
    private final int price;
    ShopItem(String label, int price, String description) { this.label = label; this.price = price; this.description = description; }
    public String label() { return label; }
    public int price() { return price; }
    public String description() { return description; }
    @Override public String toString() { return label; }
}
