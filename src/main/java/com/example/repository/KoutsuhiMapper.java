package com.example.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.domain.kintai.model.Koutsuhi;
import com.example.dto.KoutsuhiDto;

@Mapper
public interface KoutsuhiMapper {
    
    // 登録
    int insertKoutsuhi(Koutsuhi koutsuhi);

    // 一覧取得（ユーザー単位）
    List<KoutsuhiDto> findByUserId(@Param("userId") String userId);

    // 単体取得（ID指定）
    Koutsuhi findById(@Param("koutsuhiId") int koutsuhiId);

    // 更新
    int updateOne(Koutsuhi koutsuhi);

    // 削除
    int deleteOne(@Param("koutsuhiId") int koutsuhiId);
    
    List<KoutsuhiDto> findByUserIdAndYearMonth(String userId, String yearMonth);
}
