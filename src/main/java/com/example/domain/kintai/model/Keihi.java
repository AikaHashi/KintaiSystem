package com.example.domain.kintai.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Keihi {
    private Integer keihiId;
    private String userId;
    private LocalDate date;
    private String method;
    private String departure;
    private String arrival;
    private String via;
    private Integer amount;
    private String note;
    private LocalDateTime createdAt;
}
