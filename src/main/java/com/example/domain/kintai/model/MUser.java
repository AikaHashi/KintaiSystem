package com.example.domain.kintai.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MUser {

    private String userId;
    private String password;
    private String userName;
    private LocalDate birthday;
    private Integer gender;
    private Integer departmentId;
    private String role;

    // フォーマットされた誕生日を保持するフィールド（追加された部分）
    private String formattedBirthday;

    public void setFormattedBirthday(String formattedBirthday) {
        this.formattedBirthday = formattedBirthday;
    }

}