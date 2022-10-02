package com.example.MyBookShopApp.integration;

import com.example.MyBookShopApp.dto.ChangeBookStatusDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BooksControllerTest {

    private final MockMvc mockMvc;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
    }

    @AfterEach
    void tearDown() {
        request = null;
    }

    @Autowired
    public BooksControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void addBookToCartWithCookieStringTest() throws Exception {
        ChangeBookStatusDto changeBookStatusDto = new ChangeBookStatusDto("CART", 352);
        Cookie cookie = new Cookie("cartBookIds", "1");

        mockMvc.perform(post("/changeBookStatus")
                        .cookie(cookie)
                        .with(request -> {
                            request.addHeader("Referer", "http://localhost:8085/books/book-tzmy-6652");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changeBookStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("true"))
                .andExpect(cookie().value("cartBookIds", "1/352"));
    }

    @Test
    void addBookToCartWithoutCookieStringTest() throws Exception {
        ChangeBookStatusDto changeBookStatusDto = new ChangeBookStatusDto("CART", 352);

        mockMvc.perform(post("/changeBookStatus")
                        .with(request -> {
                            request.addHeader("Referer", "http://localhost:8085/books/book-tzmy-6652");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changeBookStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("true"))
                .andExpect(cookie().value("cartBookIds", "352"));
    }

    @Test
    void moveBookFromPostponedToCart() throws Exception {
        ChangeBookStatusDto changeBookStatusDto = new ChangeBookStatusDto("CART", 352);
        Cookie cookiePostponed = new Cookie("postponedBookIds", "200/352");
        Cookie cookieCart = new Cookie("cartBookIds", "13");

        mockMvc.perform(post("/changeBookStatus")
                        .cookie(cookiePostponed, cookieCart)
                        .with(request -> {
                            request.addHeader("Referer", "http://localhost:8085/postponed");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changeBookStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("true"))
                .andExpect(cookie().value("postponedBookIds", "200"))
                .andExpect(cookie().value("cartBookIds", "13/352"));
    }

    @Test
    void removeBookFromCart() throws Exception {
        ChangeBookStatusDto changeBookStatusDto = new ChangeBookStatusDto("UNLINK", 352);
        Cookie cookie = new Cookie("cartBookIds", "352/2");

        mockMvc.perform(post("/changeBookStatus")
                        .cookie(cookie)
                        .with(request -> {
                            request.addHeader("Referer", "http://localhost:8085/cart");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changeBookStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("true"))
                .andExpect(cookie().value("cartBookIds", "2"));
    }

    @Test
    void moveBookFromCartToPostponed() throws Exception {
        ChangeBookStatusDto changeBookStatusDto = new ChangeBookStatusDto("KEPT", 352);
        Cookie cookieCart = new Cookie("cartBookIds", "352/2");
        Cookie cookiePostponed = new Cookie("postponedBookIds", "200");

        mockMvc.perform(post("/changeBookStatus")
                        .cookie(cookieCart, cookiePostponed)
                        .with(request -> {
                            request.addHeader("Referer", "http://localhost:8085/cart");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changeBookStatusDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value("true"))
                .andExpect(cookie().value("cartBookIds", "2"))
                .andExpect(cookie().value("postponedBookIds", "200/352"));
    }
}