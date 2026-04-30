package com.example.form;

import java.time.LocalDate;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDetailForm {
    private String userId;
    
    /** パスワード */
    @NotBlank(message = "パスワードは必須です")
    @Length(min = 4, max = 100, message = "パスワードは4文字以上100文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "パスワードは半角英数字で入力してください")
    private String password;

    /** ユーザー名 */
    @NotBlank(message = "ユーザー名は必須です")
     private String userName;
    
    private LocalDate  birthday;
    private Integer age;
    private Integer gender;
   
}