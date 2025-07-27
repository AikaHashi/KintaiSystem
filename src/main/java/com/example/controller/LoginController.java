package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String getLogin() {
        return "login/login"; // ログイン画面を表示
    }

    @PostMapping("/login")
    public String postLogin(String userId, HttpSession session) {
        MUser user = userService.getUserOne(userId);
        if (user != null) {
            session.setAttribute("sessionUserId", user.getUserId());
            session.setAttribute("sessionUserName", user.getUserName());
            return "redirect:/kintai/kintai"; // ログイン成功
        }
        return "login/login"; // ログイン失敗は再表示
    }
}
