package com.example.MyBookShopApp.model;

import javax.persistence.*;

@Entity
@Table(name = "balance_transaction2book")
public class BalanceTransaction2Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "balance_transaction_id")
    private Integer balanceTransactionId;

    @Column(name = "book_id")
    private Integer bookId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBalanceTransactionId() {
        return balanceTransactionId;
    }

    public void setBalanceTransactionId(Integer balanceTransactionId) {
        this.balanceTransactionId = balanceTransactionId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
}
