INSERT INTO employee
(
   id,
   name,
   age
)
VALUES
(
   '1',
   'Tom',
   30
);

/* ユーザーマスタ */
INSERT INTO m_user(
user_id
,password
,user_name
,birthday
,age
,gender
,department_id
,role
) VALUES
('system@co.jp','$2a$10$sjfTsuZN7IiQodfxQ0NdFOlX6OYm21AGrhS0F1oPmpeuif6NxzF9y','システム管理者','2000-01-01',21,1,1,'ROLE_ADMIN')
,('user@co.jp','$2a$10$sjfTsuZN7IiQodfxQ0NdFOlX6OYm21AGrhS0F1oPmpeuif6NxzF9y','ユーザー１','2000-01-01',21,2,2,'ROLE_GENERAL');

/* 部署マスタ */
INSERT INTO m_department(
department_id
,department_name
)VALUES
(1,'システム管理部')
,(2,'営業部');

/* 給料テーブル */
/*INSERT INTO t_salary(
user_id
,year_month
,salary
)VALUES
('user@co.jp','2020/11',280000)
,('user@co.jp','2020/12',290000)
,('user@co.jp','2021/01',300000); */

/* 勤怠記録テーブル */
INSERT INTO t_kintai (
  user_id, work_date,
  updated_by,updated_at,
  planned_work_start_time, planned_work_end_time,
  planned_break_start_time, planned_break_end_time,
  actual_work_start_time, actual_work_end_time,
  actual_break_start_time, actual_break_end_time,
  scheduled_work_hours, actual_work_hours,
  overtime_hours, deduction_time,
  kintai_status, kintai_comment
) VALUES
('user@co.jp', '2025-07-01',
 'system@co.jp','2025-06-01',
 '09:00', '18:00', '12:00', '13:00',
 '09:05', '18:10', '12:05', '13:00',
 8.0, 8.0, 0.5, 0.0, 'nothing', '通常勤務'),
('user@co.jp', '2025-07-02',
 'system@co.jp','2025-06-01',
 '09:00', '18:00', '12:00', '13:00',
 '09:20', '18:00', '12:00', '13:00',
 8.0, 7.5, 0.0, 0.5, 'paid_leave_full', '体調不良');
 
 /* 交通費テーブル */
 INSERT INTO t_koutsuhi (
  user_id, date, method, departure, arrival, via, amount, note
) VALUES
('user@co.jp', '2025-07-16', '電車(片道)', '新宿', '渋谷', NULL, 200, '営業訪問'),
('user@co.jp', '2025-07-20', '電車(往復)', '池袋', '東京', '新橋', 600, NULL);
 
 /* 経費テーブル */
 INSERT INTO t_keihi (
  user_id, date, method, departure, arrival, via, amount, note
) VALUES
('user@co.jp', '2025-07-15', 'タクシー', '会社', '取引先A', NULL, 1500, '急ぎの訪問'),
('user@co.jp', '2025-07-14', 'バス', '自宅', '会社', NULL, 300, NULL);
