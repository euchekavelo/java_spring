package com.example.MyBookShopApp.dto;

public class BookReviewLikeDto {

    private Integer reviewId;
    private Short value;

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
        this.reviewId = reviewId;
    }

    public Short getValue() {
        return value;
    }

    public void setValue(Short value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BookReviewLikeDto{" +
                "reviewId=" + reviewId +
                ", value=" + value +
                '}';
    }
}
