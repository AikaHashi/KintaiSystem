package com.example.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.Keihi;
import com.example.domain.kintai.model.Koutsuhi;
import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.KeihiService;
import com.example.domain.kintai.service.KoutsuhiService;
import com.example.domain.kintai.service.UserService;
import com.example.form.KeihiForm;
import com.example.form.KoutsuhiForm;

@Controller
public class KoutsuhiKeihiController {

    @Autowired
    private UserService userService;

    @Autowired
    private KeihiService keihiService;

    @Autowired
    private KoutsuhiService koutsuhiService;

    @GetMapping("/kintai/koutsuhiKeihi/{userId}")
    public String displayKeihiForm(@PathVariable String userId, Model model) {
        MUser user = userService.getUserOne(userId);

        // DBから交通費・経費の一覧を取得
        String currentYearMonth = java.time.LocalDate.now().withDayOfMonth(1).toString().substring(0, 7);
        List<Koutsuhi> koutsuhiList = koutsuhiService.findByUserIdAndYearMonth(userId,currentYearMonth);
        List<Keihi> keihiList = keihiService.findByUserIdAndYearMonth(userId,currentYearMonth);

        
        
        // フォームにセット
        KoutsuhiForm koutsuhiForm = new KoutsuhiForm();
        koutsuhiForm.setUserId(userId);
        koutsuhiForm.setKoutsuhiList(koutsuhiList);

        KeihiForm keihiForm = new KeihiForm();
        keihiForm.setUserId(userId);
        keihiForm.setKeihiList(keihiList);

        
        model.addAttribute("koutsuhiList", koutsuhiList);
        model.addAttribute("keihiList", keihiList);
        model.addAttribute("koutsuhiForm", koutsuhiForm);
        model.addAttribute("keihiForm", keihiForm);
        model.addAttribute("sessionUserName", userId);

        return "kintai/koutsuhiKeihi";
    }

    @PostMapping("/kintai/keihi/saveKoutsuhi")
    public String saveKoutsuhi(@ModelAttribute KoutsuhiForm form) {
        List<Koutsuhi> list = form.getKoutsuhiList();
        if (list != null) {
            for (Koutsuhi k : list) {
                if (k.getDate() == null) {
                    // 日付がない行はスキップ
                    System.out.println("日付が未入力の行をスキップ: " + k);
                    continue;
                }

                // userId をセット（フォームから取得）
                k.setUserId(form.getUserId());

                if (k.getKoutsuhiId() != null && k.getKoutsuhiId() > 0) {
                    // 更新処理
                    System.out.println("更新処理: " + k);
                    koutsuhiService.update(k);
                } else {
                    // 新規追加処理
                    System.out.println("新規追加処理: " + k);
                    koutsuhiService.insertKoutsuhi(k);
                }
            }
        }

        String encodedUserId = URLEncoder.encode(form.getUserId(), StandardCharsets.UTF_8);
        return "redirect:/kintai/koutsuhiKeihi/" + encodedUserId;
    }

    @PostMapping("/kintai/keihi/saveKeihi")
    public String saveKeihi(@ModelAttribute KeihiForm form) {
        List<Keihi> list = form.getKeihiList();
        if (list != null) {
            for (Keihi k : list) {
                if (k.getDate() == null) {
                    // 日付がない行はスキップ
                    System.out.println("日付が未入力の行をスキップ: " + k);
                    continue;
                }

                // userId をセット（フォームから取得）
                k.setUserId(form.getUserId());

                if (k.getKeihiId() != null && k.getKeihiId() > 0) {
                    // 更新処理
                    System.out.println("更新処理: " + k);
                    keihiService.update(k);
                } else {
                    // 新規追加処理
                    System.out.println("新規追加処理: " + k);
                    keihiService.insertKeihi(k);
                }
            }
        }

        String encodedUserId = URLEncoder.encode(form.getUserId(), StandardCharsets.UTF_8);
        return "redirect:/kintai/koutsuhiKeihi/" + encodedUserId;
    }


    // 月指定で交通費リスト取得（Ajax用）
    @GetMapping("/kintai/keihi/listKoutsuhi")
    @ResponseBody
    public List<Koutsuhi> listKoutsuhiByMonth(@RequestParam String userId, @RequestParam String yearMonth) {
        return koutsuhiService.findByUserIdAndYearMonth(userId, yearMonth);
    }

    // 月指定で経費リスト取得（Ajax用）
    @GetMapping("/kintai/keihi/listKeihi")
    @ResponseBody
    public List<Keihi> listKeihiByMonth(@RequestParam String userId, @RequestParam String yearMonth) {
        return keihiService.findByUserIdAndYearMonth(userId, yearMonth);
    }
}
