package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.BookstoreUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookstoreUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public BookstoreUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findUserByEmail(s);
        if (userOptional.isPresent()) {
            return new BookstoreUserDetails(userOptional.get());
        } else {
            throw new UsernameNotFoundException("The user with the specified email address was not found.");
        }
    }
}
