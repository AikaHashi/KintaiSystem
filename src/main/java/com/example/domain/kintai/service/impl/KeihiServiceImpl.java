package com.example.domain.kintai.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.kintai.model.Keihi;
import com.example.domain.kintai.service.KeihiService;
import com.example.repository.KeihiMapper;

@Service
public class KeihiServiceImpl implements KeihiService  {
	
    @Autowired
    private KeihiMapper keihiMapper;

    @Override
    @Transactional 
    public int insertKeihi(Keihi keihi) {
        return keihiMapper.insertKeihi(keihi);
    }

    @Override
    public List<Keihi> getListByUserId(String userId) {
        return keihiMapper. findByUserId(userId);

    }


    @Override
    public int update(Keihi keihi) {
        return keihiMapper. updateOne(keihi);
    }

    @Override
    public int delete(int keihiId) {
        return keihiMapper.deleteOne(keihiId);
    }

    // 追加：月指定の検索
    @Override
    public List<Keihi> findByUserIdAndYearMonth(String userId, String yearMonth) {
        return keihiMapper.findByUserIdAndYearMonth(userId, yearMonth);
    }
}
