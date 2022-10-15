package com.example.MyBookShopApp.dto;

public class ContactConfirmationError {

    private boolean result;
    private String error;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ContactConfirmationError{" +
                "result=" + result +
                ", error='" + error + '\'' +
                '}';
    }
}
