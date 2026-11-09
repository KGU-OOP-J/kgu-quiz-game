package org.kgu.application;

import org.kgu.factory.PersonFileFactory;
import org.kgu.manager.PersonFileManager;

public final class Application {
    private final PersonFileManager personFileManager;
    private boolean running = false;

    public Application() {
        personFileManager = new PersonFileManager();
    }

    public void run() {
        if (running) {
            throw new IllegalStateException(
                "Application is already running."
            );
        }

        initialize();
        running = true;

        // TODO: UI 실행
    }

    private void initialize() {
        personFileManager.add(new PersonFileFactory("professor", "data/교수님.txt"));
        personFileManager.add(new PersonFileFactory("celebrity", "data/연예인.txt"));
        personFileManager.add(new PersonFileFactory("scientist", "data/과학자.txt"));
        personFileManager.add(new PersonFileFactory("history", "data/한국사.txt"));
    }

    public void shutdown() {
        if (!running) {
            return;
        }

        personFileManager.clear();
        running = false;
    }
}
