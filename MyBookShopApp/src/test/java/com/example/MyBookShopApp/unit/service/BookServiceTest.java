package com.example.MyBookShopApp.unit.service;

import com.example.MyBookShopApp.exception.NotFoundException;
import com.example.MyBookShopApp.exception.RecordExistException;
import com.example.MyBookShopApp.model.BookReview;
import com.example.MyBookShopApp.repository.BookReviewRepository;
import com.example.MyBookShopApp.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BookServiceTest {

    private final BookService bookService;
    private final BookReviewRepository bookReviewRepository;

    @Autowired
    public BookServiceTest(BookService bookService, BookReviewRepository bookReviewRepository) {
        this.bookService = bookService;
        this.bookReviewRepository = bookReviewRepository;
    }

    @Test
    @WithUserDetails("kirill@mail.ru")
    void checkInitialBookReviewRatingByAuthorizedUserTest() throws RecordExistException, NotFoundException {
        bookService.saveBookReview(15, "Test message.");
        BookReview bookReview = bookReviewRepository.getBookReviewById(1).orElse(null);
        assertThat(bookReview.getRating(), Matchers.equalTo(0));
    }

    @Test
    void setInitialReviewRatingByUnauthorizedUserTest() {
        assertThrows(NullPointerException.class, () -> bookService.saveBookReview(15, "Test message."));
    }

    @Test
    @WithUserDetails("kirill@mail.ru")
    void changeTheInitialBookReviewRatingByAnAuthorizedUserTest() throws NotFoundException {
        bookService.saveBookReviewLike((short) -1,1);
        BookReview bookReview = bookReviewRepository.getBookReviewById(1).orElse(null);
        assertThat(bookReview.getRating(), Matchers.equalTo(-1));
    }

    @Test
    @WithUserDetails("max@mail.ru")
    void changeTheBookReviewRatingByAnotherAnAuthorizedUserTest() throws NotFoundException {
        bookService.saveBookReviewLike((short) 1,10);
        BookReview bookReview = bookReviewRepository.getBookReviewById(10).orElse(null);
        assertThat(bookReview.getRating(), Matchers.equalTo(2));
    }

    @Test
    void changeTheBookReviewRatingByAnUnauthorizedUserTest() {
        assertThrows(NullPointerException.class, () -> bookService.saveBookReviewLike((short) 1,1));
    }
}