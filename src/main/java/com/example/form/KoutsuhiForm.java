package com.example.form;

import java.util.List;

import com.example.domain.kintai.model.Koutsuhi;

import lombok.Data;

//	@Data
//	public class KoutsuhiForm {
//		private String koutsuhi_id;
//		private String userId;
//		private LocalDate date;
//		private String departure;
//		private String arrival;
//		private String via;
//		private Integer amount;
//		private String note;
//
//		private List<Koutsuhi> koutsuhiList;  
//	}
@Data
public class KoutsuhiForm {
    private String userId;
    private List<Koutsuhi> koutsuhiList;
}
