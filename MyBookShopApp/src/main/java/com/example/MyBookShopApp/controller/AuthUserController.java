package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.dto.*;
import com.example.MyBookShopApp.logging.annotation.InfoLogs;
import com.example.MyBookShopApp.exception.EmptyException;
import com.example.MyBookShopApp.exception.UserExistException;
import com.example.MyBookShopApp.security.service.BookstoreUserAuthorization;
import com.example.MyBookShopApp.security.service.BookstoreUserRegister;
import com.example.MyBookShopApp.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public AuthUserController(BookstoreUserRegister userRegister, BookstoreUserAuthorization userAuthorization,
                              PaymentService paymentService) {
        this.userRegister = userRegister;
        this.userAuthorization = userAuthorization;
        this.paymentService = paymentService;
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
    public ContactConfirmationResponse handleRequestContactConfirmation(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
        return response;
    }

    @PostMapping("/approveContact")
    @ResponseBody
    public ContactConfirmationResponse handleApproveContact(@RequestBody ContactConfirmationPayload payload) {
        ContactConfirmationResponse response = new ContactConfirmationResponse();
        response.setResult("true");
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
    public String changeProfileInformation(UserDataChangeDto userDataChangeDto) {
        userAuthorization.changeUserData(userDataChangeDto);
        return "redirect:/profile";
    }

    @PostMapping("/credit")
    public RedirectView handlePay(HttpServletRequest httpServletRequest) {
        String signatureValue = httpServletRequest.getParameter("SignatureValue");
        String sum = httpServletRequest.getParameter("OutSum");
        if (signatureValue != null && !signatureValue.isEmpty() && sum != null && !sum.isEmpty()) {
            userAuthorization.creditMoneyToTheUsersBalance(Double.parseDouble(sum));
        }
        return new RedirectView("profile#topup");
    }

    @PostMapping("/payment")
    @ResponseBody
    public PaymentUrlDto transferFundsToAPersonalAccount(@RequestBody TransferMoneyDto transferMoneyDto)
            throws NoSuchAlgorithmException, NumberFormatException {
        String url = paymentService.getPaymentUrl(transferMoneyDto.getSum());
        PaymentUrlDto paymentUrlDto = new PaymentUrlDto();
        paymentUrlDto.setUrl(url);
        return paymentUrlDto;
    }

    @GetMapping("/transactions")
    @ResponseBody
    public TransactionPageDto getUserTransactions(@RequestParam("offset") Integer offset,
                                                  @RequestParam("limit") Integer limit) {

        return new TransactionPageDto(userAuthorization.getPageOfBalanceTransaction(offset, limit));
    }
}
