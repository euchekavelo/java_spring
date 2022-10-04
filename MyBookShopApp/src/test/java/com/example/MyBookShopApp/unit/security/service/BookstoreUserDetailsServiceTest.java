package com.example.MyBookShopApp.unit.security.service;

import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.service.BookstoreUserDetailsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BookstoreUserDetailsServiceTest {

    private final BookstoreUserDetailsService userDetailsService;

    @Autowired
    BookstoreUserDetailsServiceTest(BookstoreUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Test
    void successfullyGetUserTest() {
        BookstoreUserDetails bookstoreUserDetails =
                (BookstoreUserDetails) userDetailsService.loadUserByUsername("kirill@mail.ru");
        assertNotNull(bookstoreUserDetails.getUser());
        assertThat(bookstoreUserDetails.getUser().getName(), Matchers.equalTo("Kirill"));
        assertThat(bookstoreUserDetails.getUser().getEmail(), Matchers.equalTo("kirill@mail.ru"));
    }

    @Test
    void notSuccessfullyGetUserTest() {
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("vlad@gmail.com"));
    }
}