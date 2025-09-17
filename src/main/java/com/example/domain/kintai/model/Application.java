package com.example.domain.kintai.model;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Application {
    private Integer applicationId;    // application_id
    private String userId;            // 申請者
    private String category;          // 'KINTAI', 'KOUTSUHI', 'KEIHI'
    private String yearMonth;         // '2025-08' など
    private String status;            // 'APPLYING','REJECTED','REAPPLYING','APPROVED'
    private String comment;           // 差戻コメントなど
    private Timestamp createdAt;      // 作成日時
    private Timestamp updatedAt;      // 更新日時
}

