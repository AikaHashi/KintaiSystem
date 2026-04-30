'use strict';

/** 画面ロード時の処理 */
jQuery(function($){

  /** 削除ボタン押下 */
  $('#btn-delete').on('click', function (event) {

    // 確認ダイアログ
    if (!confirm('本当に削除しますか？')) {
      event.preventDefault(); // キャンセル時は送信しない
    }
  });

});