package com.example.MyBookShopApp.unit.security.service;

import com.example.MyBookShopApp.dto.ContactConfirmationPayload;
import com.example.MyBookShopApp.dto.ContactConfirmationResponse;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource("/application-test.properties")
class BookstoreUserAuthorizationTest {

    private final BookstoreUserAuthorization bookstoreUserAuthorization;
    private ContactConfirmationPayload payloadWithExistingData;
    private ContactConfirmationPayload payloadWithNonExistentData;
    private ContactConfirmationPayload payloadWithWrongPassword;

    @Autowired
    public BookstoreUserAuthorizationTest(BookstoreUserAuthorization bookstoreUserAuthorization) {
        this.bookstoreUserAuthorization = bookstoreUserAuthorization;
    }

    @BeforeEach
    void setUp() {
        payloadWithExistingData = new ContactConfirmationPayload();
        payloadWithExistingData.setContact("kirill@mail.ru");
        payloadWithExistingData.setCode("1111111");

        payloadWithNonExistentData = new ContactConfirmationPayload();
        payloadWithNonExistentData.setContact("kirillseq@mail.ru");
        payloadWithNonExistentData.setCode("12345671");

        payloadWithWrongPassword = new ContactConfirmationPayload();
        payloadWithWrongPassword.setContact("kirill@mail.ru");
        payloadWithWrongPassword.setCode("1222222");
    }

    @AfterEach
    void tearDown() {
        payloadWithExistingData = null;
        payloadWithNonExistentData = null;
        payloadWithWrongPassword = null;
    }

    @Test
    void successfulAuthenticateUserAndGetTokenTest() {
        ContactConfirmationResponse response = bookstoreUserAuthorization.jwtLogin(payloadWithExistingData);
        assertNotNull(response.getResult());
    }

    @Test
    void unsuccessfulAuthenticateUserWithNonExistentDataTest() {
        assertThrows(UsernameNotFoundException.class,
                () -> bookstoreUserAuthorization.jwtLogin(payloadWithNonExistentData));
    }

    @Test
    void unsuccessfulAuthenticateUserWithWrongPasswordTest() {
        assertThrows(BadCredentialsException.class,
                () -> bookstoreUserAuthorization.jwtLogin(payloadWithWrongPassword));
    }
}