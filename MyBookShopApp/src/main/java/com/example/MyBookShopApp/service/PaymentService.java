package com.example.MyBookShopApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class PaymentService {

    @Value("${robokassa.merchant.login}")
    private String merchantLogin;

    @Value("${robokassa.pass.first.test}")
    private String firstTestPass;
    public String getPaymentUrl(String sum) throws NoSuchAlgorithmException, NumberFormatException {
        double doubleSum = Double.parseDouble(sum);
        MessageDigest md = MessageDigest.getInstance("MD5");
        String invId = "5"; //Номер счета только для тестовых целей
        md.update((merchantLogin + ":" + doubleSum + ":" + invId + ":" + firstTestPass).getBytes());
        return "https://auth.robokassa.ru/Merchant/Index.aspx" +
                "?MerchantLogin=" + merchantLogin +
                "&InvId=" + invId +
                "&Culture=ru" +
                "&Encoding=utf-8" +
                "&OutSum=" + doubleSum +
                "&SignatureValue=" + DatatypeConverter.printHexBinary(md.digest()).toUpperCase() +
                "&IsTest=1";
    }
}
