package org.kgu.integration.collection;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * 사용자 ID별로 얻은 카드를 저장한다. 서버가 없어서 ID마다 saves/{ID}.txt 파일 하나를 쓴다.
 * 한 줄이 한 단계의 획득이고 형식은 분야, 이름, 난이도(탭으로 구분)다. 단계마다 따로 얻는다(상만 깨면 3성만 있다).
 * 파일은 처음 카드를 얻을 때 만든다.
 */
public final class CardSave {
    // shortcut: 실행 폴더 기준 경로, 배포 구조(chore/new-architecture)에 합칠 때 그쪽 경로 규칙으로 교체.
    private static final Path SAVE_DIR = Path.of("saves");
    // 게임 설정 화면(GameSetupScreen)과 같은 난이도 이름을 쓴다.
    private static final List<String> DIFFICULTIES = List.of("하", "중", "상");

    private CardSave() { }

    /**
     * 사용자가 얻은 카드를 읽는다.
     *
     * @param playerId 시작 화면에서 입력한 ID
     * @return "분야\t이름" → 얻은 단계(1~3) 집합. 처음 온 사용자나 ID가 비어 있으면 빈 맵
     */
    public static Map<String, SortedSet<Integer>> load(String playerId) {
        return load(SAVE_DIR, playerId);
    }

    /**
     * 인물을 클리어해 그 난이도 단계의 카드를 얻었다고 기록한다. 이미 그 단계를 얻었으면 아무것도 하지 않는다.
     *
     * @param playerId 시작 화면에서 입력한 ID
     * @param field 분야(예: 교수님)
     * @param name 인물 이름
     * @param difficulty 하·중·상 (세션의 {@code getDifficulty()} 값)
     * @return 그 단계를 처음 얻었으면 true
     */
    public static boolean record(String playerId, String field, String name, String difficulty) {
        return record(SAVE_DIR, playerId, field, name, difficulty);
    }

    /** 난이도(하·중·상)를 별 개수(1~3)로 바꾼다. 난이도 이름이 아니면 0이다. */
    public static int starsOf(String difficulty) {
        return DIFFICULTIES.indexOf(difficulty) + 1;
    }

    static Map<String, SortedSet<Integer>> load(Path dir, String playerId) {
        Map<String, SortedSet<Integer>> stars = new HashMap<>();
        if (playerId == null || playerId.isBlank()) return stars;
        Path file = fileOf(dir, playerId);
        if (!Files.isRegularFile(file)) return stars;
        List<String> lines;
        try {
            lines = Files.readAllLines(file);
        } catch (IOException e) {
            System.err.println("저장 파일을 읽지 못했습니다: " + file + " (" + e + ")");
            return stars;
        }
        for (String line : lines) {
            if (line.isBlank()) continue;
            String[] cols = line.split("\t");
            if (cols.length != 3 || starsOf(cols[2]) == 0) {
                System.err.println("저장 파일 형식이 틀려 건너뜁니다: " + file + " (" + line + ")");
                continue;
            }
            stars.computeIfAbsent(cols[0] + "\t" + cols[1], k -> new TreeSet<>()).add(starsOf(cols[2]));
        }
        return stars;
    }

    static boolean record(Path dir, String playerId, String field, String name, String difficulty) {
        if (playerId == null || playerId.isBlank()) throw new IllegalArgumentException("ID가 비어 있어 카드를 저장할 수 없습니다.");
        int stars = starsOf(difficulty);
        if (stars == 0) throw new IllegalArgumentException("난이도 이름이 아닙니다: " + difficulty);
        // 탭이나 줄바꿈이 들어가면 저장 파일의 줄 형식이 깨진다.
        if (!isCell(field) || !isCell(name)) throw new IllegalArgumentException("분야나 이름이 비었거나 탭·줄바꿈이 있습니다: " + field + ", " + name);

        if (load(dir, playerId).getOrDefault(field + "\t" + name, new TreeSet<>()).contains(stars)) return false;
        // 이전 줄을 지우지 않고 덧붙이기만 해서, 쓰다가 끊겨도 원래 기록은 남는다.
        String line = field + "\t" + name + "\t" + DIFFICULTIES.get(stars - 1) + System.lineSeparator();
        try {
            Files.createDirectories(dir);
            Files.writeString(fileOf(dir, playerId), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("카드를 저장하지 못했습니다: " + fileOf(dir, playerId), e);
        }
        return true;
    }

    /** ID를 파일 이름으로 쓸 수 있게 바꾼다. 파일 이름에 못 쓰는 문자는 _로 바꾼다. */
    // shortcut: CON·NUL 같은 Windows 예약 이름은 거르지 않음, 그런 ID 때문에 저장이 실패하면 추가.
    static Path fileOf(Path dir, String playerId) {
        String safe = playerId.trim().replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        if (safe.matches("\\.*")) safe = "_" + safe; // "."이나 ".."가 상위 폴더를 가리키지 않게
        return dir.resolve(safe + ".txt");
    }

    private static boolean isCell(String text) {
        return text != null && !text.isBlank() && !text.contains("\t") && !text.contains("\n") && !text.contains("\r");
    }
}
