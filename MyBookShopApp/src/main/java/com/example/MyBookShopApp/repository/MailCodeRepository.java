package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.MailCode;
import com.example.MyBookShopApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailCodeRepository extends JpaRepository<MailCode, Integer> {

    void deleteByNewEmailAndConfirm(String email, Boolean confirm);

    void deleteByUser(User user);

    Optional<MailCode> findMailCodeByNewEmailAndCodeAndDestination(String email, String code, String description);

    Optional<MailCode> findMailCodeByNewEmailAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(String email,
                                                                                                  Boolean confirm,
                                                                                                  String destination,
                                                                                                  User user);

    Optional<MailCode> findMailCodeByCodeAndUserAndConfirmAndDestination(String code, User user, Boolean confirm,
                                                                         String destination);
}
