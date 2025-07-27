package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.UserService;
import com.example.form.UserDetailForm;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/kintai")
@Slf4j
public class UserDetailController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    /** ユーザー詳細画面を表示 */
    @GetMapping("/userDetail/{userId:.+}")
    public String getUser(UserDetailForm form, Model model, @PathVariable("userId") String userId) {

        // ユーザーを1件取得
//        MUser user = userService.getUserOne(userId);
//        user.setPassword(null);
        
        // 表示対象のユーザー情報を取得（ここがポイント）
        MUser targetUserName = userService.getUserOne(userId);
        if (targetUserName != null) {
            model.addAttribute("targetUserName", targetUserName.getUserName());
            modelMapper.map(targetUserName, form);
        } else {
            model.addAttribute("targetUserName", "不明なユーザー");
        }

        // MUserをformに変換
       // form = modelMapper.map(targetUserName, UserDetailForm.class);
       

         // ← これに変更
        // Modelに登録
        model.addAttribute("userDetailForm", form);

        // ユーザー詳細画面を表示
        return "kintai/userDetail";
    }
    
    
//    @GetMapping("/userDetail")
//    public String getUserDetail(@PathVariable("userId") String userId,UserDetailForm form, Model model) {
////    	
////    	 // ユーザーを1件取得
//        MUser user = userService.getUserOne(userId);
//       
////        user.setPassword(null);
////
////        // MUserをformに変換
//        form = modelMapper.map(user, UserDetailForm.class);
////        form.setSalaryList(user.getSalaryList());
//        // Modelに登録
//        model.addAttribute("userDetailForm", form);
//
//    	 MUser targetUserName = userService.getUserOne(userId);
//         if (targetUserName != null) {
//             model.addAttribute("targetUserName", targetUserName.getUserName());
//         } else {
//             model.addAttribute("targetUserName", "不明なユーザー");
//         }
//    	
//        return "kintai/userDetail"; // など適当な画面にリダイレクト
//    }
//
//    /** ユーザー更新処理 */
    @PostMapping(value = "/userDetail", params = "update")
    @ResponseBody
    public Map<String, String> updateUser(UserDetailForm form) {
        Map<String, String> result = new HashMap<>();
        try {
            userService.updateUserOne(form.getUserId(), form.getPassword(), form.getUserName());
            result.put("result", "success");
        } catch (Exception e) {
            log.error("ユーザー更新でエラー", e);
            result.put("result", "error");
        }
        return result;
    }

    /** ユーザー削除処理 */
    @PostMapping(value = "/userDetail", params = "delete")
    public String deleteUser(UserDetailForm form, Model model) {
        userService.deleteUserOne(form.getUserId());
        return "redirect:/kintai/userList";
    }
}
