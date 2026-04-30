package com.example.domain.kintai.service;

import java.util.List;
import java.util.Map;

import org.springframework.validation.BindingResult;

import com.example.domain.kintai.model.Koutsuhi;
import com.example.dto.KoutsuhiDto;
import com.example.form.KoutsuhiForm;


public interface KoutsuhiService {
	
    public int insertKoutsuhi(Koutsuhi koutsuhi);
    public List<KoutsuhiDto> getListByUserId(String userId);
    public  int update(Koutsuhi koutsuhi);
    public int delete(int ikoutsuhiId);
    public List<KoutsuhiDto> findByUserIdAndYearMonth(String userId, String yearMonth) ;
	public void calculate(KoutsuhiDto dto);
	
	Map<String,Object> save(KoutsuhiForm form, String deletedIds, String yearMonth);

	Map<String,Object> buildErrorResponse(BindingResult result);
	
}

