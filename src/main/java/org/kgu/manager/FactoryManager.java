package org.kgu.manager;

import java.util.NoSuchElementException;

public final class FactoryManager<F extends Factory<?>> extends Manager<F> {

    @Override
    public void add(F factory) {
        if (factory == null) {
            throw new IllegalArgumentException(
                "Factory must not be null."
            );
        }

        String id = factory.getId();
        validateById(id);

        if (findById(id) != null) {
            throw new IllegalArgumentException(
                "Factory '" + id + "' already exists."
            );
        }

        super.add(factory);
    }

    public F get(String id) {
        validateById(id);

        F factory = findById(id);

        if (factory == null) {
            throw new NoSuchElementException(
                "Factory '" + id + "' not found."
            );
        }

        return factory;
    }

    private F findById(String id) {
        for (F factory : getAll()) {
            if (factory.getId().equals(id)) {
                return factory;
            }
        }

        return null;
    }

    private static void validateById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                "Factory ID must not be null or blank."
            );
        }
    }
}
