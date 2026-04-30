package com.example.domain.kintai.service;

import java.util.List;
import java.util.Map;

import org.springframework.validation.BindingResult;

import com.example.dto.KeihiDto;
import com.example.form.KeihiForm;


public interface KeihiService {

    
    public int insertKeihi(KeihiDto k);

    public List<KeihiDto> getListByUserId(String userId);
    
    public int update(KeihiDto k);

    public int delete(int keihiId);
    
    public List<KeihiDto> findByUserIdAndYearMonth(String userId, String yearMonth);

	public void calculate(KeihiDto k);

	Map<String,Object> save(KeihiForm form, String deletedIds, String yearMonth);

	Map<String,Object> buildErrorResponse(BindingResult result);
	
}