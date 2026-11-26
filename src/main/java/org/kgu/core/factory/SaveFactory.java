package org.kgu.core.factory;

import org.kgu.core.application.Difficulty;
import org.kgu.core.model.Person;
import org.kgu.core.model.Save;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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
    }

    @Override
    protected void read(Scanner in) throws IOException {
        try {
            Save loaded = Save.read(in);

            if (in.ioException() != null) {
                throw in.ioException();
            }

            save = loaded;
        } catch (IllegalArgumentException e) {
            throw new IOException("세이브 파일 형식 오류: " + getPath(), e);
        }
    }

    /**
     * 디스크에 쓰기가 성공한 뒤에만 메모리에 획득을 반영한다. 중복 보상은 쓰지 않는다.
     */
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

        createParentPath();
        String line = person.getCategory() + "\t" + person.getName() + "\t" + difficulty.getLabel();
        String separator = needsLineSeparator() ? System.lineSeparator() : "";

        Files.writeString(getPath(), separator + line + System.lineSeparator(), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        save.record(person.getCategory(), person.getName(), difficulty);

        return true;
    }

    private boolean needsLineSeparator() throws IOException {
        if (!Files.exists(getPath()) || Files.size(getPath()) == 0) {
            return false;
        }
        try (SeekableByteChannel channel = Files.newByteChannel(getPath(), StandardOpenOption.READ)) {
            ByteBuffer last = ByteBuffer.allocate(1);
            channel.position(channel.size() - 1);
            channel.read(last);
            byte value = last.array()[0];
            return value != '\n' && value != '\r';
        }
    }

    public void save() throws IOException {
        createParentPath();
        Files.write(getPath(), save.getLines(), StandardCharsets.UTF_8);
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
