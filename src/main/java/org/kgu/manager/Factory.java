package org.kgu.manager;

import java.util.Objects;

public abstract class Factory<T> {
    private final String id;

    protected Factory(String id) {
        this.id = Objects.requireNonNull(id);

        if (id.isBlank()) {
            throw new IllegalArgumentException("Factory ID cannot be blank");
        }
    }

    public final String getId() {
        return id;
    }

    public abstract T create();
}
