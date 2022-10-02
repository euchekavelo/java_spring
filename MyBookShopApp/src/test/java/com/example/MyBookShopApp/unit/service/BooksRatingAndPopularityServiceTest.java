package com.example.MyBookShopApp.unit.service;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.repository.BookRepository;
import com.example.MyBookShopApp.service.BooksRatingAndPopularityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BooksRatingAndPopularityServiceTest {

    private final BookRepository bookRepository;
    private final BooksRatingAndPopularityService booksRatingAndPopularityService;

    @Autowired
    public BooksRatingAndPopularityServiceTest(BookRepository bookRepository,
                                               BooksRatingAndPopularityService booksRatingAndPopularityService) {
        this.bookRepository = bookRepository;
        this.booksRatingAndPopularityService = booksRatingAndPopularityService;
    }

    @Test
    void calculatePopularityRatingTest() {
        double calculatedBookRating = booksRatingAndPopularityService.calculatePopularityRating(10, 10, 10);
        assertEquals(calculatedBookRating, 21);
    }

    @Test
    void successfullyCompareListOfBooksSortedByRatingTest() {
        Book firstTestBook = bookRepository.findBookById(352).orElse(null);
        Book secondTestBook = bookRepository.findBookById(520).orElse(null);
        List<Book> testBookList = new ArrayList<>();
        testBookList.add(firstTestBook);
        testBookList.add(secondTestBook);

        List<Book> sortedBookList = booksRatingAndPopularityService.getSortedBooksByPopularRating(0,2);
        assertEquals(sortedBookList.toString(), testBookList.toString());
    }

    @Test
    void unsuccessfullyCompareListOfBooksSortedByRatingTest() {
        Book firstTestBook = bookRepository.findBookById(1).orElse(null);
        Book secondTestBook = bookRepository.findBookById(400).orElse(null);
        List<Book> testBookList = new ArrayList<>();
        testBookList.add(firstTestBook);
        testBookList.add(secondTestBook);

        List<Book> sortedBookList = booksRatingAndPopularityService.getSortedBooksByPopularRating(0,2);
        assertNotEquals(sortedBookList.toString(), testBookList.toString());
    }
}