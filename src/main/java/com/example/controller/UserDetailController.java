package com.example.controller;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.UserService;
import com.example.form.UserDetailForm;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/kintai")
@Slf4j
public class UserDetailController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    /** ユーザー詳細画面表示 */
    @GetMapping("/userDetail/{userId:.+}")
    public String getUser(UserDetailForm form, Model model,
                          @PathVariable("userId") String userId) {

        // ユーザー取得
        MUser user = userService.getUserOne(userId);

        if (user != null) {
            modelMapper.map(user, form);
            model.addAttribute("targetUserName", user.getUserName());
        } else {
            model.addAttribute("targetUserName", "不明なユーザー");
        }

        model.addAttribute("userDetailForm", form);

        return "kintai/userDetail";
    }

    /** ユーザー更新処理（バリデーション対応・画面遷移） */
    @PostMapping(value = "/userDetail", params = "update")
    public String updateUser(
            @Valid @ModelAttribute UserDetailForm form,
            BindingResult bindingResult,
            Model model) {

        // バリデーションエラー
        if (bindingResult.hasErrors()) {
            model.addAttribute("targetUserName", form.getUserName());
            return "kintai/userDetail";
        }

        try {
            userService.updateUserOne(
                    form.getUserId(),
                    form.getPassword(),
                    form.getUserName()
            );
        } catch (Exception e) {
            log.error("ユーザー更新でエラー", e);
            model.addAttribute("targetUserName", form.getUserName());
            model.addAttribute("errorMessage", "更新に失敗しました");
            return "kintai/userDetail";
        }

        // 一覧へリダイレクト
        return "redirect:/kintai/userList";
    }

    /** ユーザー削除処理 */
    @PostMapping(value = "/userDetail", params = "delete")
    public String deleteUser(UserDetailForm form) {

        userService.deleteUserOne(form.getUserId());

        return "redirect:/kintai/userList";
    }
}