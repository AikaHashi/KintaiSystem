package com.example.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.domain.kintai.model.Kintai;

@Mapper
public interface KintaiMapper {

    /** 勤怠情報の登録 */
    public int insertKintai(Kintai kintai);

    /** ユーザーごとの勤怠情報を一覧取得 */
    public List<Kintai> findKintaiByUserId(String userId);

    /** 勤怠1件取得（日付指定） */
    public Kintai findKintaiByUserAndDate(@Param("userId") String userId, @Param("workDate") LocalDate workDate);

    /** 勤怠の更新 */
    public int updateKintai(Kintai kintai);

    /** 勤怠削除（任意） */
    public int deleteKintai(@Param("userId") String userId, @Param("workDate") LocalDate workDate);
    
    Kintai selectOneByUserIdAndDate(@Param("userId") String userId, @Param("workDate") LocalDate workDate);
    void update(Kintai kintai);
}
