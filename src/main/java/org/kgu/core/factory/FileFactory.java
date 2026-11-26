package org.kgu.core.factory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public abstract class FileFactory<T> extends Factory<T> {
    private final Path path;

    protected FileFactory(Path path) {
        this.path = Objects.requireNonNull(path);
    }

    public final Path getPath() {
        return path;
    }

    public abstract void load() throws IOException;

    public abstract void clear();

    public final void reload() throws IOException {
        clear();
        load();
    }
}
