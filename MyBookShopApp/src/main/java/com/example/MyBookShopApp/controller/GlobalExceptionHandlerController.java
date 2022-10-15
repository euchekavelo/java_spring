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

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ContactConfirmationError handleUsernameNotFoundException(UsernameNotFoundException ex) {
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(ex.getMessage());
        return contactConfirmationError;
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

    @ExceptionHandler(RecordExistException.class)
    @ResponseBody
    public ContactConfirmationError handleRecordExistException(RecordExistException e) {
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(e.getMessage());
        return contactConfirmationError;
    }

    @ExceptionHandler({JwtException.class, IllegalArgumentException.class})
    public HttpServletResponse handleJwtException(JwtException e, HttpServletResponse httpServletResponse)
            throws IOException {

        httpServletResponse.sendRedirect("/logout");
        return httpServletResponse;
    }
}
