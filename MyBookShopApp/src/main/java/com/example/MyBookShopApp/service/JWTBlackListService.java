package com.example.MyBookShopApp.service;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.exception.UsedTokenException;
import com.example.MyBookShopApp.model.JWTBlackList;
import com.example.MyBookShopApp.repository.JWTBlackListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@DebugLogs
public class JWTBlackListService {

    private final JWTBlackListRepository jwtBlackListRepository;

    @Autowired
    public JWTBlackListService(JWTBlackListRepository jwtBlackListRepository) {
        this.jwtBlackListRepository = jwtBlackListRepository;
    }

    public void saveToken(String token) {
        if (!getTokenByValue(token).isPresent()) {
            JWTBlackList jwtBlackList = new JWTBlackList();
            jwtBlackList.setValue(token);
            jwtBlackListRepository.save(jwtBlackList);
        }
    }

    public Optional<JWTBlackList> getTokenByValue(String token) {
        return jwtBlackListRepository.findJWTBlackListByValue(token);
    }

    public boolean tokenIsNotInTheTable(String token) {
        if (getTokenByValue(token).isPresent()) {
            throw new UsedTokenException("The value of this token has been used previously.");
        } else {
            return true;
        }
    }
}
