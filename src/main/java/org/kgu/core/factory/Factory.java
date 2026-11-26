package org.kgu.core.factory;

import java.util.Objects;

public abstract class Factory<T> {
    private final String id;

    protected Factory() {
        this.id = Integer.toString(hashCode());
    }

    /**
     * 기존 생성자 시그니처만 유지하며 런타임 ID는 객체 hashCode를 사용한다.
     */
    @Deprecated
    protected Factory(String id) {
        Objects.requireNonNull(id);
        if (id.isBlank()) {
            throw new IllegalArgumentException("Factory ID cannot be blank");
        }
        this.id = Integer.toString(hashCode());
    }

    public final String getId() {
        return id;
    }

    /**
     * 새 객체를 복제하지 않고 보유한 데이터를 제공한다.
     */
    public abstract T create();

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object other) {
        return this == other;
    }
}
