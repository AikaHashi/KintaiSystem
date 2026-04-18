package com.example.repository;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.domain.kintai.model.Kintai;

@Mapper
public interface KintaiMapper {

	int insertKintai(Kintai kintai);

	List<Kintai> findKintaiByUserId(String userId);

	Kintai findKintaiByUserAndDate(@Param("userId") String userId, @Param("workDate") LocalDate workDate);

	int updateKintai(Kintai kintai);

	int deleteKintai(@Param("userId") String userId, @Param("workDate") LocalDate workDate);

	/** 追加 */
	default Kintai selectOneByUserIdAndDate(String userId, LocalDate workDate) {
		return findKintaiByUserAndDate(userId, workDate);
	}

	void update(Kintai kintai);

	List<Kintai> findByUserAndMonth(
			@Param("userId") String userId,
			@Param("year") int year,
			@Param("month") int month);
}
