package com.example.common;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class SessionAttributeAdvice {

    @ModelAttribute("sessionUserId")
    public String populateSessionUserId(HttpSession session) {
        return (String) session.getAttribute("userId");
    }

    @ModelAttribute("sessionUserName")
    public String populateSessionUserName(HttpSession session) {
        return (String) session.getAttribute("userName");
    }
}