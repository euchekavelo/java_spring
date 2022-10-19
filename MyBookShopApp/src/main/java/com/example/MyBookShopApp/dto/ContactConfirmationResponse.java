package com.example.MyBookShopApp.dto;

public class ContactConfirmationResponse {

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "ContactConfirmationResponse{" +
                "result='" + result + '\'' +
                '}';
    }
}
