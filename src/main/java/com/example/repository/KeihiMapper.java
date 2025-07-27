package com.example.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.domain.kintai.model.Keihi;

@Mapper
public interface KeihiMapper {
    
    // 登録
    int insertKeihi(Keihi keihi);

    // 一覧取得（ユーザー単位）
    List<Keihi> findByUserId(@Param("userId") String userId);

    // 単体取得（ID指定）
    Keihi findById(@Param("keihiId") int keihiId);

    // 更新
    int updateOne(Keihi keihi);

    // 削除
    int deleteOne(@Param("keihiId") int keihiId);
    
    public List<Keihi> findByUserIdAndYearMonth(String userId, String yearMonth);
    
}