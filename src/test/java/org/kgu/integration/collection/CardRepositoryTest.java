package org.kgu.integration.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CardRepositoryTest {
    private static Card card(String name, String field, int stars) {
        return new Card(name, "", field, stars == 0 ? List.of() : List.of(stars), List.of());
    }

    @Test
    void 전체는_별_많은_순_같으면_분야_순_그_안은_등록_순() {
        // 분야 순·등록 순으로 들어온 카드
        List<Card> cards = List.of(
                card("과1", "과학자", 0), card("과2", "과학자", 1),
                card("교1", "교수님", 3), card("교2", "교수님", 1),
                card("한1", "한국사", 3), card("연1", "아이돌", 0));

        List<String> names = CardRepository.byField(cards, CardRepository.ALL).stream().map(Card::name).toList();

        assertEquals(List.of("교1", "한1", "과2", "교2", "과1", "연1"), names);
    }

    @Test
    void 분야를_고르면_등록_순_그대로() {
        List<Card> cards = List.of(card("교1", "교수님", 0), card("과1", "과학자", 3), card("교2", "교수님", 3));

        assertEquals(List.of("교1", "교2"),
                CardRepository.byField(cards, "교수님").stream().map(Card::name).toList());
    }
}
