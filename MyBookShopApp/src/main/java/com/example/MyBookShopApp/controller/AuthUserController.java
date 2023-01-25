package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.dto.*;
import com.example.MyBookShopApp.exception.*;
import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import com.example.MyBookShopApp.security.service.BookstoreUserRegister;
import com.example.MyBookShopApp.service.MailService;
import com.example.MyBookShopApp.service.PaymentService;
import com.example.MyBookShopApp.service.PhoneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.NoSuchAlgorithmException;

@Controller
@InfoLogs
public class AuthUserController {

    private final BookstoreUserRegister userRegister;
    private final BookstoreUserAuthorization userAuthorization;
    private final PaymentService paymentService;
    private final MailService mailService;
    private final PhoneService phoneService;

    @Autowired
    public AuthUserController(BookstoreUserRegister userRegister, BookstoreUserAuthorization userAuthorization,
                              PaymentService paymentService, MailService mailService, PhoneService phoneService) {
        this.userRegister = userRegister;
        this.userAuthorization = userAuthorization;
        this.paymentService = paymentService;
        this.mailService = mailService;
        this.phoneService = phoneService;
    }

    @GetMapping("/signin")
    public String handleSignIn() {
        return "signin";
    }

    @GetMapping("/signup")
    public String handleSignUp(Model model) {
        model.addAttribute("regForm", new RegistrationForm());
        return "signup";
    }

    @PostMapping("/requestContactConfirmation")
    @ResponseBody
    public ContactConfirmationResponse handleRequestContactConfirmation(@RequestBody ContactConfirmationPayload payload)
            throws PhoneUserNotFoundException, SmsCodeException, JsonProcessingException {

        ContactConfirmationResponse response = new ContactConfirmationResponse();
        if (!payload.getContact().contains("@")) {
            phoneService.sendSmsCodeToRegisteredNumber(payload.getContact());
        }
        response.setResult("true");
        return response;
    }

    @PostMapping("/requestPhoneConfirmation")
    @ResponseBody
    public ContactConfirmationResponse handleRequestPhoneConfirmation(@RequestBody ContactConfirmationPayload payload)
            throws SmsCodeException, JsonProcessingException {

        ContactConfirmationResponse response = new ContactConfirmationResponse();
        phoneService.createPhoneRegistrationCode(payload.getContact());
        response.setResult("true");
        return response;
    }

    @PostMapping("/requestEmailConfirmation")
    @ResponseBody
    public ContactConfirmationResponse handleRequestEmailConfirmation(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        mailService.createMailRegistrationCode(payload.getContact());
        response.setResult("true");
        return response;
    }

    @PostMapping("/approveContact")
    @ResponseBody
    public ContactConfirmationResponse handleApproveContact(@RequestBody ContactConfirmationPayload payload)
            throws MailCodeException, SmsCodeException {
        ContactConfirmationResponse response = new ContactConfirmationResponse();

        if(payload.getContact().contains("@")) {
            mailService.checkRegistrationMailCode(payload.getContact(), payload.getCode());
            response.setResult("true");
        } else {
            phoneService.checkRegistrationPhoneCode(payload.getContact(), payload.getCode());
            response.setResult("true");
        }
        return response;
    }

    @PostMapping("/reg")
    public String handleUserRegistration(RegistrationForm registrationForm, Model model) throws UserExistException,
            EmptyException {
        userRegister.registerNewUser(registrationForm);
        model.addAttribute("regOk", true);
        return "signin";
    }

    @PostMapping("/login")
    @ResponseBody
    public ContactConfirmationResponse handleLogin(@RequestBody ContactConfirmationPayload payload,
                                                   HttpServletResponse httpServletResponse) {
        ContactConfirmationResponse loginResponse = userAuthorization.jwtLogin(payload);
        Cookie cookie = new Cookie("token", loginResponse.getResult());
        httpServletResponse.addCookie(cookie);
        return loginResponse;
    }

    @PostMapping("/login-by-phone-number")
    @ResponseBody
    public ContactConfirmationResponse handleLoginByPhoneNumber(@RequestBody ContactConfirmationPayload payload,
                                                                HttpServletResponse httpServletResponse)
            throws SmsCodeException {

        ContactConfirmationResponse loginResponse =
                phoneService.checkTheSentLoginSmsCode(payload.getContact(), payload.getCode());

        Cookie cookie = new Cookie("token", loginResponse.getResult());
        httpServletResponse.addCookie(cookie);
        return loginResponse;
    }

    @GetMapping("/my")
    public String handleMy(Model model) {
        model.addAttribute("curUsr", userAuthorization.getCurrentUser());
        return "my";
    }

    @GetMapping("/profile")
    public String handleProfile(Model model) {
        model.addAttribute("curUsr", userAuthorization.getCurrentUser());
        model.addAttribute("userDataChangeDto", new UserDataChangeDto());
        model.addAttribute("transferMoneyDto", new TransferMoneyDto());
        model.addAttribute("userTransactions", new TransactionPageDto(userAuthorization.getPageOfBalanceTransaction(0, 1)));
        return "profile";
    }

    @PostMapping("/profile")
    @ResponseBody
    public ContactConfirmationResponse changeProfileInformation(@RequestBody UserDataChangeDto userDataChangeDto)
            throws EmptyPasswordException, MailCodeException, SmsCodeException, CodesNotFoundException {

        userAuthorization.changeUserData(userDataChangeDto);
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/credit")
    public RedirectView handlePay(HttpServletRequest httpServletRequest) {
        String signatureValue = httpServletRequest.getParameter("SignatureValue");
        String sum = httpServletRequest.getParameter("OutSum");
        if (signatureValue != null && !signatureValue.isEmpty() && sum != null && !sum.isEmpty()) {
            userAuthorization.creditMoneyToTheUsersBalance(Double.parseDouble(sum));
        }
        return new RedirectView("profile#topup");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/payment")
    @ResponseBody
    public PaymentUrlDto transferFundsToAPersonalAccount(@RequestBody TransferMoneyDto transferMoneyDto)
            throws NoSuchAlgorithmException, NumberFormatException {
        String url = paymentService.getPaymentUrl(transferMoneyDto.getSum());
        PaymentUrlDto paymentUrlDto = new PaymentUrlDto();
        paymentUrlDto.setUrl(url);
        return paymentUrlDto;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/transactions")
    @ResponseBody
    public TransactionPageDto getUserTransactions(@RequestParam("offset") Integer offset,
                                                  @RequestParam("limit") Integer limit) {

        return new TransactionPageDto(userAuthorization.getPageOfBalanceTransaction(offset, limit));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mailConfirmation")
    @ResponseBody
    public ContactConfirmationResponse confirmNewMail(@RequestBody NewUserMailDto newUserMailDto) {
        mailService.createMailCode(newUserMailDto.getEmail());
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/mailCodeVerification")
    @ResponseBody
    public ContactConfirmationResponse checkMailCode(@RequestBody CodeDto codeDto) throws MailCodeException {

        mailService.checkMailCode(codeDto.getCode());
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/phoneConfirmation")
    @ResponseBody
    public ContactConfirmationResponse confirmNewPhone(@RequestBody NewUserPhoneDto newUserPhoneDto)
            throws SmsCodeException, JsonProcessingException {

        phoneService.createPhoneCodeToChangeNumber(newUserPhoneDto.getPhone());
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/phoneCodeVerification")
    @ResponseBody
    public ContactConfirmationResponse checkPhoneCode(@RequestBody CodeDto codeDto) throws SmsCodeException {
        phoneService.checkPhoneCodeToChangeNumber(codeDto.getCode());
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }
}
