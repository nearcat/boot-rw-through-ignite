package com.github.iyboklee.repository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.github.iyboklee.model.Book;

@Repository
public class BookRepository {

    @Autowired private JdbcTemplate jdbcTemplate;

    static RowMapper<Book> bookMapper = (rs, rowNum) -> new Book(rs.getString("isbn"), rs.getString("title"));

    public Book findByIsbn(String isbn) {
        return jdbcTemplate.queryForObject("select * from books where isbn = ?", new Object[] {isbn}, bookMapper);
    }

    public List<Book> findAll() {
        return jdbcTemplate.query("select * from books", bookMapper);
    }

}
