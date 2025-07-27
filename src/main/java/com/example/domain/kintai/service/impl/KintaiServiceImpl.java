package com.example.domain.kintai.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.domain.kintai.model.Kintai;
import com.example.domain.kintai.service.KintaiService;
import com.example.repository.KintaiMapper;

@Service
public class KintaiServiceImpl implements KintaiService {

    @Autowired
    private KintaiMapper kintaiMapper;

    @Override
    @Transactional
    public int insert(Kintai kintai) {
        return kintaiMapper.insertKintai(kintai);
    }

    @Override
    public List<Kintai> getListByUserId(String userId) {
        return kintaiMapper.findKintaiByUserId(userId);
    }

    @Override
    public Kintai getOne(String userId, LocalDate workDate) {
        return kintaiMapper.findKintaiByUserAndDate(userId, workDate);
    }

    @Override
    public int update(Kintai kintai) {
        return kintaiMapper.updateKintai(kintai);
    }

    @Override
    public int delete(String userId, LocalDate workDate) {
        return kintaiMapper.deleteKintai(userId, workDate);
    }

    @Override
    public Kintai selectOneByUserIdAndDate(String userId, LocalDate workDate) {
        return kintaiMapper.selectOneByUserIdAndDate(userId, workDate);
    }
}