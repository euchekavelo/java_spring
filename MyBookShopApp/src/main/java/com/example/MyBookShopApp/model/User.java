package com.example.MyBookShopApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.sql.Date;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String hash;
    private Date regTime;

    @ColumnDefault("0.0")
    private Double balance;
    private String name;
    private String email;
    private String password;
    private String phone;

    @OneToMany(mappedBy = "user")
    private List<MailCode> mailCodeList;

    @OneToMany(mappedBy = "user")
    private List<PhoneCode> phoneCodeList;

    @OneToMany(mappedBy = "user")
    private List<BalanceTransaction> balanceTransactionList;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @ManyToMany(mappedBy = "listAppraisers")
    @JsonIgnore
    List<Book> bookList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getRegTime() {
        return regTime;
    }

    public void setRegTime(Date regTime) {
        this.regTime = regTime;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BalanceTransaction> getBalanceTransactionList() {
        return balanceTransactionList;
    }

    public List<MailCode> getMailCodeList() {
        return mailCodeList;
    }

    public List<PhoneCode> getPhoneCodeList() {
        return phoneCodeList;
    }
}
