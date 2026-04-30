package com.example.domain.kintai.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import com.example.domain.kintai.model.Koutsuhi;
import com.example.domain.kintai.service.KoutsuhiService;
import com.example.dto.KoutsuhiDto;
import com.example.form.KoutsuhiForm;
import com.example.repository.KoutsuhiMapper;

@Service
public class KoutsuhiServiceImpl implements KoutsuhiService{

    private static final Logger logger = LoggerFactory.getLogger(KoutsuhiServiceImpl.class);

    @Autowired
    KoutsuhiMapper koutsuhiMapper;
    
    @Autowired
    ModelMapper modelMapper;
     
    @Override
    public int insertKoutsuhi(Koutsuhi koutsuhi) {
        logger.info("insertKoutsuhi called: {}", koutsuhi);
        return koutsuhiMapper.insertKoutsuhi(koutsuhi);
    }

    @Override
    public List<KoutsuhiDto> getListByUserId(String userId) {
        logger.info("getListByUserId called: userId={}", userId);
        return koutsuhiMapper.findByUserId(userId);
    }

    @Override
    public int update(Koutsuhi koutsuhi) {
        logger.info("update called for koutsuhiId={} data: {}", koutsuhi.getKoutsuhiId(), koutsuhi);
        int count = koutsuhiMapper.updateOne(koutsuhi);
        logger.info("update result: updated rows={}", count);
        return count;
    }

    @Override
    public int delete(int koutsuhiId) {
        logger.info("delete called for koutsuhiId={}", koutsuhiId);
        return koutsuhiMapper.deleteOne(koutsuhiId);
    }

    @Override
    public List<KoutsuhiDto> findByUserIdAndYearMonth(String userId, String yearMonth) {
        logger.info("findByUserIdAndYearMonth called: userId={}, yearMonth={}", userId, yearMonth);
        return koutsuhiMapper.findByUserIdAndYearMonth(userId, yearMonth);
    }
    
    @Override
    public void calculate(KoutsuhiDto dto) {

        if (dto.getAmount() == null) {
            dto.setAmount(0);
        }

        if ("電車(往復)".equals(dto.getMethod())) {
            dto.setAmount(dto.getAmount() * 2);
        }
    }
    
    @Override
    public Map<String, Object> save(KoutsuhiForm form, String deletedIds, String yearMonth) {

        Map<String,Object> res = new HashMap<>();

        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 削除
        if (deletedIds != null && !deletedIds.isEmpty()) {
            for (String idStr : deletedIds.split(",")) {
                try {
                    koutsuhiMapper.deleteOne(Integer.parseInt(idStr.trim()));
                } catch (Exception ignore) {}
            }
        }

        // 保存
        for (KoutsuhiDto dto : form.getKoutsuhi()) {

            if (dto.getDate() == null) continue;

            // ★計算ロジックはService
            calculate(dto);

            Koutsuhi entity = modelMapper.map(dto, Koutsuhi.class);
            entity.setUserId(form.getUserId());

            if (entity.getKoutsuhiId() != null && entity.getKoutsuhiId() > 0) {
                koutsuhiMapper.updateOne(entity);
            } else {
                koutsuhiMapper.insertKoutsuhi(entity);
            }
        }

        res.put("status", "success");
        res.put("koutsuhi",
                koutsuhiMapper.findByUserIdAndYearMonth(form.getUserId(), yearMonth));

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
