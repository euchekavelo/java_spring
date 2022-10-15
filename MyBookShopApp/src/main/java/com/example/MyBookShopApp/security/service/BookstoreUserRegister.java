package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.dto.RegistrationForm;
import com.example.MyBookShopApp.exception.EmptyException;
import com.example.MyBookShopApp.exception.UserExistException;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;

@Service
@DebugLogs
public class BookstoreUserRegister {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public BookstoreUserRegister(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(RegistrationForm registrationForm) throws UserExistException, EmptyException {
        String name = registrationForm.getName();
        String email = registrationForm.getEmail();
        String phone = registrationForm.getPhone();
        String pass = registrationForm.getPass();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            throw new EmptyException("Registration error. When registering, the values of the transmitted fields " +
                    "cannot be empty.");
        }

        if (!userRepository.findUserByEmail(email).isPresent() && !userRepository.findUserByPhone(phone).isPresent()) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(registrationForm.getPass()));
            user.setPhone(phone);
            user.setRegTime(new Date(System.currentTimeMillis()));
            userRepository.save(user);
            return user;
        } else {
            throw new UserExistException("Registration error. A user with this email or phone number already exists.");
        }
    }
}
