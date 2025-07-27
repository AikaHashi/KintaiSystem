package com.example.aspect;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalControllAdvice {

/** データベース関連の例外処理  */
	@ExceptionHandler(DataAccessException.class)
public String dataAccessExceptionHandler(DataAccessException e, Model model) {
		
		// 空文字をセット
		model.addAttribute("error","");
		
		// メッセージをModelに登録
		model.addAttribute("message","DataAccessExceptionが発生しました");
		
		// HTTPのエラーコード（500）をModelに登録
		model.addAttribute("status",HttpStatus.INTERNAL_SERVER_ERROR);
		
		return "error";
	}
	
//	/** 403の例外 */
//	@ExceptionHandler(Exception.class)
//	public String handleAccessDeniedException(Exception e, Model model) {
//	    // 403 Forbidden エラーメッセージを設定
//	    model.addAttribute("error", "403 Forbidden");
//	    model.addAttribute("message", "アクセスが拒否されました");
//	    model.addAttribute("status", HttpStatus.FORBIDDEN); // 403 エラー
//	    model.addAttribute("returnUrl", "/login"); // ログインページへのリンク（必要に応じて）
//	    return "error"; 
//	}
	
	/** その他の例外処理*/
	@ExceptionHandler(Exception.class)
	public String exceptionHandler(Exception e, Model model) {
		
		// 空文字をセット
		model.addAttribute("error","");
		
		// メッセージをModelに登録
		model.addAttribute("message","Exceptionが発生しました");
		
		// HTTPのエラーコード（500）をModelに登録
		model.addAttribute("status",HttpStatus.INTERNAL_SERVER_ERROR);
		
		return "error";
	}
	
}
