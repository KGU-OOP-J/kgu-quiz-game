package org.kgu.core.manager;

import org.kgu.core.factory.PersonFactory;

public final class PersonManager extends FactoryManager<PersonFactory> {
    private static final PersonManager INSTANCE = new PersonManager();

    private PersonManager() {
    }

    public static PersonManager getInstance() {
        return INSTANCE;
    }
}
