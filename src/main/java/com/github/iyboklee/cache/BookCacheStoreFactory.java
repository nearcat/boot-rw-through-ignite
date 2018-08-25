package com.github.iyboklee.cache;

import javax.cache.configuration.Factory;
import javax.sql.DataSource;

import com.github.iyboklee.config.BookCacheStore;

public class BookCacheStoreFactory implements Factory<BookCacheStore> {

    private transient DataSource dataSource;

    @Override
    public BookCacheStore create() {
        BookCacheStore store = new BookCacheStore();
        store.setDataSource(dataSource);
        return store;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
