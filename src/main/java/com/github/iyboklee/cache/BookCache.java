package com.github.iyboklee.cache;

import javax.annotation.PostConstruct;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.iyboklee.model.Book;

@Component
public class BookCache {

    public static final String CACHE_NAME = "book_cache";

    @Autowired private Ignite ignite;

    private IgniteCache<String, Book> cache;

    @PostConstruct
    public void init() {
        cache = ignite.getOrCreateCache(CACHE_NAME);
        cache.loadCache(null, 6);
    }

    public Book findByIsbn(String isbn) {
        return cache.get(isbn);
    }

    public void put(String isbn, Book book) {
        assert isbn.equals(book.getIsbn());

        cache.put(isbn, book);
    }

    public void cacheClear(String isbn) {
        cache.clear(isbn);
    }

    public void delete(String isbn) {
        cache.remove(isbn);
    }

    public int size() {
        return cache.size();
    }

}
