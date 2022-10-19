package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.logging.annotation.DebugLogs;
import com.example.MyBookShopApp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@DebugLogs
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findUserById(Integer id);

    Optional<User> findUserByEmail(String email);

    Optional<User> findUserByPhone(String phone);
}
