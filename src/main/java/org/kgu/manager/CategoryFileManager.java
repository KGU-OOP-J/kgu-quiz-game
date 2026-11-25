package org.kgu.manager;

import org.kgu.factory.CategoryFileFactory;

public final class CategoryFileManager extends FactoryManager<CategoryFileFactory> {
    private static final CategoryFileManager INSTANCE = new CategoryFileManager();

    private CategoryFileManager() {
    }

    public static CategoryFileManager getInstance() {
        return INSTANCE;
    }
}
