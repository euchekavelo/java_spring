package com.example.MyBookShopApp.controller;

import com.example.MyBookShopApp.exception.*;
import com.example.MyBookShopApp.dto.ContactConfirmationError;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.logging.Logger;

@ControllerAdvice
public class GlobalExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmptySearchException.class)
    public String handleEmptySearchException(EmptySearchException e, RedirectAttributes redirectAttributes){
        Logger.getLogger(this.getClass().getSimpleName()).warning(e.getLocalizedMessage());
        redirectAttributes.addFlashAttribute("searchError", e);
        return "redirect:/";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFoundException(NotFoundException e, RedirectAttributes redirectAttributes) {
        Logger.getLogger(this.getClass().getSimpleName()).warning(e.getMessage());
        redirectAttributes.addFlashAttribute("searchError", e);
        return "redirect:/";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ContactConfirmationError handleUsernameNotFoundException(UsernameNotFoundException ex) {
        Logger.getLogger(this.getClass().getSimpleName()).severe(ex.getMessage());
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(ex.getMessage());
        return contactConfirmationError;
    }

    @ExceptionHandler(UserExistException.class)
    public String handUserExistException(UserExistException e, RedirectAttributes redirectAttributes) {
        Logger.getLogger(this.getClass().getSimpleName()).severe(e.getMessage());
        redirectAttributes.addFlashAttribute("resultError", e);
        return "redirect:/signup";
    }

    @ExceptionHandler(EmptyException.class)
    public ResponseEntity<ContactConfirmationError> handEmptyException(EmptyException e) {
        Logger.getLogger(this.getClass().getSimpleName()).severe(e.getMessage());
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(e.getMessage());
        return ResponseEntity.badRequest().body(contactConfirmationError);
    }

    @ExceptionHandler(RecordExistException.class)
    @ResponseBody
    public ContactConfirmationError handleRecordExistException(RecordExistException e) {
        Logger.getLogger(this.getClass().getSimpleName()).severe(e.getMessage());
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(e.getMessage());
        return contactConfirmationError;
    }
}
