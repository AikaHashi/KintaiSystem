package com.example.domain.kintai.model;

import java.time.LocalDate;

import lombok.Data;

@Data
public class DayInfo {
	private LocalDate date;       // 日付（例: "2025-05-01"）
	private int year;
	private int month;
	private int day;
    private String dayOfWeek;  // 曜日（例: "木"）
//    private boolean isHoliday; //祝日
//    private boolean isWeekend; //休日
}
