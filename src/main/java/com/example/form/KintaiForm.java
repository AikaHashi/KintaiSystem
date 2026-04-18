package com.example.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KintaiForm {
    private String userId;
    private String userName;  
    
    private String updatedBy;
    private Timestamp updatedAt;
    
    private LocalDate workDate;
    @NotNull(message = "予定開始時間は必須です", groups = ValidGroup1.class)
    private LocalTime plannedWorkStartTime;
    @NotNull(message = "予定終了時間は必須です", groups = ValidGroup1.class)
    private LocalTime plannedWorkEndTime;
    private LocalTime plannedBreakStartTime;
   
    private LocalTime plannedBreakEndTime;
    
    @NotNull(message = "実績開始時間は必須です", groups = ValidGroup1.class)
    private LocalTime actualWorkStartTime;
    @NotNull(message = "実績終了時間は必須です", groups = ValidGroup1.class)
    private LocalTime actualWorkEndTime;
    
    private LocalTime actualBreakStartTime;
    
    private LocalTime actualBreakEndTime;
    
    private BigDecimal scheduledWorkHours;
    private BigDecimal actualWorkHours;
    private BigDecimal overtimeHours;
    private BigDecimal deductionTime;
    
    private String kintaiStatus;
    @Length(max = 20, groups = ValidGroup1.class)
    private String kintaiComment;
}
