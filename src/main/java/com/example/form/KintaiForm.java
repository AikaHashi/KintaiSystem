package com.example.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KintaiForm {
    private String userId;
    private String userName;  
    
    private String updatedBy;
    private Timestamp updatedAt;
    
    private LocalDate workDate;
    @NotBlank(groups = ValidGroup1.class )
    private LocalTime plannedWorkStartTime;
    @NotBlank(groups = ValidGroup1.class )
    private LocalTime plannedWorkEndTime;

    private LocalTime plannedBreakStartTime;
   
    private LocalTime plannedBreakEndTime;
    
    @NotBlank(groups = ValidGroup1.class )
    private LocalTime actualWorkStartTime;
    @NotBlank(groups = ValidGroup1.class )
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
