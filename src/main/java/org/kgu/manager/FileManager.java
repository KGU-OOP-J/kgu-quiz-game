package org.kgu.manager;

import org.kgu.factory.FileFactory;

import java.util.NoSuchElementException;

public class FileManager<F extends FileFactory<?>> extends Manager<F> {

    @Override
    public void add(F factory) {
        if (factory == null) {
            throw new IllegalArgumentException("FileFactory must not be null.");
        }

        for (F item : getAll()) {
            if (item.getId().equals(factory.getId())) {
                throw new IllegalArgumentException(
                    "FileFactory '" + factory.getId() + "' already exists."
                );
            }
        }

        super.add(factory);
    }

    public F get(String id) {
        for (F factory : getAll()) {
            if (factory.getId().equals(id)) {
                return factory;
            }
        }

        throw new NoSuchElementException(
            "FileFactory '" + id + "' not found."
        );
    }
}
