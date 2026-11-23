package org.kgu.factory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;

/**
 * 파일 하나당 인스턴스 하나. 최초 표시 요청 시에만 이미지 바이트를 읽는다.
 */
public final class ImageFileFactory extends BinaryFileFactory<BufferedImage> {
    private Optional<BufferedImage> image = Optional.empty();

    public ImageFileFactory(Path path) {
        super(path);
    }

    public ImageFileFactory(String classpathResource) {
        super(classpathResource);
    }

    @Override
    protected void read(InputStream in) throws IOException {
        try (MemoryCacheImageInputStream imageInput = new MemoryCacheImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IOException("지원하지 않는 이미지: " + getPath());
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput);
                image = Optional.of(reader.read(0));
            } finally {
                reader.dispose();
            }
        }
    }

    @Override
    public BufferedImage create() {
        if (image.isEmpty()) {
            try {
                load();
            } catch (IOException e) {
                throw new UncheckedIOException("이미지 읽기 실패: " + getPath(), e);
            }
        }
        return image.orElseThrow();
    }

    @Override
    public void clear() {
        image = Optional.empty();
    }
}
