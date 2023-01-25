package com.example.MyBookShopApp.exception;

public class NegativeBalanceException extends Exception {

    public NegativeBalanceException(String message) {
        super(message);
    }
}
