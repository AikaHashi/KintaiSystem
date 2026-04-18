package com.example.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
import com.example.form.ValidGroup1;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

@Controller
public class KoutsuhiKeihiController {

    @Autowired private UserService userService;
    @Autowired private KeihiService keihiService;
    @Autowired private KoutsuhiService koutsuhiService;
    @Autowired private ApplicationService applicationService;
    @Autowired private MailService mailService;
    @Autowired private Validator validator;
    
    private static final ZoneId JST = ZoneId.of("Asia/Tokyo");

    /** ---------------------- 表示 ---------------------- */
    @GetMapping("/kintai/koutsuhiKeihi/{userId:.+}")
    public String displayKeihiForm(@PathVariable("userId") String userId,
                                   @RequestParam(value = "yearMonth", required = false) String yearMonth,
                                   Model model, HttpSession session,
                                   HttpServletRequest request) {
    	
    	System.out.println("=== displayKeihiForm START ===");
    	System.out.println("userId=" + userId);
    	System.out.println("yearMonth=" + yearMonth);

    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	System.out.println("loginUser=" + auth.getName());

    	System.out.println("serverTime=" + LocalDateTime.now());
    	System.out.println("serverTime JST=" + LocalDateTime.now(JST));
    	System.out.println("=== displayKeihiForm END ===");
    	
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
//        if (yearMonth == null || yearMonth.isEmpty()) {
//            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // 遷移時も保存後と同じように、その月のデータを取得
        List<Koutsuhi> koutsuhiList = koutsuhiService.findByUserIdAndYearMonth(userId, yearMonth);
        List<Keihi> keihiList = keihiService.findByUserIdAndYearMonth(userId, yearMonth);

        System.out.println("koutsuhi size=" + koutsuhiList.size());
        System.out.println("keihi size=" + keihiList.size());

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
    @PostMapping("/kintai/koutsuhi/saveKoutsuhi")
    @ResponseBody
    public Map<String,Object> saveKoutsuhi(
            @Valid @ModelAttribute KoutsuhiForm form,
            BindingResult result,
            @RequestParam(name="deletedKoutsuhiIds", required=false) String deletedIds,
            @RequestParam(value = "yearMonth", required = false) String yearMonth) {

        Map<String,Object> res = new HashMap<>();

        // ★ 独自バリデーション（その他→特記事項必須）
        if (form.getKoutsuhi() != null) {
            for (int i = 0; i < form.getKoutsuhi().size(); i++) {
                Koutsuhi k = form.getKoutsuhi().get(i);

                if ("その他".equals(k.getMethod())) {
                    if (k.getNote() == null || k.getNote().isBlank()) {
                        result.rejectValue(
                            "koutsuhi[" + i + "].note",
                            "note.required",
                            "特記事項は必須です"
                        );
                    }
                }
            }
        }

        // ★ 行ごとエラー整形（ソート対応版）
        if (result.hasErrors()) {

            Map<Integer, List<String>> errorMap = new LinkedHashMap<>();

            for (var e : result.getFieldErrors()) {
                String field = e.getField(); // koutsuhi[1].departure
                String msg = e.getDefaultMessage();

                int start = field.indexOf("[");
                int end = field.indexOf("]");

                int row = -1;
                if (start != -1 && end != -1) {
                    row = Integer.parseInt(field.substring(start + 1, end));
                }

                errorMap
                    .computeIfAbsent(row, k -> new ArrayList<>())
                    .add(msg);
            }

            // ★ 表示用に整形（←ここをソート版に変更）
            List<String> errors = new ArrayList<>();

            errorMap.entrySet().stream()
                .filter(e -> e.getKey() >= 0) // 念のため
                .sorted(Map.Entry.comparingByKey()) // ★これが重要（行順にする）
                .forEach(entry -> {
                    int row = entry.getKey();
                    List<String> msgs = entry.getValue();

                    StringBuilder sb = new StringBuilder();
                    sb.append((row + 1) + "行目:\n");

                    for (String m : msgs) {
                        sb.append("・").append(m).append("\n");
                    }

                    errors.add(sb.toString());
                });

            res.put("status", "error");
            res.put("errors", errors);
            return res;
        }

        try {
            if (yearMonth == null || yearMonth.isEmpty()) {
                yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }

            if(deletedIds != null && !deletedIds.isEmpty()) {
                for(String idStr : deletedIds.split(",")) {
                    try { 
                        koutsuhiService.delete(Integer.parseInt(idStr.trim())); 
                    } catch(NumberFormatException e){ 
                        System.err.println("Invalid Koutsuhi ID: "+idStr);
                    }
                }
            }

            if(form.getKoutsuhi() != null) {
                for(Koutsuhi k: form.getKoutsuhi()) {

                    if(k.getDate() == null) continue;

                    k.setUserId(form.getUserId());

                    if(k.getKoutsuhiId() != null && k.getKoutsuhiId() > 0) {
                        koutsuhiService.update(k);
                    } else {
                        koutsuhiService.insertKoutsuhi(k);
                    }
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
    public Map<String,Object> saveKeihi(
            @ModelAttribute @Validated(ValidGroup1.class) KeihiForm form,
            BindingResult result,
            @RequestParam(name="deletedKeihiIds", required=false) String deletedIds,
            @RequestParam(value = "yearMonth", required = false) String yearMonth) {

        Map<String,Object> res = new HashMap<>();

        // =========================
        // yearMonth補正
        // =========================
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }

        // =====================================================
        // ★ 追加：method別バリデーション（ここが今回の本体）
        // =====================================================
        List<String> methodErrors = new ArrayList<>();

        if (form.getKeihi() != null) {
            for (int i = 0; i < form.getKeihi().size(); i++) {

                Keihi k = form.getKeihi().get(i);

                if ("その他".equals(k.getMethod())) {

                    if (k.getDate() == null) {
                        result.rejectValue("keihi[" + i + "].date", "date.required", "日付は必須です");
                    }

                    if (k.getAmount() == null) {
                        result.rejectValue("keihi[" + i + "].amount", "amount.required", "金額は必須です");
                    }

                    if (k.getNote() == null || k.getNote().isBlank()) {
                        result.rejectValue("keihi[" + i + "].note", "note.required", "特記事項は必須です");
                    }

                } else {

                    if (k.getDate() == null) {
                        result.rejectValue("keihi[" + i + "].date", "date.required", "日付は必須です");
                    }

                    if (k.getDeparture() == null || k.getDeparture().isBlank()) {
                        result.rejectValue("keihi[" + i + "].departure", "departure.required", "出発地は必須です");
                    }

                    if (k.getArrival() == null || k.getArrival().isBlank()) {
                        result.rejectValue("keihi[" + i + "].arrival", "arrival.required", "到着地は必須です");
                    }

                    if (k.getAmount() == null) {
                        result.rejectValue("keihi[" + i + "].amount", "amount.required", "金額は必須です");
                    }
                }
            }
        }

        // =========================
        // エラー整形
        // =========================
        if (result.hasErrors()) {

            Map<Integer, List<String>> errorMap = new LinkedHashMap<>();

            for (var e : result.getFieldErrors()) {

                String field = e.getField();
                String msg = e.getDefaultMessage();

                int row = -1;
                int start = field.indexOf("[");
                int end = field.indexOf("]");

                if (start != -1 && end != -1) {
                    try {
                        row = Integer.parseInt(field.substring(start + 1, end));
                    } catch (Exception ignore) {}
                }

                errorMap.computeIfAbsent(row, k -> new ArrayList<>()).add(msg);
            }

            List<String> errors = new ArrayList<>();

            errorMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {

                        StringBuilder sb = new StringBuilder();
                        sb.append((entry.getKey() + 1)).append("行目:\n");

                        for (String m : entry.getValue()) {
                            sb.append("・").append(m).append("\n");
                        }

                        errors.add(sb.toString());
                    });

            res.put("status", "error");
            res.put("errors", errors);
            return res;
        }

        // =========================
        // 保存処理
        // =========================
        try {

            if (deletedIds != null && !deletedIds.isEmpty()) {
                for (String idStr : deletedIds.split(",")) {
                    try {
                        keihiService.delete(Integer.parseInt(idStr.trim()));
                    } catch (Exception ignore) {}
                }
            }

            for (Keihi k : form.getKeihi()) {

                if (k.getDate() == null) continue;

                k.setUserId(form.getUserId());

                if (k.getKeihiId() != null && k.getKeihiId() > 0) {
                    keihiService.update(k);
                } else {
                    keihiService.insertKeihi(k);
                }
            }

            res.put("keihi",
                    keihiService.findByUserIdAndYearMonth(form.getUserId(), yearMonth));

            res.put("status", "success");

        } catch (Exception e) {
            e.printStackTrace();
            res.put("status", "error");
            res.put("message", e.getMessage());
        }

        return res;
    }
    /** ---------------------- 交通費・経費申請共通メソッド ---------------------- */
    private Map<String,Object> applyCommon(String userId, String category, String comment, String yearMonth) {
        Map<String,Object> response = new HashMap<>();

//        if (yearMonth == null || yearMonth.isEmpty()) {
//            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        // userId が null または空の場合はエラー返却
        if(userId == null || userId.isEmpty()) {
            response.put("status","ERROR");
            response.put("message","セッション情報がありません。再ログインしてください。");
            return response;
        }

//        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        if (yearMonth == null || yearMonth.isEmpty()) {
//            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
        
        LocalDate today = LocalDate.now(JST);
        LocalDate target = LocalDate.parse(yearMonth + "-01");

        if (target.isAfter(today.withDayOfMonth(1))) {
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
            app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(JST))); 
        }

        app.setStatus("APPLYING"); 
        app.setComment(comment); 
//        app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now(JST)));
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

    private Map<String,Object> approveOrRejectCommon(String userId, String category, String comment, String status, String yearMonth) {
        Map<String,Object> response = new HashMap<>();

//        if (yearMonth == null || yearMonth.isEmpty()) {
//            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
        
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        // userId が null または空の場合はエラー返却
        if(userId == null || userId.isEmpty()) {
            response.put("status","ERROR");
            response.put("message","セッション情報がありません。再ログインしてください。");
            return response;
        }

//        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

//        if (yearMonth == null || yearMonth.isEmpty()) {
//            yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//        }
        if (yearMonth == null || yearMonth.isEmpty()) {
            yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
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
//            app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
//            app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now(JST)));
            app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now(JST)));
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
    public Map<String,Object> applyKoutsuhi(
            @RequestParam(name="comment", required=false) String comment,
            @RequestParam(value="yearMonth", required=false) String yearMonth) {

        Map<String,Object> res = new HashMap<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String loginUserId = auth.getName();

            // 過去月でも申請可能
            res = applyCommon(loginUserId, "交通費", comment, yearMonth);

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
            @RequestParam(value="yearMonth", required=false) String yearMonth, // ★追加
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

            if (comment == null) comment = "";

            // ★ここ修正
            res = approveOrRejectCommon(targetUserId, "交通費", comment, "APPROVED", yearMonth);

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
            @RequestParam(value="yearMonth", required=false) String yearMonth, // ★追加
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

            if (comment == null) comment = "";

            // yearMonth が null の場合は今月をセット
            if (yearMonth == null || yearMonth.isEmpty()) {
                yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM"));
            }

            res = approveOrRejectCommon(targetUserId, "交通費", comment, "REJECTED", yearMonth);
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
        @RequestParam(value="yearMonth", required=false) String yearMonth) {

    Map<String,Object> res = new HashMap<>();
    try {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginUserId = auth.getName();

        // 過去月でも申請可能
        res = applyCommon(loginUserId, "経費", comment, yearMonth);

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

    	System.out.println("=== JSON API START ===");
    	System.out.println("userId=" + userId);
    	System.out.println("yearMonth=" + yearMonth);
    	System.out.println("loginUser=" + SecurityContextHolder.getContext().getAuthentication().getName());
    	
        Map<String,Object> res = new HashMap<>();
        try {
//            if(yearMonth == null || yearMonth.isEmpty()) {
//                yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
//            }
        	if (yearMonth == null || yearMonth.isEmpty()) {
        	    yearMonth = LocalDate.now(ZoneId.of("Asia/Tokyo"))
        	            .format(DateTimeFormatter.ofPattern("yyyy-MM"));
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
