package com.example.form;

import java.time.LocalDate;
import java.util.List;

import com.example.domain.kintai.model.Keihi;

import lombok.Data;

@Data
public class KeihiForm {
	private int keihi_id;
	private String userId;
	private LocalDate date;
	private String departure;
	private String arrival;
	private String via;
	private Integer amount;
	private String note;

	private List<Keihi> keihi;  
	
}

