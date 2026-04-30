package com.example.domain.kintai.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.kintai.model.Kintai;
import com.example.domain.kintai.service.KintaiService;
import com.example.repository.KintaiMapper;

@Service
public class KintaiServiceImpl implements KintaiService {

    @Autowired
    private KintaiMapper kintaiMapper;

    @Override
    @Transactional
    public int insert(Kintai kintai) {
        return kintaiMapper.insertKintai(kintai);
    }

    @Override
    public List<Kintai> getListByUserId(String userId) {
        return kintaiMapper.findKintaiByUserId(userId);
    }

    @Override
    public Kintai getOne(String userId, LocalDate workDate) {
        return kintaiMapper.findKintaiByUserAndDate(userId, workDate);
    }

    @Override
    public int update(Kintai kintai) {
        return kintaiMapper.updateKintai(kintai);
    }

    @Override
    public int delete(String userId, LocalDate workDate) {
        return kintaiMapper.deleteKintai(userId, workDate);
    }

    @Override
    public Kintai selectOneByUserIdAndDate(String userId, LocalDate workDate) {
        return kintaiMapper.selectOneByUserIdAndDate(userId, workDate);
    }

    @Override
    public List<Kintai> findByUserAndMonth(String userId, int year, int month) {
        return kintaiMapper.findByUserAndMonth(userId, year, month);
    }

    // ===============================
    // 計算
    // ===============================

    /** 勤務時間（休憩差し引き前） */
    private BigDecimal calculateWorkHours(Kintai kintai) {
        if (kintai.getActualWorkStartTime() == null || kintai.getActualWorkEndTime() == null) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(
                kintai.getActualWorkStartTime(),
                kintai.getActualWorkEndTime()
        ).toMinutes();

        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    /** 休憩時間 */
    private BigDecimal calculateBreakHours(Kintai kintai) {
        if (kintai.getActualBreakStartTime() == null || kintai.getActualBreakEndTime() == null) {
            return BigDecimal.ZERO;
        }

        long minutes = Duration.between(
                kintai.getActualBreakStartTime(),
                kintai.getActualBreakEndTime()
        ).toMinutes();

        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
    
    /** 実働時間（勤務 - 休憩） */
//    public BigDecimal calculateActualWorkHours(KintaiDto dto) {
//        BigDecimal work = calculateWorkHours(dto);
//        BigDecimal breakTime = calculateBreakHours(dto);
//        return work.subtract(breakTime);
//    }
//    
   @Override
    public void calculate(Kintai kintai) {

        // 勤務時間（休憩差し引き前）
        BigDecimal work = calculateWorkHours(kintai);

        // 休憩時間
        BigDecimal breakTime = calculateBreakHours(kintai);

        // 実働時間
        BigDecimal actual = work.subtract(breakTime);

        // ★ Entityの項目に合わせる
        kintai.setScheduledWorkHours(work);
        kintai.setActualWorkHours(actual);

        // 必要なら控除時間として保持
        kintai.setDeductionTime(breakTime);
    }
    
}