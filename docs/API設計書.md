勤怠管理システム API設計（要約）
■概要

本システムは、ユーザーの勤怠・交通費・経費を一元管理し、
申請・承認フローを備えたWebアプリケーションである。

Spring Boot / Thymeleaf / MyBatis を用いて開発。

■主な機能
① 認証・ユーザー管理
ログイン（Spring Security）
ユーザー登録・更新（管理者）
② 勤怠管理
勤怠入力・更新（単体 / 一括）
勤怠一覧表示（月単位）
カレンダー連携（FullCalendar）
勤怠申請・承認・差戻
③ 交通費・経費管理
交通費・経費の登録 / 更新 / 削除
月単位での一覧取得
往復時の金額自動計算
申請・承認・差戻機能
■API構成（主要）
■勤怠
機能	URL	メソッド
勤怠保存（単体）	/kintai/api/save	POST
勤怠保存（一括）	/kintai/api/save-list/{userId}	POST
勤怠取得（日別）	/kintai/api/data	GET
カレンダーイベント	/kintai/events	GET
■申請・承認
機能	URL	メソッド
勤怠申請	/kintai/apply	POST
勤怠承認	/kintai/approve	POST
勤怠差戻	/kintai/reject	POST
ステータス取得	/kintai/status	GET
■交通費・経費
機能	URL	メソッド
交通費保存	/kintai/koutsuhi/saveKoutsuhi	POST
経費保存	/kintai/keihi/saveKeihi	POST
データ取得	/kintai/koutsuhiKeihiSafe/{userId}	GET
申請	/kintai/koutsuhi/apply/{userId}	POST
■設計ポイント
Spring Securityによる認証・認可
Service層でのビジネスロジック集約
DTO + ModelMapperによる責務分離
月単位取得でパフォーマンス最適化
FullCalendar連携を前提としたAPI設計
行単位バリデーションによるエラーハンドリング
申請ステータス管理（未申請 / 申請中 / 承認 / 差戻）
■業務ルール
同一ユーザー・同一日の勤怠は1件のみ
交通費「往復」は金額を2倍
未来月の申請は禁止
差戻時はコメント必須
■技術スタック
Java / Spring Boot
Spring Security
MyBatis
Thymeleaf
JavaScript（Ajax / FullCalendar）
H2 → PostgreSQL（予定）
■補足

詳細なAPI仕様・リクエスト/レスポンス例は以下参照
https://docs.google.com/document/d/1TUwLRC8PbbWrUBrW1eOuzPfHNFwZRG7SmKy3MV2ody0/edit?tab=t.0