package com.example.MyBookShopApp.dto;

import com.example.MyBookShopApp.model.BalanceTransaction;

import java.util.List;
import java.util.stream.Collectors;

public class TransactionPageDto {

    private Integer count;
    private List<ShortTransactionDto> transactions;

    public TransactionPageDto(List<BalanceTransaction> balanceTransactionList) {
        this.transactions = balanceTransactionList.stream().map(ShortTransactionDto::new).collect(Collectors.toList());
        this.count = balanceTransactionList.size();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<ShortTransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<ShortTransactionDto> transactions) {
        this.transactions = transactions;
    }
}
