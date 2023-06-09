package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.exception.*;
import com.example.MyBookShopApp.dto.ContactConfirmationError;
import io.jsonwebtoken.JwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmptySearchException.class)
    public String handleEmptySearchException(EmptySearchException e, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("searchError", e);
        return "redirect:/";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("searchError", e);
        return "redirect:/";
    }

    @ExceptionHandler(UserExistException.class)
    public String handUserExistException(UserExistException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("resultError", e);
        return "redirect:/signup";
    }

    @ExceptionHandler(EmptyException.class)
    public ResponseEntity<ContactConfirmationError> handEmptyException(EmptyException e) {
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(e.getMessage());
        return ResponseEntity.badRequest().body(contactConfirmationError);
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public HttpServletResponse handleJwtException(HttpServletResponse httpServletResponse)
            throws IOException {

        httpServletResponse.sendRedirect("/logout");
        return httpServletResponse;
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseBody
    public ContactConfirmationError handleNumberFormatException() {
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError("Incorrect sum value was entered");
        return contactConfirmationError;
    }

    @ExceptionHandler(NegativeBalanceException.class)
    public String handleNegativeBalanceException(NegativeBalanceException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("resultError", e.getMessage());
        return "redirect:/profile#topup";
    }

    @ExceptionHandler({PhoneUserNotFoundException.class, UsernameNotFoundException.class, RecordExistException.class,
     EmptyPasswordException.class, SmsCodeException.class, MailCodeException.class, CodesNotFoundException.class})
    @ResponseBody
    public ContactConfirmationError handlePhoneUserNotFoundException(Exception exception) {
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(exception.getMessage());
        return contactConfirmationError;
    }
}
