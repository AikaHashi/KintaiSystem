package com.example.domain.kintai.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Keihi {
    private Integer keihiId;
    private String userId;
    
    @NotNull(message = "日付は必須です")
    private LocalDate date;

    @NotBlank(message = "交通手段は必須です")
    private String method;

    @NotBlank(message = "出発地は必須です")
    private String departure;

    @NotBlank(message = "到着地は必須です")
    private String arrival;

    private String via; // 経由地
    @NotNull(message = "金額は必須です")
    private Integer amount;

    @NotBlank(message = "特記事項は必須です")
    private String note;
    private LocalDateTime createdAt;
}

