package org.kgu.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Manager<T> {
    private final String id = Integer.toString(hashCode());
    private final List<T> items = new ArrayList<>();

    public final String getId() {
        return id;
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }

    protected void add(T item) {
        items.add(Objects.requireNonNull(item));
    }

    public List<T> getAll() {
        return List.copyOf(items);
    }

    public int size() {
        return items.size();
    }

    public boolean remove(T item) {
        return items.remove(item);
    }

    public void clear() {
        items.clear();
    }
}
