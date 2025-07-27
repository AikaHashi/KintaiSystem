package com.example.domain.kintai.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Koutsuhi {
    private Integer koutsuhiId;
    private String userId;
    private LocalDate date;
    private String method;
    private String departure; // 出発地
    private String arrival; // 到着地
    private String via; // 経由地
    private Integer amount;
    private String note;
    private LocalDateTime createdAt;
}
