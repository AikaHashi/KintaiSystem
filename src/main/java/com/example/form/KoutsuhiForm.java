package com.example.form;

import java.util.List;

import com.example.dto.KoutsuhiDto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class KoutsuhiForm {
    private String userId;
    @Valid
    private List<KoutsuhiDto> koutsuhi;
}
