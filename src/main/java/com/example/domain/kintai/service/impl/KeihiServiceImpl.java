package com.example.domain.kintai.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import com.example.domain.kintai.service.KeihiService;
import com.example.dto.KeihiDto;
import com.example.form.KeihiForm;
import com.example.repository.KeihiMapper;

@Service
public class KeihiServiceImpl implements KeihiService  {
	
    @Autowired
    private KeihiMapper keihiMapper;

    @Override
    @Transactional 
    public int insertKeihi(KeihiDto keihi) {
        return keihiMapper.insertKeihi(keihi);
    }

    @Override
    public List<KeihiDto> getListByUserId(String userId) {
        return keihiMapper. findByUserId(userId);

    }


    @Override
    public int update(KeihiDto keihi) {
        return keihiMapper. updateOne(keihi);
    }

    @Override
    public int delete(int keihiId) {
        return keihiMapper.deleteOne(keihiId);
    }

    // 追加：月指定の検索
    @Override
    public List<KeihiDto> findByUserIdAndYearMonth(String userId, String yearMonth) {
        return keihiMapper.findByUserIdAndYearMonth(userId, yearMonth);
    }
    @Override
    public void calculate(KeihiDto dto) {

        if (dto.getAmount() == null) {
            dto.setAmount(0);
        }

        // 交通手段で計算
        if ("電車(往復)".equals(dto.getMethod())) {
            dto.setAmount(dto.getAmount() * 2);
        }

    }
    
    @Override
    public Map<String, Object> save(KeihiForm form, String deletedIds, String yearMonth) {

        Map<String,Object> res = new HashMap<>();

        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 削除処理
        if (deletedIds != null && !deletedIds.isEmpty()) {
            for (String idStr : deletedIds.split(",")) {
                try {
                    keihiMapper.deleteOne(Integer.parseInt(idStr.trim()));
                } catch (Exception ignore) {}
            }
        }

        // 保存処理
        for (KeihiDto k : form.getKeihi()) {

            if (k.getDate() == null) continue;

            // ★ビジネスロジック
            calculate(k);

            k.setUserId(form.getUserId());

            if (k.getKeihiId() != null && k.getKeihiId() > 0) {
                keihiMapper.updateOne(k);
            } else {
                keihiMapper.insertKeihi(k);
            }
        }

        res.put("status", "success");
        res.put("keihi",
                keihiMapper.findByUserIdAndYearMonth(form.getUserId(), yearMonth));

        return res;
    }
    
    @Override
    public Map<String, Object> buildErrorResponse(BindingResult result) {

        Map<Integer, List<String>> errorMap = new LinkedHashMap<>();

        for (var e : result.getFieldErrors()) {

            String field = e.getField();
            String msg = e.getDefaultMessage();

            int row = -1;
            int s = field.indexOf("[");
            int t = field.indexOf("]");

            if (s != -1 && t != -1) {
                row = Integer.parseInt(field.substring(s + 1, t));
            }

            errorMap.computeIfAbsent(row, k -> new ArrayList<>()).add(msg);
        }

        List<String> errors = new ArrayList<>();

        errorMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {

                    StringBuilder sb = new StringBuilder();
                    sb.append((entry.getKey() + 1)).append("行目:\n");

                    for (String m : entry.getValue()) {
                        sb.append("・").append(m).append("\n");
                    }

                    errors.add(sb.toString());
                });

        Map<String,Object> res = new HashMap<>();
        res.put("status", "error");
        res.put("errors", errors);

        return res;
    }
    
}
