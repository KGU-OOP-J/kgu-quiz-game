package org.kgu.integration.collection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/** 도감 카드를 제공한다. */
public final class CardRepository {
    /** 모든 분야를 함께 보는 항목. */
    public static final String ALL = "전체";
    /** 도감 왼쪽에 나오는 항목. */
    public static final List<String> FIELDS = List.of(ALL, "과학자", "교수님", "한국사", "아이돌");

    // shortcut: 실행 폴더 기준 경로, 데이터를 jar에 넣게 되면 클래스패스 리소스로 교체.
    private static final Path DATA_DIR = Path.of("data");

    /** 모든 인물 카드(아직 아무도 얻지 않은 상태). 인물 데이터는 실행 중에 바뀌지 않아 한 번만 읽는다. */
    private static final List<Card> PEOPLE = loadPeople();

    private CardRepository() { }

    /**
     * 사용자가 얻은 단계를 채운 모든 카드를 돌려준다.
     *
     * @param playerId 시작 화면에서 입력한 ID
     * @return 모든 분야의 카드. 얻지 않은 카드는 단계가 비어 있다
     */
    public static List<Card> cards(String playerId) {
        Map<String, SortedSet<Integer>> owned = CardSave.load(playerId);
        return PEOPLE.stream()
                .map(c -> new Card(c.name(), c.intro(), c.field(),
                        List.copyOf(owned.getOrDefault(c.field() + "\t" + c.name(), new TreeSet<>())), c.achievements()))
                .toList();
    }

    /**
     * 카드 중 한 분야만 고른다.
     *
     * @param cards {@link #cards(String)}가 돌려준 카드
     * @param field {@link #FIELDS}의 항목
     * @return 그 분야의 카드. {@link #ALL}이면 전부를 별 많은 순으로(같으면 분야 순, 그 안은 등록 순)
     */
    public static List<Card> byField(List<Card> cards, String field) {
        // cards는 이미 분야 순·등록 순이고 stream 정렬은 안정 정렬이라 별 개수만 비교하면 된다.
        if (field.equals(ALL)) return cards.stream().sorted(Comparator.comparingInt(Card::stars).reversed()).toList();
        return cards.stream().filter(card -> card.field().equals(field)).toList();
    }

    /** data/{분야}.txt를 읽는다. 파일이 없으면 그 분야는 비어 있다. */
    private static List<Card> loadPeople() {
        List<Card> cards = new ArrayList<>();
        for (String field : FIELDS.subList(1, FIELDS.size())) {
            cards.addAll(readCards(DATA_DIR.resolve(field + ".txt"), field));
        }
        return List.copyOf(cards);
    }

    /** 인물 줄만 카드로 만든다. 문제 줄은 도감에서 쓰지 않는다. 형식: 이름, 소개, 정보1, 정보2, 정보3 (탭으로 구분) */
    private static List<Card> readCards(Path file, String field) {
        List<Card> cards = new ArrayList<>();
        for (String line : readLines(file)) {
            String[] cols = line.split("\t");
            if (line.isBlank() || CardSave.starsOf(cols[0]) > 0) continue; // 하·중·상으로 시작하면 문제 줄
            if (cols.length < 2) {
                System.err.println("인물 줄 형식이 틀려 건너뜁니다: " + file + " (" + line + ")");
                continue;
            }
            List<String> achievements = List.of(cols).subList(2, cols.length);
            cards.add(new Card(cols[0], cols[1], field, List.of(), achievements));
        }
        return cards;
    }

    private static List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("파일을 읽지 못했습니다: " + file + " (" + e + ")");
            return List.of();
        }
    }
}
