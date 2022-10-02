package com.example.MyBookShopApp.unit.service;

import com.example.MyBookShopApp.model.Book;
import com.example.MyBookShopApp.repository.BookRepository;
import com.example.MyBookShopApp.service.MainPageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;

import javax.servlet.http.Cookie;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class MainPageServiceTest {

    private final MainPageService mainPageService;
    private final BookRepository bookRepository;
    private MockHttpServletRequest request;
    private List<Book> testBookList;

    @Autowired
    public MainPageServiceTest(MainPageService mainPageService, BookRepository bookRepository) {
        this.mainPageService = mainPageService;
        this.bookRepository = bookRepository;
    }

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        testBookList = new ArrayList<>();
        testBookList.add(bookRepository.findBookById(521).orElse(null));
        testBookList.add(bookRepository.findBookById(837).orElse(null));
    }

    @AfterEach
    void tearDown() {
        request = null;
        testBookList = null;
    }

    @Test
    @WithUserDetails("max@mail.ru")
    void compareGettingAListOfRecommendedBooksByAnAuthorizedUserWithCookieStringsTest() {
        Cookie cookieCart = new Cookie("cartBookIds", "5");
        Cookie postponedCart = new Cookie("postponedBookIds", "1/2");
        request.setCookies(cookieCart, postponedCart);

        List<Book> bookList = mainPageService.getPageOfRecommendedBooks(request, 0, 2);
        assertEquals(bookList.toString(), testBookList.toString());
    }

    @Test
    @WithAnonymousUser
    void compareGettingAListOfRecommendedBooksByANonAuthorizedUserWithCookieStringsTest() {
        Cookie cookieCart = new Cookie("cartBookIds", "5");
        Cookie postponedCart = new Cookie("postponedBookIds", "1/2");
        request.setCookies(cookieCart, postponedCart);

        List<Book> bookList = mainPageService.getPageOfRecommendedBooks(request, 0, 2);
        assertNotEquals(bookList.toString(), testBookList.toString());
    }

    @Test
    @WithUserDetails("max@mail.ru")
    void compareGettingAListOfRecommendedBooksByAnAuthorizedUserWithoutCookieStringsTest() {
        List<Book> bookList = mainPageService.getPageOfRecommendedBooks(request, 0, 2);
        assertNotEquals(bookList.toString(), testBookList.toString());
    }

    @Test
    @WithAnonymousUser
    void compareGettingAListOfRecommendedBooksByANonAuthorizedUserWithoutCookieStringsTest() {
        List<Book> bookList = mainPageService.getPageOfRecommendedBooks(request, 0, 2);
        assertNotEquals(bookList.toString(), testBookList.toString());
    }
}