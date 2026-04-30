package com.example.form;

import java.util.List;

import com.example.dto.KeihiDto;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class KeihiForm {

    private String userId;

    @Valid
    private List<KeihiDto> keihi;

		
	}

	


