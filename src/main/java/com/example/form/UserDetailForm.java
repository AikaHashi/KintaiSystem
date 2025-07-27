package com.example.form;

import java.time.LocalDate;

import com.example.domain.kintai.model.Department;

import lombok.Data;

@Data
public class UserDetailForm {
    private String userId;
    private String password;
    private String userName;
    private LocalDate  birthday;
    private Integer age;
    private Integer gender;
    private Department department;
}