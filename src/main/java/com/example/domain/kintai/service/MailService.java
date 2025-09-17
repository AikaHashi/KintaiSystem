package com.example.domain.kintai.service;

public interface MailService {
    /**
     * 指定した宛先にメールを送信
     * @param to      宛先メールアドレス
     * @param subject 件名
     * @param text    本文
     */
    void send(String to, String subject, String text);
}
