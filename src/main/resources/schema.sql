CREATE TABLE IF NOT EXISTS employee
(
   id VARCHAR (50) PRIMARY KEY,
   name VARCHAR (50),
   age INT
);

/* ユーザーマスタ */
CREATE TABLE IF NOT EXISTS m_user(
 user_id VARCHAR(50) PRIMARY KEY
 ,password VARCHAR(100)
 ,user_name VARCHAR(50)
 ,birthday DATE
 ,age INT
 ,gender INT
 ,department_id INT
 ,role VARCHAR(50)
);

/* 部署マスタ */
CREATE TABLE IF NOT EXISTS m_department(
department_id INT PRIMARY KEY
 ,department_name VARCHAR(50)
);

/* 給料テーブル  */
/*CREATE TABLE IF NOT EXISTS t_salary(
 user_id VARCHAR(50) 
 ,year_month VARCHAR(50)
 ,salary INT
 ,PRIMARY KEY(user_id,year_month)
); */

/* 勤務区分マスタ */
/*CREATE TABLE IF NOT EXISTS m_kintai(
); */


/* 勤怠記録テーブル */
CREATE TABLE IF NOT EXISTS t_kintai(
user_id VARCHAR(50) 
,work_date DATE
,updated_by VARCHAR(50)
,updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
,planned_work_start_time TIME
,planned_work_end_time TIME
,planned_break_start_time TIME
,planned_break_end_time TIME
,actual_work_start_time TIME
,actual_work_end_time TIME
,actual_break_start_time TIME
,actual_break_end_time TIME
,scheduled_work_hours DECIMAL
,actual_work_hours DECIMAL
,overtime_hours DECIMAL
,deduction_time DECIMAL
,kintai_status VARCHAR(50)
,kintai_comment VARCHAR(50)
,PRIMARY KEY (user_id, work_date)
);


/* 勤怠状態マスタテーブル */
/*CREATE TABLE IF NOT EXISTS m_kintai_status (
  kintai_status_id VARCHAR(50) PRIMARY KEY,
  kintai_status_name VARCHAR(100)
);*/


/* 交通費テーブル  */
CREATE TABLE IF NOT EXISTS t_koutsuhi (
  koutsuhi_id INT AUTO_INCREMENT PRIMARY KEY,            -- 自動連番
  user_id VARCHAR(50),                       -- 誰の申請か
  date DATE NOT NULL,                        -- 日付
  method VARCHAR(50) NOT NULL,               -- 手段（ex: 電車(片道)）
  departure TEXT NOT NULL,                   -- 出発地
  arrival TEXT NOT NULL,                     -- 到着地
  via TEXT,                                  -- 経由地（任意）
  amount INT NOT NULL,                       -- 金額
  note TEXT,                                 -- 特記事項（任意）
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



/*  経費テーブル */
CREATE TABLE IF NOT EXISTS t_keihi (
  keihi_id INT AUTO_INCREMENT PRIMARY KEY,               -- 自動連番
  user_id VARCHAR(50),                       -- 誰の申請か
  date DATE NOT NULL,                        -- 日付
  method VARCHAR(50) NOT NULL,               -- 手段（ex: タクシー）
  departure TEXT NOT NULL,                   -- 出発地
  arrival TEXT NOT NULL,                     -- 到着地
  via TEXT,                                  -- 経由地（任意）
  amount INT NOT NULL,                       -- 金額
  note TEXT,                                 -- 特記事項（任意）
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);