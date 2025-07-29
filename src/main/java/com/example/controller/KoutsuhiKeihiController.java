package com.example.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
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
        if (user == null) {
            model.addAttribute("errorMessage", "ユーザーが見つかりません。");
            return "error/404"; // 404ページを用意しておくと良い
        }

        String currentYearMonth = java.time.LocalDate.now().withDayOfMonth(1).toString().substring(0, 7);

        List<Koutsuhi> koutsuhiList = koutsuhiService.findByUserIdAndYearMonth(userId, currentYearMonth);
        List<Keihi> keihiList = keihiService.findByUserIdAndYearMonth(userId, currentYearMonth);

        KoutsuhiForm koutsuhiForm = new KoutsuhiForm();
        koutsuhiForm.setUserId(userId);
        koutsuhiForm.setKoutsuhiList(koutsuhiList);

        KeihiForm keihiForm = new KeihiForm();
        keihiForm.setUserId(userId);
        keihiForm.setKeihiList(keihiList);

        model.addAttribute("koutsuhiForm", koutsuhiForm);
        model.addAttribute("keihiForm", keihiForm);
        model.addAttribute("koutsuhiList", koutsuhiList);
        model.addAttribute("keihiList", keihiList);
        model.addAttribute("sessionUserId", userId); // ← 画面側でも `${sessionUserId}` を使うように統一

        return "kintai/koutsuhiKeihi";
    }

    @PostMapping("/kintai/keihi/saveKoutsuhi")
    public String saveKoutsuhi(
            @ModelAttribute KoutsuhiForm form,
            @RequestParam(name = "deletedKoutsuhiIds", required = false) String deletedKoutsuhiIds) {

        if (deletedKoutsuhiIds != null && !deletedKoutsuhiIds.isEmpty()) {
            for (String idStr : deletedKoutsuhiIds.split(",")) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    koutsuhiService.delete(id);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid Koutsuhi ID: " + idStr);
                }
            }
        }

        List<Koutsuhi> list = form.getKoutsuhiList();
        if (list != null) {
            for (Koutsuhi k : list) {
                if (k.getDate() == null) continue;
                k.setUserId(form.getUserId());

                if (k.getKoutsuhiId() != null && k.getKoutsuhiId() > 0) {
                    koutsuhiService.update(k);
                } else {
                    koutsuhiService.insertKoutsuhi(k);
                }
            }
        }

        String encodedUserId = URLEncoder.encode(form.getUserId(), StandardCharsets.UTF_8);
        return "redirect:/kintai/koutsuhiKeihi/" + encodedUserId;
    }

    @PostMapping("/kintai/keihi/saveKeihi")
    public String saveKeihi(
            @ModelAttribute KeihiForm form,
            @RequestParam(name = "deletedKeihiIds", required = false) String deletedKeihiIds) {

        if (deletedKeihiIds != null && !deletedKeihiIds.isEmpty()) {
            for (String idStr : deletedKeihiIds.split(",")) {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    keihiService.delete(id);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid Keihi ID: " + idStr);
                }
            }
        }

        List<Keihi> list = form.getKeihiList();
        if (list != null) {
            for (Keihi k : list) {
                if (k.getDate() == null) continue;
                k.setUserId(form.getUserId());

                if (k.getKeihiId() != null && k.getKeihiId() > 0) {
                    keihiService.update(k);
                } else {
                    keihiService.insertKeihi(k);
                }
            }
        }

        String encodedUserId = URLEncoder.encode(form.getUserId(), StandardCharsets.UTF_8);
        return "redirect:/kintai/koutsuhiKeihi/" + encodedUserId;
    }

    @DeleteMapping("/kintai/keihi/deleteKoutsuhi")
    @ResponseBody
    public ResponseEntity<String> deleteKoutsuhi(@RequestParam int koutsuhiId) {
        int result = koutsuhiService.delete(koutsuhiId);
        return result > 0
                ? ResponseEntity.ok("削除成功")
                : ResponseEntity.badRequest().body("削除失敗");
    }

    @DeleteMapping("/kintai/keihi/deleteKeihi")
    @ResponseBody
    public ResponseEntity<String> deleteKeihi(@RequestParam int keihiId) {
        int result = keihiService.delete(keihiId);
        return result > 0
                ? ResponseEntity.ok("削除成功")
                : ResponseEntity.badRequest().body("削除失敗");
    }

    @GetMapping("/kintai/keihi/listKoutsuhi")
    @ResponseBody
    public List<Koutsuhi> listKoutsuhiByMonth(@RequestParam String userId, @RequestParam String yearMonth) {
        return koutsuhiService.findByUserIdAndYearMonth(userId, yearMonth);
    }

    @GetMapping("/kintai/keihi/listKeihi")
    @ResponseBody
    public List<Keihi> listKeihiByMonth(@RequestParam String userId, @RequestParam String yearMonth) {
        return keihiService.findByUserIdAndYearMonth(userId, yearMonth);
    }
}
