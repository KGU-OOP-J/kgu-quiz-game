package org.kgu.integration.collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CardSaveTest {
    @TempDir
    Path dir;

    @Test
    void 처음_온_사용자는_카드가_없다() {
        assertEquals(Map.of(), CardSave.load(dir, "태호"));
        assertEquals(Map.of(), CardSave.load(dir, " "));
    }

    @Test
    void 얻은_카드와_단계를_ID별로_저장한다() {
        assertTrue(CardSave.record(dir, "태호", "교수님", "권기현", "중"));
        assertTrue(CardSave.record(dir, "태호", "아이돌", "뷔", "하"));
        assertTrue(CardSave.record(dir, "정현", "교수님", "권기현", "상"));

        assertEquals(Map.of("교수님\t권기현", Set.of(2), "아이돌\t뷔", Set.of(1)), CardSave.load(dir, "태호"));
        assertEquals(Map.of("교수님\t권기현", Set.of(3)), CardSave.load(dir, "정현"));
    }

    @Test
    void 단계마다_따로_얻고_같은_단계는_한_번만_저장한다() {
        CardSave.record(dir, "태호", "교수님", "권기현", "상");
        assertEquals(Set.of(3), CardSave.load(dir, "태호").get("교수님\t권기현")); // 상만 깨면 3성만

        assertFalse(CardSave.record(dir, "태호", "교수님", "권기현", "상"));
        assertTrue(CardSave.record(dir, "태호", "교수님", "권기현", "하"));
        assertEquals(List.of(1, 3), List.copyOf(CardSave.load(dir, "태호").get("교수님\t권기현")));
    }

    @Test
    void 잘못된_값은_저장하지_않는다() {
        assertThrows(IllegalArgumentException.class, () -> CardSave.record(dir, "", "교수님", "권기현", "하"));
        assertThrows(IllegalArgumentException.class, () -> CardSave.record(dir, "태호", "교수님", "권기현", "보통"));
        assertThrows(IllegalArgumentException.class, () -> CardSave.record(dir, "태호", "교수님", "권\t기현", "하"));
        assertEquals(Map.of(), CardSave.load(dir, "태호"));
    }

    @Test
    void 파일_이름에_못_쓰는_ID도_저장_폴더_안에_만든다() {
        assertEquals(dir.resolve("a_b.txt"), CardSave.fileOf(dir, "a/b"));
        assertEquals(dir.resolve("_...txt"), CardSave.fileOf(dir, ".."));

        CardSave.record(dir, "../밖", "교수님", "권기현", "하");
        assertTrue(Files.isRegularFile(dir.resolve(".._밖.txt")));
        assertEquals(Set.of(1), CardSave.load(dir, "../밖").get("교수님\t권기현"));
    }
}
