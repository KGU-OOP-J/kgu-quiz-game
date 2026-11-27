package org.kgu.core.factory;

import org.kgu.core.model.Setting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

public final class SettingFactory extends TextFileFactory<Setting> {
    private final Setting setting;

    public SettingFactory(Setting setting) {
        this(Path.of("settings.properties"), setting);
    }

    public SettingFactory(Path path, Setting setting) {
        super(path);
        this.setting = Objects.requireNonNull(setting);
    }

    public void open() throws IOException {
        if (Files.exists(getPath())) {
            load();
        }
    }

    @Override
    protected void read(Scanner in) throws IOException {
        Properties properties = new Properties();

        while (in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int separator = line.indexOf('=');
            if (separator < 0) {
                throw new IOException("설정 파일 형식 오류: " + getPath());
            }
            properties.setProperty(line.substring(0, separator).trim(), line.substring(separator + 1).trim());
        }

        String value = properties.getProperty("soundEnabled", "true");

        if (!value.equals("true") && !value.equals("false")) {
            throw new IOException("soundEnabled는 true 또는 false여야 합니다.");
        }

        if (in.ioException() != null) {
            throw in.ioException();
        }

        setting.setSoundEnabled(Boolean.parseBoolean(value));
    }

    public void saveSoundEnabled(boolean enabled) throws IOException {
        Files.writeString(getPath(), "soundEnabled=" + enabled + System.lineSeparator(), StandardCharsets.UTF_8);
        setting.setSoundEnabled(enabled);
    }

    @Override
    public Setting create() {
        return setting;
    }

    @Override
    public void clear() {
        setting.setSoundEnabled(true);
    }
}
