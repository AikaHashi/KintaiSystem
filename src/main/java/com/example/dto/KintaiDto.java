package com.example.dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.example.util.TimeUtil;

import lombok.Data;

@Data
public class KintaiDto {
    private String userId;
    private LocalDate workDate;
    private String userName; 
    private String updatedBy;
    private Timestamp updatedAt;

    // ===============================
    // フロントからの時刻文字列
    // ===============================
    private String plannedWorkStartTimeStr;
    private String plannedWorkEndTimeStr;
    private String plannedBreakStartTimeStr;
    private String plannedBreakEndTimeStr;

    private String actualWorkStartTimeStr;
    private String actualWorkEndTimeStr;
    private String actualBreakStartTimeStr;
    private String actualBreakEndTimeStr;

    // ===============================
    // 計算結果（String表示用）
    // ===============================
    private String scheduledWorkHours;
    private String actualWorkHours;
    private String overtimeHours;
    private String deductionTime;

    // ===============================
    // 計算結果（BigDecimal計算用）
    // ===============================
    private BigDecimal scheduledWorkHoursDecimal;
    private BigDecimal actualWorkHoursDecimal;
    private BigDecimal overtimeHoursDecimal;
    private BigDecimal deductionTimeDecimal;

    private String kintaiStatus;
    private String kintaiComment;

    // ===============================
    // LocalTime フィールド（必要に応じて変換用）
    // ===============================
    private LocalTime plannedWorkStartTime;
    private LocalTime plannedWorkEndTime;
    private LocalTime plannedBreakStartTime;
    private LocalTime plannedBreakEndTime;

    private LocalTime actualWorkStartTime;
    private LocalTime actualWorkEndTime;
    private LocalTime actualBreakStartTime;
    private LocalTime actualBreakEndTime;

    // ===============================
    // 時刻文字列 → LocalTime 変換
    // ===============================
    public static LocalTime timeStrToLocalTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return LocalTime.parse(timeStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    // ===============================
    // 全休・代休判定
    // ===============================
    public boolean isHolidayOrSubstitute() {
        return "ALL_HOLIDAY".equals(kintaiStatus) || "COMP_OFF".equals(kintaiStatus);
    }

    // ===============================
    // DTO 内でまとめて変換・計算するメソッド
    // ===============================
    public void convertStringsToTimeAndDecimal() {
        // LocalTime 変換
        plannedWorkStartTime = timeStrToLocalTime(plannedWorkStartTimeStr);
        plannedWorkEndTime   = timeStrToLocalTime(plannedWorkEndTimeStr);
        plannedBreakStartTime = timeStrToLocalTime(plannedBreakStartTimeStr);
        plannedBreakEndTime   = timeStrToLocalTime(plannedBreakEndTimeStr);

        actualWorkStartTime = timeStrToLocalTime(actualWorkStartTimeStr);
        actualWorkEndTime   = timeStrToLocalTime(actualWorkEndTimeStr);
        actualBreakStartTime = timeStrToLocalTime(actualBreakStartTimeStr);
        actualBreakEndTime   = timeStrToLocalTime(actualBreakEndTimeStr);

        // ===============================
        // 所定勤務時間計算
        // ===============================
        scheduledWorkHoursDecimal = BigDecimal.ZERO;
        scheduledWorkHours = "0.00";
        if (plannedWorkStartTime != null && plannedWorkEndTime != null) {
            long workMinutes = calculateMinutes(plannedWorkStartTime, plannedWorkEndTime);
            long breakMinutes = (plannedBreakStartTime != null && plannedBreakEndTime != null)
                    ? calculateMinutes(plannedBreakStartTime, plannedBreakEndTime)
                    : 0;
            BigDecimal hours = BigDecimal.valueOf(workMinutes - breakMinutes).divide(BigDecimal.valueOf(60.0));
            scheduledWorkHoursDecimal = hours;
            scheduledWorkHours = hours.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }

        // ===============================
        // 実績勤務時間計算
        // ===============================
        actualWorkHoursDecimal = BigDecimal.ZERO;
        actualWorkHours = "0.00";
        if (actualWorkStartTime != null && actualWorkEndTime != null) {
            long workMinutes = calculateMinutes(actualWorkStartTime, actualWorkEndTime);
            long breakMinutes = (actualBreakStartTime != null && actualBreakEndTime != null)
                    ? calculateMinutes(actualBreakStartTime, actualBreakEndTime)
                    : 0;
            BigDecimal hours = BigDecimal.valueOf(workMinutes - breakMinutes).divide(BigDecimal.valueOf(60.0));
            actualWorkHoursDecimal = hours;
            actualWorkHours = hours.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }

        // ===============================
        // 控除時間計算（所定 − 実働、負なら0、全休・代休は0）
        // ===============================
        deductionTimeDecimal = TimeUtil.calculateDeduction(isHolidayOrSubstitute(), scheduledWorkHoursDecimal, actualWorkHoursDecimal);
        deductionTime = deductionTimeDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    // ===============================
    // LocalTime 2つから分数計算（翌日跨ぎ対応）
    // ===============================
    private long calculateMinutes(LocalTime start, LocalTime end) {
        Duration duration = Duration.between(start, end);
        if (duration.isNegative()) {
            duration = duration.plusHours(24);
        }
        return duration.toMinutes();
    }

    // ===============================
    // LocalTime → String 変換
    // ===============================
    public void syncLocalTimeToString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (plannedWorkStartTime != null) {
            plannedWorkStartTimeStr = plannedWorkStartTime.format(formatter);
        }
        if (plannedWorkEndTime != null) {
            plannedWorkEndTimeStr = plannedWorkEndTime.format(formatter);
        }
        if (plannedBreakStartTime != null) {
            plannedBreakStartTimeStr = plannedBreakStartTime.format(formatter);
        }
        if (plannedBreakEndTime != null) {
            plannedBreakEndTimeStr = plannedBreakEndTime.format(formatter);
        }
        if (actualWorkStartTime != null) {
            actualWorkStartTimeStr = actualWorkStartTime.format(formatter);
        }
        if (actualWorkEndTime != null) {
            actualWorkEndTimeStr = actualWorkEndTime.format(formatter);
        }
        if (actualBreakStartTime != null) {
            actualBreakStartTimeStr = actualBreakStartTime.format(formatter);
        }
        if (actualBreakEndTime != null) {
            actualBreakEndTimeStr = actualBreakEndTime.format(formatter);
        }
    }
}
