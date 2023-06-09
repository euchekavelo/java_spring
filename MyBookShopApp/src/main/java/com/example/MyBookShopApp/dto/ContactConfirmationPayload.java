package com.example.MyBookShopApp.dto;

public class ContactConfirmationPayload {

    private String contact;
    private String code;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "ContactConfirmationPayload{" +
                "contact='" + contact + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
