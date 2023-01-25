package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.PhoneNumberUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@DebugLogs
public class BookstoreUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public BookstoreUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) {
        Optional<User> userOptionalByEmail = userRepository.findUserByEmail(s);
        if (userOptionalByEmail.isPresent()) {
            return new BookstoreUserDetails(userOptionalByEmail.get());
        }

        Optional<User> userOptionalByPhone = userRepository.findUserByPhone(s);
        if (userOptionalByPhone.isPresent()) {
            return new PhoneNumberUserDetails(userOptionalByPhone.get());
        } else {
            throw new UsernameNotFoundException("The user with the specified email address was not found.");
        }
    }
}
