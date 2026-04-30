package com.example.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Length;

import com.example.form.ValidGroup1;

import lombok.Data;

@Data
public class KintaiDto {

    private String userId;
    private LocalDate workDate;
    private String userName;
    private String updatedBy;
    private Timestamp updatedAt;

    // 時刻（全部 LocalTime に統一）
    private LocalTime plannedWorkStartTime;
    private LocalTime plannedWorkEndTime;
    private LocalTime plannedBreakStartTime;
    private LocalTime plannedBreakEndTime;

    private LocalTime actualWorkStartTime;
    private LocalTime actualWorkEndTime;
    private LocalTime actualBreakStartTime;
    private LocalTime actualBreakEndTime;

    // 数値
    private BigDecimal scheduledWorkHours;
    private BigDecimal actualWorkHours;
    private BigDecimal overtimeHours;
    private BigDecimal deductionTime;

    private String kintaiStatus;

    @Length(max = 20, message = "コメントは20文字以内で入力してください", groups = ValidGroup1.class)
    private String kintaiComment;
}