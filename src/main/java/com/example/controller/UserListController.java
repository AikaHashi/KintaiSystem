package com.example.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.UserService;
import com.example.form.UserListForm;

@Controller
@RequestMapping("/kintai")
public class UserListController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;
    
    /** ユーザー一覧画面を表示 */
    @GetMapping("/userList")
    public String getUserList(@ModelAttribute UserListForm form, Model model) {

        // ログインユーザー名を先に常に設定する
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser loginUser = userService.getUserOne(currentUserName);
        if (loginUser != null) {
            model.addAttribute("sessionUserName", loginUser.getUserName());
        } else {
            model.addAttribute("sessionUserName", "ゲスト"); // 念のためnull回避
        }

        // formをMUserクラスに変換
        MUser user = modelMapper.map(form, MUser.class);

        // ユーザー一覧取得
        List<MUser> userList = userService.getUsers(user);

        // 誕生日をフォーマットして新しいリストに追加
        List<MUser> formattedUserList = userList.stream()
            .peek(u -> {
                if (u.getBirthday() != null) {
                    u.setFormattedBirthday(u.getBirthday().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                } else {
                    u.setFormattedBirthday("未登録");
                }
            })
            .collect(Collectors.toList());

        // Modelに登録
        model.addAttribute("userList", formattedUserList);

        // 単一ユーザー情報の取得（例として）
        String userId = form.getUserId(); // UserListFormにuserIdがある想定
        if (userId != null) {
            MUser singleUser = userService.getUserOne(userId);
            if (singleUser != null) {
                LocalDate birthday = singleUser.getBirthday();
                String birthdayStr = (birthday != null) ? birthday.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) : "未登録";
                model.addAttribute("birthday", birthdayStr);
            } else {
                model.addAttribute("birthday", "未登録");
            }
        }

        return "kintai/userList";
    }

    @GetMapping("/get/userList")
    @ResponseBody
    public List<MUser> getUserListApi(@ModelAttribute UserListForm form) {

        MUser user = modelMapper.map(form, MUser.class);
        List<MUser> userList = userService.getUsers(user);

        // 誕生日をフォーマットして新しいリストに追加
        List<MUser> formattedUserList = userList.stream()
            .peek(u -> {
                if (u.getBirthday() != null) {
                    u.setFormattedBirthday(u.getBirthday().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                } else {
                    u.setFormattedBirthday("未登録");
                }
            })
            .collect(Collectors.toList());

        return formattedUserList;
    }

    /** ユーザー検索処理 */
    @PostMapping("/userList")
    public String postUserList(@ModelAttribute UserListForm form, Model model) {

        // formをMUserクラスに変換
        MUser user = modelMapper.map(form, MUser.class);

        // ユーザー検索
        List<MUser> userList = userService.getUsers(user);

        // 誕生日をフォーマットして新しいリストに追加
        List<MUser> formattedUserList = userList.stream()
            .peek(u -> {
                if (u.getBirthday() != null) {
                    u.setFormattedBirthday(u.getBirthday().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                } else {
                    u.setFormattedBirthday("未登録");
                }
            })
            .collect(Collectors.toList());

        // Modelに登録
        model.addAttribute("userList", formattedUserList);

        // こちらも同様に単一ユーザーの誕生日を表示したい場合は同様の処理を追加可能

        // ユーザー一覧画面を表示
        return "kintai/userList";
    }
}
