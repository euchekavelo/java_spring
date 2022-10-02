package com.example.MyBookShopApp.integration;

import com.example.MyBookShopApp.dto.ContactConfirmationPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
class AuthUserControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    public AuthUserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void successfulRegistrationNewUserTest() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Jenya");
        params.add("email", "jenya@mail.ru");
        params.add("phone", "+7(926)1111111");
        params.add("pass", "2222222");

        mockMvc.perform(post("/reg").params(params))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void unsuccessfullyRegisterARegisteredUserTest() throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "Kirill");
        params.add("email", "kirill@mail.ru");
        params.add("phone", "+7(111)1111111");
        params.add("pass", "1111111");

        mockMvc.perform(post("/reg").params(params))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signup"));
    }

    @Test
    void successHandleLoginByEmailTest() throws Exception {
        ContactConfirmationPayload payload = new ContactConfirmationPayload();
        payload.setContact("kirill@mail.ru");
        payload.setCode("1111111");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(payload)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void unSuccessHandleLoginByEmailTest() throws Exception {
        ContactConfirmationPayload payload = new ContactConfirmationPayload();
        payload.setContact("elena@mail.ru");
        payload.setCode("4444444");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(payload)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("result").value(false))
                .andExpect(jsonPath("error").value("The user with the specified email " +
                        "address was not found."));
    }

    @Test
    void logoutTest() throws Exception {
        mockMvc.perform(get("/logout"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/signin"));
    }
}