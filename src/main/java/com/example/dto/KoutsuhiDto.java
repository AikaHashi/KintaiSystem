package com.example.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KoutsuhiDto {

    private Integer koutsuhiId;
    private String userId;

    @NotNull(message = "日付は必須です")
    private LocalDate date;

    @NotBlank(message = "交通手段は必須です")
    private String method;

    @NotBlank(message = "出発地は必須です")
    private String departure;

    @NotBlank(message = "到着地は必須です")
    private String arrival;

    private String via;

    @NotNull(message = "金額は必須です")
    private Integer amount;

    // 任意
    private String note;
}