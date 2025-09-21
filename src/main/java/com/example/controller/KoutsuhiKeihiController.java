package com.example.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.Application;
import com.example.domain.kintai.model.Keihi;
import com.example.domain.kintai.model.Koutsuhi;
import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.ApplicationService;
import com.example.domain.kintai.service.KeihiService;
import com.example.domain.kintai.service.KoutsuhiService;
import com.example.domain.kintai.service.MailService;
import com.example.domain.kintai.service.UserService;
import com.example.form.KeihiForm;
import com.example.form.KoutsuhiForm;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class KoutsuhiKeihiController {

    @Autowired private UserService userService;
    @Autowired private KeihiService keihiService;
    @Autowired private KoutsuhiService koutsuhiService;
    @Autowired private ApplicationService applicationService;
    @Autowired private MailService mailService;

    /** ---------------------- 表示 ---------------------- */
    @GetMapping("/kintai/koutsuhiKeihi/{userId:.+}")
    public String displayKeihiForm(@PathVariable("userId") String userId,
                                   @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                   Model model, HttpSession session,
                                   HttpServletRequest request) {
        System.out.println("userId=" + userId + ", yearMonth=" + yearMonth);
        System.out.println("=== displayKeihiForm called ===");

        if (userId == null || userId.isEmpty()) {
            userId = (String) session.getAttribute("userId");
        }
        if (userId == null) {
            userId = "defaultUser";
        }

        MUser user = userService.getUserOne(userId);
        if (user == null) {
            model.addAttribute("errorMessage", "ユーザーが見つかりません");
            return "error/404";
        }

        // yearMonth が指定されていなければ現在月を設定
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 遷移時も保存後と同じように、その月のデータを取得
        List<Koutsuhi> koutsuhiList = koutsuhiService.findByUserIdAndYearMonth(userId, yearMonth);
        List<Keihi> keihiList = keihiService.findByUserIdAndYearMonth(userId, yearMonth);


        KoutsuhiForm koutsuhiForm = new KoutsuhiForm();
        koutsuhiForm.setUserId(userId);
        koutsuhiForm.setKoutsuhi(koutsuhiList);

        KeihiForm keihiForm = new KeihiForm();
        keihiForm.setUserId(userId);

        keihiForm.setKeihi(keihiList);

        MUser targetUser = userService.getUserOne(userId);
        String userName = (targetUser != null) ? targetUser.getUserName() : "不明なユーザー";
        model.addAttribute("targetUserName", userName);
        model.addAttribute("targetUserId", userId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser loginUser = userService.getUserOne(currentUserName);
        model.addAttribute("sessionUserName", loginUser.getUserName());
        model.addAttribute("sessionUserId", loginUser.getUserId());
        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("ROLE_GENERAL");
        model.addAttribute("role", role);
        model.addAttribute("koutsuhiForm", koutsuhiForm);
        model.addAttribute("keihiForm", keihiForm);
        model.addAttribute("koutsuhi", koutsuhiList);
        model.addAttribute("keihiList", keihiList);
       // model.addAttribute("sessionUserId", userId);
        model.addAttribute("user", user);

        // 次月/前月切替用
        model.addAttribute("yearMonth", yearMonth);
       
        
     // Applicationステータスを取得して追加
        String statusKoutsuhi;
        String statusKeihi;

        if ("ROLE_ADMIN".equals(role)) {
            // 管理者は対象ユーザーのステータスを取得
            statusKoutsuhi = applicationService.getStatus(userId, "交通費", yearMonth);
            statusKeihi    = applicationService.getStatus(userId, "経費", yearMonth);
        } else {
            // 一般ユーザーは自分のステータスを取得
            statusKoutsuhi = applicationService.getStatus(loginUser.getUserId(), "交通費", yearMonth);
            statusKeihi    = applicationService.getStatus(loginUser.getUserId(), "経費", yearMonth);
        }

        model.addAttribute("statusKoutsuhi", statusKoutsuhi != null ? statusKoutsuhi : "");
        model.addAttribute("statusKeihi", statusKeihi != null ? statusKeihi : "");
        // ---------------------- CSRFトークン追加 ----------------------
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        model.addAttribute("_csrf", csrfToken);


        return "kintai/koutsuhiKeihi";
    }

    /** ---------------------- 交通費保存 ---------------------- */
    @PostMapping("/kintai/keihi/saveKoutsuhi")

    @ResponseBody
    public Map<String,Object> saveKoutsuhi(@ModelAttribute KoutsuhiForm form,
                                           @RequestParam(name="deletedKoutsuhiIds", required=false) String deletedIds,
                                           @RequestParam(value = "yearMonth", required = false) String  yearMonth) {
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = new SimpleDateFormat("yyyy-MM").format(new Date());

        }

        Map<String,Object> res = new HashMap<>();
        try {
            if(deletedIds != null && !deletedIds.isEmpty()) {
                for(String idStr : deletedIds.split(",")) {
                    try { koutsuhiService.delete(Integer.parseInt(idStr.trim())); }
                    catch(NumberFormatException e){ System.err.println("Invalid Koutsuhi ID: "+idStr);}
                }
            }

            if(form.getKoutsuhi() != null) {
                for(Koutsuhi k: form.getKoutsuhi()) {
                    if(k.getDate() == null) continue;
                    k.setUserId(form.getUserId());
                    if(k.getKoutsuhiId() != null && k.getKoutsuhiId() > 0) koutsuhiService.update(k);
                    else koutsuhiService.insertKoutsuhi(k);
                }
            }

            res.put("koutsuhi", koutsuhiService.findByUserIdAndYearMonth(form.getUserId(), yearMonth));
            res.put("status", "success");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    /** ---------------------- 経費保存 ---------------------- */
    @PostMapping("/kintai/keihi/saveKeihi")
    @ResponseBody
    public Map<String,Object> saveKeihi(@ModelAttribute KeihiForm form,
                                        @RequestParam(name="deletedKeihiIds", required=false) String deletedIds,
                                        @RequestParam(value = "yearMonth", required = false) String  yearMonth) {
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = new SimpleDateFormat("yyyy-MM").format(new Date());

        }

        Map<String,Object> res = new HashMap<>();
        try {
            if(deletedIds != null && !deletedIds.isEmpty()) {
                for(String idStr: deletedIds.split(",")) {
                    try { keihiService.delete(Integer.parseInt(idStr.trim())); }
                    catch(NumberFormatException e){ System.err.println("Invalid Keihi ID: "+idStr);}
                }
            }

            if(form.getKeihi() != null) {
                for(Keihi k: form.getKeihi()) {
                    if(k.getDate() == null) continue;
                    k.setUserId(form.getUserId());
                    if(k.getKeihiId() != null && k.getKeihiId() > 0) keihiService.update(k);
                    else keihiService.insertKeihi(k);
                }
            }

            res.put("keihi", keihiService.findByUserIdAndYearMonth(form.getUserId(), yearMonth));
            res.put("status", "success");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    /** ---------------------- 交通費・経費申請共通メソッド ---------------------- */
    private Map<String,Object> applyCommon(String userId, String category, String comment) {
        Map<String,Object> response = new HashMap<>();

        // userId が null または空の場合はエラー返却
        if(userId == null || userId.isEmpty()) {
            response.put("status","ERROR");
            response.put("message","セッション情報がありません。再ログインしてください。");
            return response;
        }

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate today = LocalDate.now();
        LocalDate target = LocalDate.parse(yearMonth + "-01");

        if(target.isAfter(today.withDayOfMonth(1))) { 
            response.put("status","ERROR"); 
            response.put("message","未来月申請はできません"); 
            return response; 
        }

        Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, category, yearMonth);
        if(app == null) { 
            app = new Application(); 
            app.setUserId(userId); 
            app.setCategory(category); 
            app.setYearMonth(yearMonth); 
            app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now())); 
        }

        app.setStatus("APPLYING"); 
        app.setComment(comment); 
        app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        applicationService.saveOrUpdate(app);

        try {
            MUser user = userService.getUserOne(userId);
            MUser admin = userService.findAdmin();

            // 申請者宛
            mailService.send(user.getUserId(), "【"+category+"申請受付】"+yearMonth,
                    "お疲れ様です。\n"
                  + user.getUserName()+"さんの"+category+"申請を受け付けました。\n"
                  + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                  + "引き続きよろしくお願いいたします。");

            // 管理者宛
            mailService.send(admin.getUserId(), "【"+category+"申請通知】"+yearMonth,
                    "お疲れ様です。\n"
                  + user.getUserName()+"さんから"+category+"申請が提出されました。\n"
                  + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                  + "ご確認のほどよろしくお願いいたします。");
        } catch(Exception e){ e.printStackTrace(); }

        response.put("status", app.getStatus()); 
        response.put("comment", app.getComment()); 
        response.put("createdAt", app.getCreatedAt());
        return response;
    }

    private Map<String,Object> approveOrRejectCommon(String userId, String category, String comment, String status) {
        Map<String,Object> response = new HashMap<>();

        // userId が null または空の場合はエラー返却
        if(userId == null || userId.isEmpty()) {
            response.put("status","ERROR");
            response.put("message","セッション情報がありません。再ログインしてください。");
            return response;
        }

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // categoryを必ず渡すことで、交通費でも勤怠でも対応可能
        Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, category, yearMonth);

        if(app != null) {
            app.setStatus(status); 
            app.setComment(comment == null ? "" : comment); // null対策
            app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            applicationService.saveOrUpdate(app);

            String statusText = "APPROVED".equals(status) ? "承認" : "差戻し";

            try {
                MUser user = userService.getUserOne(userId);
                MUser admin = userService.findAdmin();

                // 管理者宛（処理実行者向け）
                mailService.send(admin.getUserId(), "【"+category+statusText+"完了】"+yearMonth,
                        "お疲れ様です。\n"
                      + user.getUserName()+"さんの"+category+"を"+statusText+"しました。\n"
                      + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                      + "以上、よろしくお願いいたします。");

                // 申請者宛
                mailService.send(user.getUserId(), "【"+category+statusText+"通知】"+yearMonth,
                        "お疲れ様です。\n"
                      + "あなたの"+category+"申請が"+statusText+"されました。\n"
                      + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                      + "引き続きよろしくお願いいたします。");
            } catch(Exception e){ e.printStackTrace(); }

            response.put("status", app.getStatus()); 
            response.put("comment", app.getComment()); 
            response.put("updatedAt", app.getUpdatedAt());
        } else {
            app = new Application();
            app.setUserId(userId);
            app.setCategory(category); // ここで交通費でも経費でも勤怠でもOK
            app.setYearMonth(yearMonth);
            app.setStatus(status);
            app.setComment(comment == null ? "" : comment);
            app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            applicationService.saveOrUpdate(app);

            response.put("status", app.getStatus());
            response.put("comment", app.getComment());
            response.put("createdAt", app.getCreatedAt());
        }

        return response;
    }

    /** ---------------------- 交通費 ---------------------- */
    @PostMapping(value="/kintai/koutsuhi/apply/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> applyKoutsuhi(@RequestParam(name="comment", required=false) String comment) {
        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loginUserId = auth.getName(); // ログインユーザーID
            res = applyCommon(loginUserId, "交通費", comment);
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping(value="/kintai/koutsuhi/approve/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> approveKoutsuhi(
            @RequestParam(name="comment", required=false) String comment,
            @PathVariable("userId") String targetUserId) {

        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                res.put("status", "error");
                res.put("message", "承認権限がありません（管理者のみ）");
                return res;
            }

            // comment が null の場合は空文字に置き換える
            if (comment == null) comment = "";

            res = approveOrRejectCommon(targetUserId, "交通費", comment, "APPROVED");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping(value="/kintai/koutsuhi/reject/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> rejectKoutsuhi(
            @RequestParam(name="comment", required=false) String comment,
            @PathVariable("userId") String targetUserId) {

        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                res.put("status", "error");
                res.put("message", "差戻し権限がありません（管理者のみ）");
                return res;
            }

            // comment が null の場合は空文字に置き換える
            if (comment == null) comment = "";

            
            
            res = approveOrRejectCommon(targetUserId, "交通費", comment, "REJECTED");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }
    /** ---------------------- 経費 ---------------------- */
    @PostMapping(value="/kintai/keihi/apply/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> applyKeihi(
            @RequestParam(name="comment", required=false) String comment,
            @PathVariable("userId") String userId,
            HttpSession session) {
        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loginUserId = auth.getName(); // ログインユーザーID
            res = applyCommon(loginUserId, "経費", comment);
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping(value="/kintai/keihi/approve/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> approveKeihi(
            @RequestParam(name="comment", required=false) String comment,
            @PathVariable("userId") String targetUserId) {
        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                res.put("status", "error");
                res.put("message", "承認権限がありません（管理者のみ）");
                return res;
            }
            res = approveOrRejectCommon(targetUserId, "経費", comment, "APPROVED");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    @PostMapping(value="/kintai/keihi/reject/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> rejectKeihi(
            @RequestParam(name="comment", required=false) String comment,
            @PathVariable("userId") String targetUserId) {
        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                res.put("status", "error");
                res.put("message", "差戻し権限がありません（管理者のみ）");
                return res;
            }

            if (comment == null) comment = ""; // ★必須

            res = approveOrRejectCommon(targetUserId, "経費", comment, "REJECTED");
        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    /** ---------------------- 例外ハンドリング（JSON強制） ---------------------- */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Map<String,Object> handleException(Exception e) {
        e.printStackTrace();
        Map<String,Object> res = new HashMap<>();
        res.put("status", "error");
        res.put("message", e.getMessage());
        return res;
    }
    
    /** ---------------------- JSON取得用（安全版） ---------------------- */
    @GetMapping(value="/kintai/koutsuhiKeihiSafe/{userId:.+}", produces="application/json")
    @ResponseBody
    public Map<String,Object> getKoutsuhiKeihiJson(
            @PathVariable("userId") String userId,
            @RequestParam(value = "yearMonth", required = false) String yearMonth) {

        Map<String,Object> res = new HashMap<>();
        try {
            if(yearMonth == null || yearMonth.isEmpty()) {
                yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = authentication.getName();
            MUser loginUser = userService.getUserOne(currentUserName);

            List<Koutsuhi> koutsuhiList = koutsuhiService.findByUserIdAndYearMonth(userId, yearMonth);
            List<Keihi> keihiList = keihiService.findByUserIdAndYearMonth(userId, yearMonth);

            // ステータス取得（ロールによって参照先を変える）
            String statusKoutsuhi = applicationService.getStatus(
                    "ROLE_ADMIN".equals(loginUser.getRole()) ? userId : loginUser.getUserId(),
                    "交通費", yearMonth);

            String statusKeihi = applicationService.getStatus(
                    "ROLE_ADMIN".equals(loginUser.getRole()) ? userId : loginUser.getUserId(),
                    "経費", yearMonth);

            // 管理者の場合、null のときは改めて userId で確認
            if (statusKoutsuhi == null && "ROLE_ADMIN".equals(loginUser.getRole())) {
                statusKoutsuhi = applicationService.getStatus(userId, "交通費", yearMonth);
            }
            if (statusKeihi == null && "ROLE_ADMIN".equals(loginUser.getRole())) {
                statusKeihi = applicationService.getStatus(userId, "経費", yearMonth);
            }

            res.put("statusKoutsuhi", statusKoutsuhi);
            res.put("statusKeihi", statusKeihi);
            res.put("userRole", loginUser.getRole());
            res.put("koutsuhi", koutsuhiList);
            res.put("keihi", keihiList);
            res.put("status", "success");

            System.out.println("サーバーからのステータス: 交通費=" + statusKoutsuhi + ", 経費=" + statusKeihi);

        } catch(Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
            res.put("statusKoutsuhi", "error");
            res.put("statusKeihi", "error");
            res.put("koutsuhi", List.of());
            res.put("keihi", List.of());
        }
        return res;
    }
}
