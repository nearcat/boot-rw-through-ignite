package com.github.iyboklee.repository;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.iyboklee.model.Book;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookRepositoryTest {

    @Autowired private BookRepository bookRepository;

    @Test
    public void STEP01_모든_책_조회가_가능하다() {
        List<Book> books = bookRepository.findAll();

        assertThat("책 조회 결과는 null이 아니다.", books, is(notNullValue()));
        assertThat("책 조회 결과는 6개이다.", books.size(), is(6));
    }

    @Test
    public void STEP02_ISBN으로_책_조회가_가능하다() {
        Book book = bookRepository.findByIsbn("ISBN1");

        assertThat("책 조회 결과는 null이 아니다.", book, is(notNullValue()));
        assertThat("조회 된 Book의 ISBN은 ISBN1이다", book.getIsbn(), is("ISBN1"));
    }

}