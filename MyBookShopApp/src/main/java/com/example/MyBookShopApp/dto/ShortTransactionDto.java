package com.example.MyBookShopApp.dto;

import com.example.MyBookShopApp.model.BalanceTransaction;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class ShortTransactionDto {

    private Long time;
    private Double value;
    private String description;

    public ShortTransactionDto(BalanceTransaction balanceTransaction) {
        this.time = balanceTransaction.getTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.value = balanceTransaction.getValue();
        this.description = balanceTransaction.getDescription();
    }

    public String getFormattedDateTime() {
        LocalDateTime localDateTime = Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDateTime();
        String formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                .withLocale(new Locale("ru"))
                .format(localDateTime);

        LocalTime localTime = localDateTime.toLocalTime();
        String stringLocalTime = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        return formattedDate.concat(" ").concat(stringLocalTime);
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
