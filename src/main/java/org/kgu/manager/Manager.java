package org.kgu.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Manager<T> {
    private final List<T> items = new ArrayList<>();

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
