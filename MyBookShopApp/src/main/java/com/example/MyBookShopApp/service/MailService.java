package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.exception.MailCodeException;
import com.example.MyBookShopApp.model.MailCode;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.MailCodeRepository;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class MailService {

    @Value("${appEmail.email}")
    String email;

    private final JavaMailSender javaMailSender;
    private final MailCodeRepository mailCodeRepository;

    private final BookstoreUserAuthorization bookstoreUserAuthorization;

    public MailService(JavaMailSender javaMailSender, MailCodeRepository mailCodeRepository,
                       BookstoreUserAuthorization bookstoreUserAuthorization) {

        this.javaMailSender = javaMailSender;
        this.mailCodeRepository = mailCodeRepository;
        this.bookstoreUserAuthorization = bookstoreUserAuthorization;
    }

    public void sendMailCode(String recipient, String text, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(recipient);
        message.setSubject("Bookstore email verification.");
        message.setText(text + code);
        javaMailSender.send(message);
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 6) {
            sb.append(random.nextInt(9));
        }

        return sb.toString();
    }

    @Transactional
    public void createMailRegistrationCode(String email) {
        mailCodeRepository.deleteByNewEmailAndConfirm(email, false);
        String code = generateCode();
        MailCode mailCode = new MailCode();
        mailCode.setCode(code);
        mailCode.setConfirm(false);
        mailCode.setDestination("Mail Registration");
        mailCode.setExpireTime(LocalDateTime.now().plusSeconds(60));
        mailCode.setNewEmail(email);

        String textMessage = "Your registration confirmation code: ";
        sendMailCode(email, textMessage, code);
        mailCodeRepository.save(mailCode);
    }

    public void checkRegistrationMailCode(String mail, String code) throws MailCodeException {
        String codeWithoutSpace = code.replace(" ", "");
        Optional<MailCode> optionalMailCode = mailCodeRepository
                .findMailCodeByNewEmailAndCodeAndDestination(mail, codeWithoutSpace, "Mail Registration");

        if (optionalMailCode.isPresent()) {
            MailCode mailCode = optionalMailCode.get();
            if (!mailCode.isExpired() && !mailCode.getConfirm()) {
                mailCode.setConfirm(true);
                mailCodeRepository.save(mailCode);
            } else if (mailCode.isExpired() && !mailCode.getConfirm()) {
                mailCodeRepository.delete(mailCode);
                throw new MailCodeException("Mail code expired.");
            }
        } else {
            throw new MailCodeException("Invalid or non-existent code specified.");
        }
    }

    @Transactional
    public void createMailCode(String newEmail) {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        mailCodeRepository.deleteByUser(currentUser);
        if (!currentUser.getEmail().equals(newEmail)) {
            String code = generateCode();
            MailCode mailCode = new MailCode();
            mailCode.setCode(code);
            mailCode.setUser(currentUser);
            mailCode.setConfirm(false);
            mailCode.setDestination("Mail change");
            mailCode.setExpireTime(LocalDateTime.now().plusSeconds(60));
            mailCode.setNewEmail(newEmail);

            String textMessage = "Code to change the current email address to a new one '" + newEmail + "': ";
            sendMailCode(currentUser.getEmail(), textMessage, code);
            mailCodeRepository.save(mailCode);
        }
    }

    public void checkMailCode(String code) throws MailCodeException {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        Optional<MailCode> optionalMailCode = mailCodeRepository
                .findMailCodeByCodeAndUserAndConfirmAndDestination(code, currentUser, false, "Mail change");

        if (optionalMailCode.isPresent()) {
            MailCode mailCode = optionalMailCode.get();
            if(!mailCode.isExpired()) {
                mailCode.setConfirm(true);
                mailCodeRepository.save(mailCode);
            } else {
                mailCodeRepository.delete(mailCode);
                throw new MailCodeException("Mail code expired. Please refresh the current page and try again.");
            }
        } else {
            throw new MailCodeException("Invalid or non-existent code specified. " +
                    "Please refresh the current page and try again.");
        }
    }
}
