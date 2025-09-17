package com.example.dto;

import java.util.List;

import com.example.domain.kintai.model.Keihi;
import com.example.domain.kintai.model.Koutsuhi;

import lombok.Data;

@Data
public class KoutsuhiKeihiResponse {
    private List<Koutsuhi> koutsuhi;
    private List<Keihi> keihi;
    private String status;
}