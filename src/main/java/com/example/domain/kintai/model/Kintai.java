package com.example.domain.kintai.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class Kintai {
    private String userId;
    private LocalDate workDate;
    private String userName; 
    
    private String updatedBy;
    private Timestamp updatedAt;
    
    private LocalTime plannedWorkStartTime;
    private LocalTime plannedWorkEndTime;
    private LocalTime plannedBreakStartTime;
    private LocalTime plannedBreakEndTime;
    
    private LocalTime actualWorkStartTime;
    private LocalTime actualWorkEndTime;
    private LocalTime actualBreakStartTime;
    private LocalTime actualBreakEndTime;
    
    private BigDecimal scheduledWorkHours;
    private BigDecimal actualWorkHours;
    private BigDecimal overtimeHours;
    private BigDecimal deductionTime;
    
    private String kintaiStatus;
    private String kintaiComment;
}
