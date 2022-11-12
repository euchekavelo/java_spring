package com.example.MyBookShopApp.repository;

import com.example.MyBookShopApp.model.BalanceTransaction;
import com.example.MyBookShopApp.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceTransactionRepository extends JpaRepository<BalanceTransaction, Integer> {

    List<BalanceTransaction> getBalanceTransactionByUserOrderByTimeDesc(User user, Pageable nextPage);
}
