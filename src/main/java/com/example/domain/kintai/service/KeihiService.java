package com.example.domain.kintai.service;

import java.util.List;

import com.example.domain.kintai.model.Keihi;


public interface KeihiService {

    
    public int insertKeihi(Keihi keihi);

    public List<Keihi> getListByUserId(String userId);
    
    public int update(Keihi keihi);

    public int delete(int keihiId);
    
    public List<Keihi> findByUserIdAndYearMonth(String userId, String yearMonth);
}