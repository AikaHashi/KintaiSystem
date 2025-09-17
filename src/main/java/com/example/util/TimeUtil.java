package com.example.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /** HH:mm 形式の文字列から分数を計算（翌日跨ぎ対応） */
    public static int calculateMinutes(String startStr, String endStr) {
        if (startStr == null || endStr == null || startStr.isEmpty() || endStr.isEmpty()) {
            return 0;
        }

        try {
            LocalTime start = LocalTime.parse(startStr, TIME_FORMATTER);
            LocalTime end = LocalTime.parse(endStr, TIME_FORMATTER);
            Duration duration = Duration.between(start, end);
            if (duration.isNegative()) {
                duration = duration.plusHours(24); // 翌日跨ぎ対応
            }
            return (int) duration.toMinutes();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /** HH:mm形式 または 分数文字列を BigDecimal 時間に変換（安全版） */
    public static BigDecimal convertTimeStringToBigDecimal(String time) {
        if (time == null || time.isEmpty() || "0".equals(time)) {
            return BigDecimal.ZERO; // 空文字や "0" は0時間とみなす
        }

        try {
            // 分数形式（例: "600"）かを判定
            if (!time.contains(":")) {
                int minutes = Integer.parseInt(time);
                BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
                return hours;
            }

            // HH:mm形式
            String[] parts = time.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("時間形式が不正です: " + time);
            }
            BigDecimal hours = new BigDecimal(parts[0]);
            BigDecimal minutes = new BigDecimal(parts[1]);
            return hours.add(minutes.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("時間に数字以外が含まれています: " + time, e);
        }
    }

    /**
     * 控除時間計算（所定 − 実働、負なら0、全休・代休は0）
     * @param isHoliday 全休・代休かどうか
     * @param scheduledHours 所定時間（BigDecimalで渡す）
     * @param actualHours 実働時間（BigDecimalで渡す）
     * @return 控除時間 BigDecimal
     */
    public static BigDecimal calculateDeduction(boolean isHoliday, BigDecimal scheduledHours, BigDecimal actualHours) {
        if (isHoliday) {
            return BigDecimal.ZERO;
        }
        BigDecimal deduction = scheduledHours.subtract(actualHours);
        return deduction.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : deduction;
    }

    /**
     * 分数文字列（例: "600"）を "HH:mm" 形式に変換
     * @param minutesStr 分数の文字列
     * @return HH:mm 形式の文字列
     */
    public static String convertMinutesToHHmm(String minutesStr) {
        if (minutesStr == null || minutesStr.trim().isEmpty() || "0".equals(minutesStr)) {
            return "0:00";
        }
        try {
            int minutes = Integer.parseInt(minutesStr);
            int hours = minutes / 60;
            int remainMinutes = minutes % 60;
            return String.format("%d:%02d", hours, remainMinutes);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分数変換に失敗しました: " + minutesStr, e);
        }
    }
}
