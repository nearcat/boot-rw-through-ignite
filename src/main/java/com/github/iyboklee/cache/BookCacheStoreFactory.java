package com.github.iyboklee.cache;

import javax.cache.configuration.Factory;
import javax.sql.DataSource;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookCacheStoreFactory implements Factory<BookCacheStore> {

    private transient DataSource dataSource;

    @Override
    public BookCacheStore create() {
        return new BookCacheStore(dataSource);
    }

}
