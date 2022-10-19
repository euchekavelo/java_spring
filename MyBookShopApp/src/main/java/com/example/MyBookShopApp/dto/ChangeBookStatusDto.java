package com.example.MyBookShopApp.dto;

public class ChangeBookStatusDto {

    private String status;
    private Integer booksIds;

    public ChangeBookStatusDto(String status, Integer booksIds) {
        this.status = status;
        this.booksIds = booksIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getBooksIds() {
        return booksIds;
    }

    public void setBooksIds(Integer booksIds) {
        this.booksIds = booksIds;
    }

    @Override
    public String toString() {
        return "ChangeBookStatusDto{" +
                "status='" + status + '\'' +
                ", booksIds=" + booksIds +
                '}';
    }
}
