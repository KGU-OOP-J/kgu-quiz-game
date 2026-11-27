package org.kgu.core.manager;

import org.kgu.core.factory.ImageFileFactory;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 240개 인물 이미지와 공통 아이콘의 Lazy Factory를 관리한다.
 */
public final class ImageManager extends FactoryManager<ImageFileFactory> {
    private static final ImageManager INSTANCE = new ImageManager();
    private final Map<String, ImageFileFactory> images = new LinkedHashMap<>();

    private ImageManager() {
    }

    public static ImageManager getInstance() {
        return INSTANCE;
    }

    public void register(String key, Path path) {
        register(key, new ImageFileFactory(path));
    }

    public void registerResource(String key, String classpathResource) {
        register(key, new ImageFileFactory(classpathResource));
    }

    private void register(String key, ImageFileFactory factory) {
        if (images.containsKey(key)) {
            throw new IllegalArgumentException("이미 등록한 이미지: " + key);
        }
        super.add(factory);
        images.put(key, factory);
    }

    /**
     * 등록하지 않은 선택적 장식은 View가 도형으로 그릴 수 있다. 읽기 실패는 숨기지 않는다.
     */
    public Optional<BufferedImage> findImage(String key) {
        return Optional.ofNullable(images.get(key)).map(ImageFileFactory::create);
    }

    @Override
    public boolean remove(ImageFileFactory factory) {
        if (!super.remove(factory)) {
            return false;
        }
        images.values().removeIf(imageFactory -> imageFactory == factory);
        return true;
    }

    @Override
    public void clear() {
        super.clear();
        images.clear();
    }
}
