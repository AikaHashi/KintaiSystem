package com.example.domain.kintai.service;

import java.time.LocalDate;
import java.util.List;

import com.example.domain.kintai.model.Kintai;


public interface KintaiService {

    int insert(Kintai kintai);

    List<Kintai> getListByUserId(String userId);

    Kintai getOne(String userId, LocalDate workDate);

    int update(Kintai kintai);

    int delete(String userId, LocalDate workDate);

    Kintai selectOneByUserIdAndDate(String userId, LocalDate workDate);
}