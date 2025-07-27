package com.example.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class KintaiDto {
    private String userId; // 社員の識別用ID（または employeeName を併用するなら追加）

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
