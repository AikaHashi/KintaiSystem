package com.example.domain.kintai.model;

import java.time.LocalDate;

import lombok.Data;

@Data
//@Entity
//@Table(name = "m_user")
public class MUser {

//    @Id
    private String userId;
    private String password;
    private String userName;
    private LocalDate birthday;
    private Integer gender;
    private Integer departmentId;
    private String role;

    // フォーマットされた誕生日を保持するフィールド（追加された部分）
    private String formattedBirthday;

    // 部門との関連
//    @ManyToOne(optional = true)
//    @JoinColumn(insertable = false, updatable = false, name = "departmentId")
    //private Department department;

    // 給与リストとの関連
//    @OneToMany(mappedBy = "user")
//    private List<Salary> salaryList;

    // フォーマットされた誕生日のgetterとsetter
//    public String getFormattedBirthday() {
//        return formattedBirthday;
//    }

    public void setFormattedBirthday(String formattedBirthday) {
        this.formattedBirthday = formattedBirthday;
    }

    // その他のgetter/setter
}