package org.kgu.factory;

import org.kgu.manager.Factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Objects;

public abstract class FileFactory<T> extends Factory<T> {
    private final File file;

    protected FileFactory(String id, String path) {
        super(id);
        this.file = new File(Objects.requireNonNull(path, "path is null"));
    }

    @Override
    public final T create() {
        try (Scanner scan = new Scanner(file)) {
            return read(scan);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(
                "Failed to open file: " + file.getPath(), e
            );
        }
    }

    protected abstract T read(Scanner scan);
}
