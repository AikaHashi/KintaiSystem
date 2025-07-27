package com.example.domain.kintai.service;

import java.util.List;

import com.example.domain.kintai.model.Koutsuhi;


public interface KoutsuhiService {
	
    public int insertKoutsuhi(Koutsuhi koutsuhi);
    public List<Koutsuhi> getListByUserId(String userId);
    public  int update(Koutsuhi koutsuhi);
    public int delete(int ikoutsuhiId);
    public List<Koutsuhi> findByUserIdAndYearMonth(String userId, String yearMonth) ;
}

