package com.example.domain.kintai.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.domain.kintai.model.Application;
import com.example.repository.ApplicationMapper;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationMapper applicationMapper;

    public String getStatus(String userId, String category, String yearMonth) {
        return applicationMapper.getStatus(userId, category, yearMonth);
    }

    /** 指定ユーザー・カテゴリ・年月の申請情報を取得 */
    public Application findByUserIdAndCategoryAndYearMonth(String userId, String category, String yearMonth) {
        return applicationMapper.findByUserIdAndCategoryAndYearMonth(userId, category, yearMonth);
    }

    /** 保存または更新 */
    public void saveOrUpdate(Application app) {
        if (app.getApplicationId() != null) { // IDがある場合は更新
            applicationMapper.update(app);
        } else { // 新規の場合は挿入
            applicationMapper.insert(app);
        }
    }
}