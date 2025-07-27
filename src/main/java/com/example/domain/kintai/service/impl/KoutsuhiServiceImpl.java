package com.example.domain.kintai.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.domain.kintai.model.Koutsuhi;
import com.example.domain.kintai.service.KoutsuhiService;
import com.example.repository.KoutsuhiMapper;

@Service
public class KoutsuhiServiceImpl implements KoutsuhiService{

    private static final Logger logger = LoggerFactory.getLogger(KoutsuhiServiceImpl.class);

    @Autowired
    KoutsuhiMapper koutsuhiMapper;
    
    @Override
    public int insertKoutsuhi(Koutsuhi koutsuhi) {
        logger.info("insertKoutsuhi called: {}", koutsuhi);
        return koutsuhiMapper.insertKoutsuhi(koutsuhi);
    }

    @Override
    public List<Koutsuhi> getListByUserId(String userId) {
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
    public List<Koutsuhi> findByUserIdAndYearMonth(String userId, String yearMonth) {
        logger.info("findByUserIdAndYearMonth called: userId={}, yearMonth={}", userId, yearMonth);
        return koutsuhiMapper.findByUserIdAndYearMonth(userId, yearMonth);
    }
}
