package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.PhoneCode;
import com.example.MyBookShopApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneCodeRepository extends JpaRepository<PhoneCode, Integer> {

    void deleteByNewPhoneAndConfirm(String phone, Boolean confirm);

    void deleteByConfirmAndDestinationAndUser(Boolean confirm, String destination, User user);

    Optional<PhoneCode> findByNewPhoneAndCodeAndDestination(String phone, String code, String destination);

    Optional<PhoneCode> findPhoneCodeByCodeAndUserAndConfirmAndDestination(String code, User user, Boolean confirm,
                                                                           String destination);

    Optional<PhoneCode> findPhoneCodeByNewPhoneAndConfirmAndDestinationAndUserOrderByExpireTimeDesc(String phone,
                                                                                                    Boolean confirm,
                                                                                                    String destination,
                                                                                                    User user);
}
