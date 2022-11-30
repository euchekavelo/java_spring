package com.example.MyBookShopApp.security.service;

import com.example.MyBookShopApp.dto.UserDataChangeDto;
import com.example.MyBookShopApp.exception.*;
import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.dto.ContactConfirmationPayload;
import com.example.MyBookShopApp.dto.ContactConfirmationResponse;
import com.example.MyBookShopApp.model.BalanceTransaction;
import com.example.MyBookShopApp.model.MailCode;
import com.example.MyBookShopApp.model.PhoneCode;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.BalanceTransactionRepository;
import com.example.MyBookShopApp.repository.MailCodeRepository;
import com.example.MyBookShopApp.repository.PhoneCodeRepository;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.jwt.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final MailCodeRepository mailCodeRepository;
    private final PhoneCodeRepository phoneCodeRepository;

    @Autowired
    public BookstoreUserAuthorization(AuthenticationManager authenticationManager,
                                      BookstoreUserDetailsService bookstoreUserDetailsService,
                                      JWTUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder,
                                      BalanceTransactionRepository balanceTransactionRepository,
                                      MailCodeRepository mailCodeRepository, PhoneCodeRepository phoneCodeRepository) {

        this.authenticationManager = authenticationManager;
        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.balanceTransactionRepository = balanceTransactionRepository;
        this.mailCodeRepository = mailCodeRepository;
        this.phoneCodeRepository = phoneCodeRepository;
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

    public ContactConfirmationResponse jwtLoginByPhoneNumber(String phone) {
        UserDetails userDetails = bookstoreUserDetailsService.loadUserByUsername(phone);
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

    public void changeUserData(UserDataChangeDto userDataChangeDto) throws EmptyPasswordException, MailCodeException,
            SmsCodeException, CodesNotFoundException {

        String newPassword = userDataChangeDto.getPassword();
        String newPasswordReply = userDataChangeDto.getPasswordReply();

        if ((newPassword.isEmpty() || newPasswordReply.isEmpty()) || !newPassword.equals(newPasswordReply)) {
            throw new EmptyPasswordException("The value of the new password is empty or does not match " +
                    "the value of the confirmation password.");
        }

        User userDatabase = getCurrentUser();
        checkTransmittedData(userDatabase, userDataChangeDto);
    }

    private void checkTransmittedData(User userDatabase, UserDataChangeDto userDataChangeDto) throws MailCodeException,
            SmsCodeException, CodesNotFoundException {

        String newPassword = userDataChangeDto.getPassword();
        String newEmail = userDataChangeDto.getMail();
        String newName = userDataChangeDto.getName();
        String newPhone = userDataChangeDto.getPhone().replaceAll("[+\\s()-]*","");

        if (newPhone.equals(userDatabase.getPhone()) && newEmail.equals(userDatabase.getEmail())) {
            changePropertiesAuthorizedUser(userDatabase, newName, newEmail, newPhone, newPassword);

        } else if (newPhone.equals(userDatabase.getPhone()) && !newEmail.equals(userDatabase.getEmail())) {
            Optional<MailCode> optionalMailCode = mailCodeRepository
                    .findMailCodeByNewEmailAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(newEmail, true,
                            "Mail change", userDatabase);

            if (optionalMailCode.isPresent()) {
                changePropertiesAuthorizedUser(userDatabase, newName, newEmail, newPhone, newPassword);
            } else {
                throw new MailCodeException("You need to confirm the change of the current email address.");
            }
        } else if (!newPhone.equals(userDatabase.getPhone()) && newEmail.equals(userDatabase.getEmail())) {
            Optional<PhoneCode> optionalPhoneCode = phoneCodeRepository
                    .findPhoneCodeByNewPhoneAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(newPhone, true,
                            "Phone change", userDatabase);

            if (optionalPhoneCode.isPresent()) {
                changePropertiesAuthorizedUser(userDatabase, newName, newEmail, newPhone, newPassword);
            } else {
                throw new SmsCodeException("You need to confirm the change of the current phone number.");
            }
        } else {
            Optional<PhoneCode> optionalPhoneCode = phoneCodeRepository
                    .findPhoneCodeByNewPhoneAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(newPhone, true,
                            "Phone change", userDatabase);

            Optional<MailCode> optionalMailCode = mailCodeRepository
                    .findMailCodeByNewEmailAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(newEmail, true,
                            "Mail change", userDatabase);

            if (optionalPhoneCode.isPresent() && optionalMailCode.isPresent()) {
                changePropertiesAuthorizedUser(userDatabase, newName, newEmail, newPhone, newPassword);
            } else {
                throw new CodesNotFoundException("You need to confirm the change of the current email address, " +
                        "as well as the current phone number.");
            }
        }
    }

    private void changePropertiesAuthorizedUser(User user, String name, String email, String phone, String password) {

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        setUserAuthentication(email);
    }

    private void setUserAuthentication(String email) {
        BookstoreUserDetails userDetails =
                (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
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
