'use strict';

var userData = []; // ユーザーデータ（空配列で初期化）
var table = null; // DataTablesオブジェクト

/** 画面ロード時の処理. */
jQuery(function($) {

	// DataTablesの初期化
	createDataTables();

	/** 検索ボタンを押したときの処理. */
	$('#btn-search').click(function(event) {
		// 検索
		search();
	});
});

/** 検索処理. */
function search() {

	// formの値を取得
	var formData = $('#user-search-form').serialize();

	// ajax通信
	$.ajax({
		type: "GET",
		url: '/kintai/get/userList',
		data: formData,
		dataType: 'json',
		cache: false,
		timeout: 5000,
	}).done(function(data) {
		// ajax成功時の処理
		console.log(data);
		// JSONを変数に入れる
		userData = data || [];
		// DataTables作成
		createDataTables();

	}).fail(function(jqXHR, textStatus, errorThrown) {
		// ajax失敗時の処理
		alert('検索処理に失敗しました');

	}).always(function() {
		// 常に実行する処理(特になし)
	});
}

/** DataTables作成. */
function createDataTables() {

	//既にDataTablesが作成されている場合
	if (table !== null) {
		// DataTables破棄
		table.destroy();
	}

	// DataTables作成
	table = $('#user-list-table').DataTable({
		// 日本語化
		language: {
			url: '/webjars/datatables-plugins/i18n/Japanese.json'
		},
		//表示データ
		data: userData,
		//データと列のマッピング
		columns: [
			{ data: 'userId' }, // ユーザーID
			{ data: 'userName' }, // ユーザー名
			{
				data: 'formattedBirthday' // すでにフォーマット済みの文字列を使用
			},
			{ data: 'age' }, // 年齢
			{
				data: 'gender', // 性別
				render: function(data, type, row) {
					var gender = '';
					if (data === 1) {
						gender = '男性';
					} else {
						gender = '女性';
					}
					return gender;
				}
			},
			{
				data: 'userId',
				render: function(data, type, row) {
					var url = '<a href="/kintai/userDetail/' + encodeURIComponent(data) + '">詳細</a>';
					return url;
				}
			},
		]
	});
}