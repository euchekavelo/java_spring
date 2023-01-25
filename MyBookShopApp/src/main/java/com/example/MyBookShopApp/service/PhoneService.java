package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.dto.ContactConfirmationResponse;
import com.example.MyBookShopApp.dto.SmsCodeResponseDto;
import com.example.MyBookShopApp.exception.PhoneUserNotFoundException;
import com.example.MyBookShopApp.exception.SmsCodeException;
import com.example.MyBookShopApp.model.PhoneCode;
import com.example.MyBookShopApp.model.User;
import com.example.MyBookShopApp.repository.PhoneCodeRepository;
import com.example.MyBookShopApp.repository.UserRepository;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@Service
public class PhoneService {

    @Value("${sms.ru.apiId}")
    private String apiId;

    private final PhoneCodeRepository phoneCodeRepository;
    private final UserRepository userRepository;
    private final BookstoreUserAuthorization bookstoreUserAuthorization;

    @Autowired
    public PhoneService(PhoneCodeRepository phoneCodeRepository, UserRepository userRepository,
                        BookstoreUserAuthorization bookstoreUserAuthorization) {

        this.phoneCodeRepository = phoneCodeRepository;
        this.userRepository = userRepository;
        this.bookstoreUserAuthorization = bookstoreUserAuthorization;
    }

    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 6) {
            sb.append(random.nextInt(9));
        }

        return sb.toString();
    }

    private void sendSmsCode(String code, String phone) throws JsonProcessingException, SmsCodeException {
        String url = "https://sms.ru/sms/send" +
                "?api_id=" + apiId +
                "&to=" + phone +
                "&msg=" + code +
                "&json=1";

        WebClient webClient = WebClient.create(url);
        ResponseEntity<String> objectResponseEntity = Objects.requireNonNull(webClient
                        .get()
                        .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_XML)
                        .exchange()
                        .block())
                        .toEntity(String.class)
                        .block();

        if (objectResponseEntity != null && objectResponseEntity.getStatusCode().is2xxSuccessful()) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(objectResponseEntity.getBody());
            JsonNode jsonNodePhoneResponse = jsonNode.get("sms").get(phone);

            SmsCodeResponseDto smsCodeResponseDto =
                    objectMapper.treeToValue(jsonNodePhoneResponse, SmsCodeResponseDto.class);

            if (smsCodeResponseDto.getStatusCode() == 100 && smsCodeResponseDto.getStatus().equals("OK")) {
            } else {
                throw new SmsCodeException(smsCodeResponseDto.getStatusText());
            }
        } else {
            throw new SmsCodeException("При взаимодействии с смс-сервисом возникли проблемы.");
        }
    }

    @Transactional
    public void createPhoneRegistrationCode(String contact) throws SmsCodeException, JsonProcessingException {
        String formattedPhone = contact.replaceAll("[+\\s()-]*","");
        String stringSmsCode = generateCode();

        phoneCodeRepository.deleteByNewPhoneAndConfirm(formattedPhone, false);
        sendSmsCode(stringSmsCode, formattedPhone);

        PhoneCode phoneCode = new PhoneCode();
        phoneCode.setCode(stringSmsCode);
        phoneCode.setNewPhone(formattedPhone);
        phoneCode.setConfirm(false);
        phoneCode.setDestination("Phone registration");
        phoneCode.setExpireTime(LocalDateTime.now().plusSeconds(120));
        phoneCodeRepository.save(phoneCode);
    }

    @Transactional
    public void createPhoneCodeToChangeNumber(String phone) throws SmsCodeException, JsonProcessingException {
        String formattedPhone = phone.replaceAll("[+\\s()-]*","");
        String smsCode = generateCode();
        User currentUser = bookstoreUserAuthorization.getCurrentUser();

        phoneCodeRepository.deleteByConfirmAndDestinationAndUser(false, "Phone change", currentUser);
        sendSmsCode(smsCode, currentUser.getPhone());

        PhoneCode phoneCode = new PhoneCode();
        phoneCode.setCode(smsCode);
        phoneCode.setNewPhone(formattedPhone);
        phoneCode.setUser(currentUser);
        phoneCode.setConfirm(false);
        phoneCode.setDestination("Phone change");
        phoneCode.setExpireTime(LocalDateTime.now().plusSeconds(120));
        phoneCodeRepository.save(phoneCode);
    }

    public void checkPhoneCodeToChangeNumber(String code) throws SmsCodeException {
        User currentUser = bookstoreUserAuthorization.getCurrentUser();
        Optional<PhoneCode> optionalPhoneCode = phoneCodeRepository
                .findPhoneCodeByCodeAndUserAndConfirmAndDestination(code, currentUser, false, "Phone change");

        if (optionalPhoneCode.isPresent()) {
            PhoneCode phoneCode = optionalPhoneCode.get();
            if(!phoneCode.isExpired()) {
                phoneCode.setConfirm(true);
                phoneCodeRepository.save(phoneCode);
            } else {
                phoneCodeRepository.delete(phoneCode);
                throw new SmsCodeException("Phone code expired. Please refresh the current page and try again.");
            }
        } else {
            throw new SmsCodeException("Invalid or non-existent code specified. " +
                    "Please refresh the current page and try again.");
        }
    }

    public void checkRegistrationPhoneCode(String phone, String code) throws SmsCodeException {
        String formattedPhone = phone.replaceAll("[+\\s()-]*","");
        String codeWithoutSpace = code.replace(" ", "");

        Optional<PhoneCode> optionalPhoneCode = phoneCodeRepository
                .findByNewPhoneAndCodeAndDestination(formattedPhone, codeWithoutSpace, "Phone registration");

        if (optionalPhoneCode.isPresent()) {
            PhoneCode phoneCode = optionalPhoneCode.get();
            if (!phoneCode.isExpired() && !phoneCode.getConfirm()) {
                phoneCode.setConfirm(true);
                phoneCodeRepository.save(phoneCode);
            } else if (phoneCode.isExpired() && !phoneCode.getConfirm()) {
                phoneCodeRepository.delete(phoneCode);
                throw new SmsCodeException("Phone code expired.");
            }
        } else {
            throw new SmsCodeException("Invalid or non-existent code specified.");
        }
    }

    public void sendSmsCodeToRegisteredNumber(String phone) throws PhoneUserNotFoundException, SmsCodeException,
            JsonProcessingException {

        String formattedPhone = phone.replaceAll("[+\\s()-]*","");
        Optional<User> optionalUser = userRepository.findUserByPhone(formattedPhone);
        if (!optionalUser.isPresent()) {
            throw new PhoneUserNotFoundException("No registered user found.");
        }

        User user = optionalUser.get();
        String userPhone = user.getPhone();
        String code = generateCode();
        sendSmsCode(code, userPhone);

        PhoneCode phoneCode = new PhoneCode();
        phoneCode.setCode(code);
        phoneCode.setNewPhone(formattedPhone);
        phoneCode.setDestination("Authorized login");
        phoneCode.setExpireTime(LocalDateTime.now().plusSeconds(90));
        phoneCodeRepository.save(phoneCode);
    }

    public ContactConfirmationResponse checkTheSentLoginSmsCode(String phone, String code)
            throws SmsCodeException {

        String formattedPhone = phone.replaceAll("[+\\s()-]*","");
        String codeWithoutSpace = code.replace(" ", "");
        Optional<PhoneCode> optionalPhoneCode = phoneCodeRepository
                .findByNewPhoneAndCodeAndDestination(formattedPhone, codeWithoutSpace, "Authorized login");

        if (optionalPhoneCode.isPresent()) {
            PhoneCode phoneCode = optionalPhoneCode.get();
            if (!phoneCode.isExpired()) {
                phoneCodeRepository.delete(phoneCode);
                return bookstoreUserAuthorization.jwtLoginByPhoneNumber(formattedPhone);
            } else {
                phoneCodeRepository.delete(phoneCode);
                throw new SmsCodeException("Phone code expired.");
            }
        } else {
            throw new SmsCodeException("Invalid or non-existent code specified.");
        }
    }
}
