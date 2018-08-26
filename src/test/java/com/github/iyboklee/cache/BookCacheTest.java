package com.github.iyboklee.cache;

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
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BookCacheTest {

    @Autowired private BookCache bookCache;

    @Test
    public void STEP01_캐시_엔트리_초기화를_성공했다() {
        assertThat("캐시 초기 엔트리는 6개이다.", bookCache.size(), is(6));
    }

    @Test
    public void STEP02_캐시에서_값_조회가_가능하다() {
        Book book = bookCache.findByIsbn("ISBN1");

        assertThat("조회 결과는 null이 아니다.", book, is(notNullValue()));
        assertThat("조회 된 Book의 ISBN은 ISBN1이다", book.getIsbn(), is("ISBN1"));
    }

    @Test
    public void STEP03_캐시에서만_값을_삭제하고_데이터베이스를_통해_다시_조회가_가능하다() {
        bookCache.cacheClear("ISBN1");

        assertThat("캐시 엔트리 삭제 후 캐시 갯수는 5개이다.", bookCache.size(), is(5));

        Book book = bookCache.findByIsbn("ISBN1");

        assertThat("캐시 엔트리 삭제 후 조회하면 캐시에 엔트리가 추가된다.", bookCache.size(), is(6));
        assertThat("캐시 엔트리 삭제 후 책 조회 결과는 null이 아니다.", book, is(notNullValue()));
        assertThat("캐시 엔트리 삭제 후 조회 된 Book의 ISBN은 ISBN1이다", book.getIsbn(), is("ISBN1"));
    }

    @Test
    public void STEP04_캐시에서_값을_삭제한다() {
        bookCache.delete("ISBN1");

        assertThat("삭제 후 캐시 갯수는 5개이다.", bookCache.size(), is(5));

        Book book = bookCache.findByIsbn("ISBN1");

        assertThat("삭제 후 조회하면 캐시에 엔트리가 추가되지 않는다.", bookCache.size(), is(5));
        assertThat("삭제 후 조회 결과는 null이다.", book, is(nullValue()));
    }

    @Test
    public void STEP05_캐시에_값을_추가한다() {
        String isbn = "ISBN7";

        bookCache.put(isbn, new Book(isbn, "Spring boot With Apache Ignite 7"));
        assertThat("추가 후 캐시 갯수는 6개이다.", bookCache.size(), is(6));

        Book book = bookCache.findByIsbn(isbn);

        assertThat("추가 후 책 조회 결과는 null이 아니다.", book, is(notNullValue()));
        assertThat("추가 후 조회 된 Book의 ISBN은 ISBN7이다", book.getIsbn(), is(isbn));
    }

}