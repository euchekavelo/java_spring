package com.example.MyBookShopApp.exception;

import io.jsonwebtoken.JwtException;

public class UsedTokenException extends JwtException {
    public UsedTokenException(String message) {
        super(message);
    }
}
