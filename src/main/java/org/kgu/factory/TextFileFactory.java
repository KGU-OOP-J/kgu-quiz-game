package org.kgu.factory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public abstract class TextFileFactory<T> extends FileFactory<T> {
    protected TextFileFactory(Path path) { super(path); }

    @Override
    public final void load() throws IOException {
        try (Scanner in = new Scanner(Files.newBufferedReader(getPath(), StandardCharsets.UTF_8))) {
            read(in);
            if (in.ioException() != null) {
                throw in.ioException();
            }
        }
    }

    protected abstract void read(Scanner in) throws IOException;
}
