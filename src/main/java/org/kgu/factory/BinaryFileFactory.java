package org.kgu.factory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public abstract class BinaryFileFactory<T> extends FileFactory<T> {
    private final Optional<String> classpathResource;

    protected BinaryFileFactory(Path path) {
        super(path);
        classpathResource = Optional.empty();
    }

    /** 배경처럼 src/main/resources에 포함되는 바이너리는 클래스패스에서 읽는다. */
    protected BinaryFileFactory(String resource) {
        super(Path.of(resource));
        if (resource.isBlank()) { throw new IllegalArgumentException("리소스 경로가 비어 있습니다."); }
        classpathResource = Optional.of(resource.startsWith("/") ? resource : "/" + resource);
    }

    @Override
    public final void load() throws IOException {
        try (InputStream in = openStream()) {
            read(in);
        }
    }

    private InputStream openStream() throws IOException {
        if (classpathResource.isEmpty()) { return Files.newInputStream(getPath()); }
        InputStream stream = BinaryFileFactory.class.getResourceAsStream(classpathResource.get());
        if (stream == null) { throw new IOException("클래스패스 리소스가 없습니다: " + classpathResource.get()); }
        return stream;
    }

    protected abstract void read(InputStream in) throws IOException;
}
