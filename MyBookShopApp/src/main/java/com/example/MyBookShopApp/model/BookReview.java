package com.example.MyBookShopApp.model;

import org.hibernate.annotations.Formula;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "book_review")
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TIMESTAMP NOT NULL")
    private LocalDateTime time;

    @Column(columnDefinition = "TEXT NOT NULL")
    private String text;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "bookReview")
    private Set<BookReviewLike> bookReviewLikeSet;

    @Formula("(select count(*) from book_review_like brl where brl.review_id = id and brl.value = 1)")
    private int countOfLikes;

    @Formula("(select count(*) from book_review_like brl where brl.review_id = id and brl.value = -1)")
    private int countOfDislikes;

    public int getRating() {
        return countOfLikes - countOfDislikes;
    }

    public int getCountOfDislikes() {
        return countOfDislikes;
    }

    public int getCountOfLikes() {
        return countOfLikes;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<BookReviewLike> getBookReviewLikeSet() {
        return new HashSet<>(bookReviewLikeSet);
    }

    public Book getBook() {
        return book;
    }

    public User getUser() {
        return user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return time.format(formatter);
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
