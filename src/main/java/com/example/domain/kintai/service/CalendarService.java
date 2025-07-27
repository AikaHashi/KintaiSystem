package com.example.domain.kintai.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.domain.kintai.model.DayInfo;

@Service
public class CalendarService {
	

	 public List<DayInfo> getCalendarDays(int year, int month) {
	        YearMonth yearMonth = YearMonth.of(year, month);
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E", Locale.JAPANESE);
	        List<DayInfo> calendar = new ArrayList<>();

	        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
	            LocalDate date = yearMonth.atDay(day);
	            String dayOfWeek = date.format(formatter);

	            DayInfo dayInfo = new DayInfo();
	           dayInfo.setDate(date);
	           dayInfo.setYear(date.getYear());
	           dayInfo.setMonth(date.getMonthValue());
	           dayInfo.setDay(date.getDayOfMonth());
	           dayInfo.setDayOfWeek(dayOfWeek);
	           calendar.add(dayInfo);
	        }
	        
	        
//	    	//日付取得
//			
//			//今日の日付
//					LocalDate date = LocalDate.now();
//					System.out.println("今日の日付: " + date);
//			
//					
//					//月
//			LocalDate nextMonth = date.plusMonths(1);
//			LocalDate prevMonth = date.minusMonths(1);
//			
//			
//			//年
//			int year =date.getYear();
//			System.out.println("曜日: " + date);
//			
//			//月
//			int current = date.getMonthValue();
//			int next = nextMonth.getMonthValue();
//			int pre = prevMonth.getMonthValue();
//
//			System.out.println("今月: " + current);
//			System.out.println("翌月: " + next);
//			System.out.println("前月: " + pre);
//			
//			
//			//日
//			int day = date.getDayOfMonth(); 
//			
//			//曜日
//			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E", Locale.JAPANESE);
//			String dayOfWeek = date.format(formatter);
//			System.out.println("曜日: " +dayOfWeek); 

	        return calendar;
	    }
	
}
