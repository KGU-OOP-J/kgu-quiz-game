package org.kgu.core.factory;

import org.kgu.core.application.Difficulty;
import org.kgu.core.model.Person;
import org.kgu.core.model.Save;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;

/**
 * 사용자 하나의 파일만 보유한다. 신규 사용자는 첫 보상 획득 시 파일을 생성한다.
 */
public final class SaveFactory extends TextFileFactory<Save> {
    private Save save = new Save();
    private final org.kgu.core.progression.ProgressStore progress;

    public org.kgu.core.progression.ProgressStore progress() { return progress; }

    public SaveFactory(Path path) {
        super(path);
        progress = new org.kgu.core.progression.ProgressStore(path.resolveSibling(path.getFileName() + ".progress"));
    }

    public static SaveFactory forPlayer(String playerId) {
        String id = Objects.requireNonNull(playerId).trim();

        if (id.isEmpty()) {
            throw new IllegalArgumentException("개인 ID를 입력해 주세요.");
        }

        // sanitize
        String safe = id.replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_");
        if (safe.matches("\\.*")) {
            safe = "_" + safe;
        }

        return new SaveFactory(Path.of("saves", safe + ".txt"));
    }

    public void open() throws IOException {
        progress.open();
        if (Files.exists(getPath())) {
            load();
        }
        restoreOwnedCards(save);
    }

    private void restoreOwnedCards(Save target) throws IOException {
        var profile = progress.snapshot();
        for (String key : profile.keys("card.owned.")) {
            if (!profile.flag(key)) continue;
            String[] fields = key.substring("card.owned.".length()).split("\\|", -1);
            if (fields.length != 3) throw new IOException("카드 진행도 형식 오류: " + key);
            int stage;
            try { stage = Integer.parseInt(fields[2]); }
            catch (NumberFormatException e) { throw new IOException("카드 단계 형식 오류: " + key, e); }
            if (stage < 1 || stage > 3) throw new IOException("카드 단계 범위 오류: " + key);
            try {
                for (Difficulty difficulty : Difficulty.values()) if (difficulty.getStage() == stage) target.record(fields[0], fields[1], difficulty);
            } catch (IllegalArgumentException e) { throw new IOException("카드 진행도 형식 오류: " + key, e); }
        }
    }

    @Override
    protected void read(Scanner in) throws IOException {
        try {
            Save loaded = Save.read(in);

            if (in.ioException() != null) {
                throw in.ioException();
            }

            restoreOwnedCards(loaded);
            save = loaded;
        } catch (IllegalArgumentException e) {
            throw new IOException("세이브 파일 형식 오류: " + getPath(), e);
        }
    }

    public boolean record(Person person, Difficulty difficulty) throws IOException {
        Objects.requireNonNull(person);
        Objects.requireNonNull(difficulty);

        if (person.getCategory().isBlank() || person.getCategory().matches("(?s).*[\\t\\r\\n].*")
            || person.getName().matches("(?s).*[\\t\\r\\n].*")) {
            throw new IllegalArgumentException("카테고리와 인물명은 비어 있거나 탭·줄바꿈을 포함할 수 없습니다.");
        }

        if (save.contains(person.getCategory(), person.getName(), difficulty)) {
            return false;
        }

        progress.acquireCard(person.getCategory(), person.getName(), difficulty.getStage());
        save.record(person.getCategory(), person.getName(), difficulty);
        try { save(); }
        catch (IOException e) {
            java.util.logging.Logger.getLogger(SaveFactory.class.getName()).warning("Legacy card export failed; ownership and NEW are saved in " + progress.path() + ": " + e.getMessage());
        }

        return true;
    }

    public void save() throws IOException {
        createParentPath();
        Path temporary = getPath().resolveSibling(getPath().getFileName() + ".tmp");
        try {
            Files.write(temporary, save.getLines(), StandardCharsets.UTF_8);
            try { Files.move(temporary, getPath(), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
            catch (java.nio.file.AtomicMoveNotSupportedException e) { Files.move(temporary, getPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }

    private void createParentPath() throws IOException {
        Path parent = getPath().toAbsolutePath().getParent();
        Files.createDirectories(parent);
    }

    @Override
    public Save create() {
        return save;
    }

    @Override
    public void clear() {
        save = new Save();
    }
}
