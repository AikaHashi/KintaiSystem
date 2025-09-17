package com.example.form;

import java.util.List;

import com.example.domain.kintai.model.Koutsuhi;

import lombok.Data;

@Data
public class KoutsuhiForm {
    private String userId;
    private List<Koutsuhi> koutsuhi;
}
