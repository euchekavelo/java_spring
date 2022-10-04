package com.example.MyBookShopApp.dto;

public class BookRatingDto {

    private Integer bookId;
    private Integer value;

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
