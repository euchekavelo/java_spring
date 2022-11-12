package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.dto.UserDataChangeDto;
import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.dto.ContactConfirmationPayload;
import com.example.MyBookShopApp.dto.ContactConfirmationResponse;
import com.example.MyBookShopApp.model.BalanceTransaction;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.BalanceTransactionRepository;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@DebugLogs
public class BookstoreUserAuthorization {

    private final AuthenticationManager authenticationManager;
    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final BalanceTransactionRepository balanceTransactionRepository;

    @Autowired
    public BookstoreUserAuthorization(AuthenticationManager authenticationManager,
                                      BookstoreUserDetailsService bookstoreUserDetailsService,
                                      JWTUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder,
                                      BalanceTransactionRepository balanceTransactionRepository) {

        this.authenticationManager = authenticationManager;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.balanceTransactionRepository = balanceTransactionRepository;
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

    public void changeUserData(UserDataChangeDto userDataChangeDto) {
        String newPassword = userDataChangeDto.getPassword();
        String newPasswordReply = userDataChangeDto.getPasswordReply();
        if (!newPassword.isEmpty() && !newPasswordReply.isEmpty() && newPassword.equals(newPasswordReply)) {
            User user = getCurrentUser();
            Optional<User> optionalUserDatabase = userRepository.findUserByEmail(user.getEmail());
            if (optionalUserDatabase.isPresent()) {
                User userDatabase = optionalUserDatabase.get();
                userDatabase.setName(userDataChangeDto.getName());
                userDatabase.setEmail(userDataChangeDto.getMail());
                userDatabase.setPhone(userDatabase.getPhone());
                userDatabase.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(userDatabase);

                BookstoreUserDetails userDetails =
                        (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(userDataChangeDto.getMail());
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
    }

    @Transactional
    public void creditMoneyToTheUsersBalance(Double sum) {
        User currentUser = getCurrentUser();
        currentUser.setBalance(currentUser.getBalance() + sum);
        userRepository.save(currentUser);

        BalanceTransaction balanceTransaction = new BalanceTransaction();
        balanceTransaction.setUser(currentUser);
        balanceTransaction.setValue(sum);
        balanceTransaction.setDescription("Зачисление на счет");
        balanceTransaction.setTime(LocalDateTime.now());
        balanceTransactionRepository.save(balanceTransaction);
    }

    public List<BalanceTransaction> getPageOfBalanceTransaction(Integer offset, Integer limit) {
        User currentUser = getCurrentUser();
        Pageable nextPage = PageRequest.of(offset,limit);
        return balanceTransactionRepository.getBalanceTransactionByUserOrderByTimeDesc(currentUser, nextPage);
    }
}
