package com.example.form;

import java.util.List;

import com.example.domain.kintai.model.Koutsuhi;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class KoutsuhiForm {
    private String userId;
    @Valid
    private List<Koutsuhi> koutsuhi;
}
