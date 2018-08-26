package com.github.iyboklee.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.iyboklee.cache.BookCache;
import com.github.iyboklee.model.Book;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("api/book")
@Api(description = "Book APIs")
public class BookRestController {

    @Autowired private BookCache bookCache;

    @GetMapping(path = "{isbn}")
    @ApiOperation(value = "책 정보를 조회한다.")
    public Book findOne(@PathVariable String isbn) {
        return bookCache.findByIsbn(isbn);
    }

    @PostMapping(path = "{isbn}")
    @ApiOperation(value = "책 정보를 추가한다.")
    public Book create(@PathVariable String isbn,
                       @RequestBody Book book) {
        return bookCache.put(isbn, book);
    }

    @PutMapping(path = "{isbn}")
    @ApiOperation(value = "책 정보를 수정한다.")
    public Book update(@PathVariable String isbn,
                       @RequestBody Book book) {
        return bookCache.put(isbn, book);
    }

    @DeleteMapping(path = "{isbn}")
    @ApiOperation(value = "책 정보를 삭제한다.")
    public boolean delete(@PathVariable String isbn) {
        bookCache.delete(isbn);
        return true;
    }

}
