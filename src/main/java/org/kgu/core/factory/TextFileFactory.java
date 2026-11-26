package org.kgu.core.factory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public abstract class TextFileFactory<T> extends FileFactory<T> {
    private final String resource;

    protected TextFileFactory(Path path) {
        super(path);
        resource = null;
    }

    protected TextFileFactory(String resource) {
        super(Path.of(resource));
        this.resource = resource;
    }

    private Scanner scanner() throws IOException {
        if (resource == null) return new Scanner(Files.newBufferedReader(getPath(), StandardCharsets.UTF_8));
        var stream = TextFileFactory.class.getResourceAsStream(resource);
        if (stream == null) throw new IOException("Missing resource: " + resource);
        return new Scanner(stream, StandardCharsets.UTF_8);
    }

    @Override
    public final void load() throws IOException {
        try (Scanner in = scanner()) {
            read(in);
            if (in.ioException() != null) {
                throw in.ioException();
            }
        }
    }

    protected abstract void read(Scanner in) throws IOException;
}
