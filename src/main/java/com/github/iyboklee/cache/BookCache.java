package com.github.iyboklee.cache;

import org.apache.ignite.IgniteCache;

import com.github.iyboklee.model.Book;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookCache {

    public static final String CACHE_NAME = "book_cache";

    private IgniteCache<String, Book> cache;

    public Book findByIsbn(String isbn) {
        return cache.get(isbn);
    }

    public Book put(String isbn, Book book) {
        assert isbn.equals(book.getIsbn());

        cache.put(isbn, book);
        return book;
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
