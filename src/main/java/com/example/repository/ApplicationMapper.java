package com.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.domain.kintai.model.Application;

@Mapper
public interface ApplicationMapper {

    /** 既存互換：ステータス取得 */
    String getStatus(@Param("userId") String userId,
                     @Param("category") String category,
                     @Param("yearMonth") String yearMonth);

    /** 指定ユーザー・カテゴリ・年月の申請情報を取得 */
    Application findByUserIdAndCategoryAndYearMonth(@Param("userId") String userId,
                                                    @Param("category") String category,
                                                    @Param("yearMonth") String yearMonth);

    /** 新規登録 */
    void insert(Application application);

    /** 更新 */
    void update(Application application);
}
