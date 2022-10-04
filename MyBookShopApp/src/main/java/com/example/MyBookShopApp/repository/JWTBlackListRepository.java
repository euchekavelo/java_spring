package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.JWTBlackList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JWTBlackListRepository extends JpaRepository<JWTBlackList, Integer> {

    Optional<JWTBlackList> findJWTBlackListByValue(String value);
}