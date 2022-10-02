package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.dto.ContactConfirmationPayload;
import com.example.MyBookShopApp.dto.ContactConfirmationResponse;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookstoreUserAuthorization {

    private final AuthenticationManager authenticationManager;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Autowired
    public BookstoreUserAuthorization(AuthenticationManager authenticationManager,
                                      BookstoreUserDetailsService bookstoreUserDetailsService,
                                      JWTUtil jwtUtil, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    public ContactConfirmationResponse jwtLogin(ContactConfirmationPayload payload) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(payload.getContact(),
                payload.getCode()));
        BookstoreUserDetails userDetails =
                (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(payload.getContact());
        String jwtToken = jwtUtil.generateToken(userDetails);
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult(jwtToken);
        return response;
    }

    public User getCurrentUser() {
        Object object = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (object instanceof DefaultOAuth2User){
            DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) object;
            String email = defaultOAuth2User.getAttribute("email");
            Optional<User> optionalUser = userRepository.findUserByEmail(email);
            if (!optionalUser.isPresent()) {
                User user = new User();
                user.setEmail(email);
                user.setName(defaultOAuth2User.getAttribute("name"));
                userRepository.save(user);
                return user;
            } else {
                return optionalUser.get();
            }
        } else {
            BookstoreUserDetails userDetails = (BookstoreUserDetails) object;
            return userDetails.getUser();
        }
    }
}
