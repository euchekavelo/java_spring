package com.example.MyBookShopApp.unit.security.service;

import com.example.MyBookShopApp.dto.RegistrationForm;
import com.example.MyBookShopApp.exception.EmptyException;
import com.example.MyBookShopApp.exception.UserExistException;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.service.BookstoreUserRegister;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BookstoreUserRegisterTests {

    private final BookstoreUserRegister userRegister;
    private final PasswordEncoder passwordEncoder;
    private RegistrationForm correctRegistrationForm;
    private RegistrationForm wrongRegistrationForm;
    private RegistrationForm registrationFormWithExistValues;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    public BookstoreUserRegisterTests(BookstoreUserRegister userRegister, PasswordEncoder passwordEncoder) {
        this.userRegister = userRegister;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() {
        correctRegistrationForm = new RegistrationForm();
        correctRegistrationForm.setEmail("test@mail.org");
        correctRegistrationForm.setName("Tester1");
        correctRegistrationForm.setPass("test1");
        correctRegistrationForm.setPhone("9031232323");

        wrongRegistrationForm = new RegistrationForm();
        wrongRegistrationForm.setEmail("");
        wrongRegistrationForm.setName("Tester2");
        wrongRegistrationForm.setPass("test2");
        wrongRegistrationForm.setPhone("1111111111");

        User user = new User();
        user.setEmail("test3@mail.org");
        user.setName("Tester3");
        user.setPhone("2222222222");
        user.setPassword("122222");

        Mockito.when(userRepositoryMock.findUserByEmail("test3@mail.org")).thenReturn(Optional.of(user));

        registrationFormWithExistValues = new RegistrationForm();
        registrationFormWithExistValues.setEmail("test3@mail.org");
        registrationFormWithExistValues.setName("Tester3");
        registrationFormWithExistValues.setPass("test3");
        registrationFormWithExistValues.setPhone("3333333333");
    }

    @AfterEach
    void tearDown() {
        correctRegistrationForm = null;
        wrongRegistrationForm = null;
        registrationFormWithExistValues = null;
    }

    @Test
    void registerNewUserTest() throws EmptyException, UserExistException {
        User user = userRegister.registerNewUser(correctRegistrationForm);
        assertNotNull(user);
        assertTrue(passwordEncoder.matches(correctRegistrationForm.getPass(), user.getPassword()));
        assertTrue(CoreMatchers.is(user.getPhone()).matches(correctRegistrationForm.getPhone()));
        assertTrue(CoreMatchers.is(user.getName()).matches(correctRegistrationForm.getName()));
        assertTrue(CoreMatchers.is(user.getEmail()).matches(correctRegistrationForm.getEmail()));

        Mockito.verify(userRepositoryMock, Mockito.times(1))
                .save(Mockito.any(User.class));
    }

    @Test
    void registerNewUserWithEmptyFieldTest() {
        assertThrows(EmptyException.class, () -> userRegister.registerNewUser(wrongRegistrationForm));
        Mockito.verify(userRepositoryMock, Mockito.times(0))
                .save(Mockito.any(User.class));
    }

    @Test
    void registerExistingUserTest() {
        UserExistException exception = assertThrows(UserExistException.class,
                () -> userRegister.registerNewUser(registrationFormWithExistValues));
        assertThat(exception.getMessage(), Matchers.containsString("Registration error. " +
                "A user with this email or phone number already exists."));
        Mockito.verify(userRepositoryMock, Mockito.times(0))
                .save(Mockito.any(User.class));
    }
}