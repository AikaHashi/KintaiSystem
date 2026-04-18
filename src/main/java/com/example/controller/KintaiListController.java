package com.example.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.Application;
import com.example.domain.kintai.model.Kintai;
import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.ApplicationService;
import com.example.domain.kintai.service.KintaiService;
import com.example.domain.kintai.service.MailService;
import com.example.domain.kintai.service.UserService;
import com.example.dto.KintaiDto;
import com.example.form.KeihiForm;
import com.example.util.TimeUtil;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/kintai")
public class KintaiListController {

    private static final Logger logger = LoggerFactory.getLogger(KintaiListController.class);

    @Autowired
    private KintaiService kintaiService;

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ModelMapper modelMapper;

    
    /** 安全な文字列 → LocalTime 変換 */
    private LocalTime parseTimeSafely(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return LocalTime.parse(timeStr, TIME_FORMATTER);
        } catch (Exception ex) {
            logger.warn("時間文字列解析失敗: {}", timeStr, ex);
            return null;
        }
    }
    
    /** 勤怠一覧表示 */
    @GetMapping("/kintaiList/{userId:.+}")
    public String showKintaiList(@ModelAttribute("KeihiForm") KeihiForm keihiForm,
                                 @PathVariable("userId") String userId,
                                 Model model,
                                 HttpSession session) {

        List<Kintai> kintaiList = kintaiService.getListByUserId(userId);
        MUser targetUser = userService.getUserOne(userId);
        String userName = (targetUser != null) ? targetUser.getUserName() : "不明なユーザー";

        List<KintaiDto> kintaiDtoList = kintaiList.stream()
                .map(this::convertToDto)
                .peek(dto -> dto.setUserName(userName))
                .collect(Collectors.toList());
        model.addAttribute("kintaiList", kintaiDtoList);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser loginUser = userService.getUserOne(currentUserName);

        model.addAttribute("sessionUserName", loginUser.getUserName());
        model.addAttribute("sessionUserId", loginUser.getUserId());

        if (session.getAttribute("userId") == null) {
            session.setAttribute("userId", loginUser.getUserId());
        }

        model.addAttribute("targetUserName", userName);
        model.addAttribute("targetUserId", userId);

        String role = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .findFirst()
                .orElse("ROLE_GENERAL");
        model.addAttribute("role", role);

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, "勤怠", yearMonth);
        model.addAttribute("kintaiApplication", app);

        String status = applicationService.getStatus(loginUser.getUserId(), role, yearMonth);
        model.addAttribute("status", status);
        model.addAttribute("currentYearMonth", yearMonth);

        return "kintai/kintaiList";
    }

    /** Entity → DTO の変換 */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private KintaiDto convertToDto(Kintai entity) {
        KintaiDto dto = new KintaiDto();
        dto.setUserId(entity.getUserId());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setWorkDate(entity.getWorkDate());

        dto.setPlannedWorkStartTime(entity.getPlannedWorkStartTime());
        dto.setPlannedWorkEndTime(entity.getPlannedWorkEndTime());
        dto.setPlannedBreakStartTime(entity.getPlannedBreakStartTime());
        dto.setPlannedBreakEndTime(entity.getPlannedBreakEndTime());
        dto.setActualWorkStartTime(entity.getActualWorkStartTime());
        dto.setActualWorkEndTime(entity.getActualWorkEndTime());
        dto.setActualBreakStartTime(entity.getActualBreakStartTime());
        dto.setActualBreakEndTime(entity.getActualBreakEndTime());

        dto.setScheduledWorkHoursDecimal(entity.getScheduledWorkHours() != null ? entity.getScheduledWorkHours() : BigDecimal.ZERO);
        dto.setActualWorkHoursDecimal(entity.getActualWorkHours() != null ? entity.getActualWorkHours() : BigDecimal.ZERO);
        dto.setOvertimeHoursDecimal(entity.getOvertimeHours() != null ? entity.getOvertimeHours() : BigDecimal.ZERO);
        dto.setDeductionTimeDecimal(entity.getDeductionTime() != null ? entity.getDeductionTime() : BigDecimal.ZERO);

        dto.setScheduledWorkHours(dto.getScheduledWorkHoursDecimal().setScale(2, RoundingMode.HALF_UP).toString());
        dto.setActualWorkHours(dto.getActualWorkHoursDecimal().setScale(2, RoundingMode.HALF_UP).toString());
        dto.setOvertimeHours(dto.getOvertimeHoursDecimal().setScale(2, RoundingMode.HALF_UP).toString());
        dto.setDeductionTime(dto.getDeductionTimeDecimal().setScale(2, RoundingMode.HALF_UP).toString());

        dto.setKintaiStatus(entity.getKintaiStatus());
        dto.setKintaiComment(entity.getKintaiComment());
        return dto;
    }

    /** Ajaxによる勤怠保存（全休・代休対応・負値防止） */
    @PostMapping("/api/save-list/{userId:.+}")
    @ResponseBody
    public Map<String, Object> saveOrUpdateKintaiListAjax(
            @RequestBody List<KintaiDto> dtoList,
            @PathVariable("userId") String userId,
            Model model) {

        logger.info("save-list API 到達 userId=" + userId);
        Map<String, Object> response = new HashMap<>();

        try {

            // ===============================
            // 0. データなしチェック（追加）
            // ===============================
            if (dtoList == null || dtoList.isEmpty()) {
                response.put("status", "error");
                response.put("message", "保存するデータはありません");
                return response;
            }

            // ===============================
            // ① 行単位バリデーション
            // ===============================
            Map<Integer, List<String>> errorMap = new LinkedHashMap<>();

            for (int i = 0; i < dtoList.size(); i++) {

                KintaiDto dto = dtoList.get(i);
                dto.syncLocalTimeToString();

                List<String> rowErrors = new ArrayList<>();

                if (dto.getWorkDate() == null) {
                    rowErrors.add("日付は必須です");
                }
                if (dto.getPlannedWorkStartTimeStr() == null || dto.getPlannedWorkStartTimeStr().isEmpty()) {
                    rowErrors.add("予定開始時刻は必須です");
                }
                if (dto.getPlannedWorkEndTimeStr() == null || dto.getPlannedWorkEndTimeStr().isEmpty()) {
                    rowErrors.add("予定終了時刻は必須です");
                }
                if (dto.getActualWorkStartTimeStr() == null || dto.getActualWorkStartTimeStr().isEmpty()) {
                    rowErrors.add("実績開始時刻は必須です");
                }
                if (dto.getActualWorkEndTimeStr() == null || dto.getActualWorkEndTimeStr().isEmpty()) {
                    rowErrors.add("実績終了時刻は必須です");
                }

                if (!rowErrors.isEmpty()) {
                    errorMap.put(i, rowErrors);
                }
            }

            // ===============================
            // ② エラー整形
            // ===============================
            if (!errorMap.isEmpty()) {

                List<String> errors = new ArrayList<>();

                errorMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {

                        int row = entry.getKey();
                        List<String> msgs = entry.getValue();

                        StringBuilder sb = new StringBuilder();
                        KintaiDto dto = dtoList.get(row);
                        sb.append(dto.getWorkDate()).append(":\n");

                        for (String m : msgs) {
                            sb.append("・").append(m).append("\n");
                        }

                        errors.add(sb.toString());
                    });

                response.put("status", "error");
                response.put("errors", errors);
                return response;
            }

            // ===============================
            // ③ 保存処理
            // ===============================
            List<KintaiDto> savedDtos = new ArrayList<>();

            for (KintaiDto dto : dtoList) {

                dto.syncLocalTimeToString();

                Kintai kintai = modelMapper.map(dto, Kintai.class);
                kintai.setUserId(userId);
                kintai.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

                // 時刻変換
                kintai.setPlannedWorkStartTime(parseTimeSafely(dto.getPlannedWorkStartTimeStr()));
                kintai.setPlannedWorkEndTime(parseTimeSafely(dto.getPlannedWorkEndTimeStr()));
                kintai.setPlannedBreakStartTime(parseTimeSafely(dto.getPlannedBreakStartTimeStr()));
                kintai.setPlannedBreakEndTime(parseTimeSafely(dto.getPlannedBreakEndTimeStr()));
                kintai.setActualWorkStartTime(parseTimeSafely(dto.getActualWorkStartTimeStr()));
                kintai.setActualWorkEndTime(parseTimeSafely(dto.getActualWorkEndTimeStr()));
                kintai.setActualBreakStartTime(parseTimeSafely(dto.getActualBreakStartTimeStr()));
                kintai.setActualBreakEndTime(parseTimeSafely(dto.getActualBreakEndTimeStr()));

                try {
                    BigDecimal scheduledHours = calculateWorkHours(
                            dto.getPlannedWorkStartTimeStr(),
                            dto.getPlannedWorkEndTimeStr(),
                            dto.getPlannedBreakStartTimeStr(),
                            dto.getPlannedBreakEndTimeStr());

                    kintai.setScheduledWorkHours(scheduledHours);
                    dto.setScheduledWorkHours(scheduledHours.setScale(2, RoundingMode.HALF_UP).toString());

                    BigDecimal actualHours = calculateWorkHours(
                            dto.getActualWorkStartTimeStr(),
                            dto.getActualWorkEndTimeStr(),
                            dto.getActualBreakStartTimeStr(),
                            dto.getActualBreakEndTimeStr());

                    kintai.setActualWorkHours(actualHours);
                    dto.setActualWorkHours(actualHours.setScale(2, RoundingMode.HALF_UP).toString());

                    BigDecimal deductionTime = TimeUtil.calculateDeduction(
                            dto.isHolidayOrSubstitute(), scheduledHours, actualHours);

                    kintai.setDeductionTime(deductionTime);
                    dto.setDeductionTime(deductionTime.setScale(2, RoundingMode.HALF_UP).toString());

                } catch (Exception ex) {
                    logger.error("勤務時間計算でエラー", ex);

                    kintai.setScheduledWorkHours(BigDecimal.ZERO);
                    kintai.setActualWorkHours(BigDecimal.ZERO);
                    kintai.setDeductionTime(BigDecimal.ZERO);

                    dto.setScheduledWorkHours("0.00");
                    dto.setActualWorkHours("0.00");
                    dto.setDeductionTime("0.00");
                }

                Kintai existing = kintaiService.selectOneByUserIdAndDate(userId, kintai.getWorkDate());
                if (existing != null) {
                    kintaiService.update(kintai);
                } else {
                    kintaiService.insert(kintai);
                }

                savedDtos.add(modelMapper.map(kintai, KintaiDto.class));
            }

            response.put("status", "success");
            response.put("message", "勤怠情報を保存しました");
            response.put("data", savedDtos);

        } catch (Exception e) {
            logger.error("勤怠の一括保存処理でエラーが発生しました", e);
            response.put("status", "error");
            response.put("message", "保存に失敗しました。サーバーを確認してください");
        }

        return response;
    }
    // --- 勤務時間計算補助メソッド ---
    private BigDecimal calculateWorkHours(String start, String end, String breakStart, String breakEnd) {
        if (start == null || end == null) return BigDecimal.ZERO;
        int workMinutes = Math.max(TimeUtil.calculateMinutes(start, end), 0);
        int breakMinutes = Math.max(calculateMinutesSafe(breakStart, breakEnd), 0);
        if (breakMinutes > workMinutes) breakMinutes = workMinutes;
        return BigDecimal.valueOf(workMinutes - breakMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBreakHours(String breakStart, String breakEnd) {
        int breakMinutes = Math.max(calculateMinutesSafe(breakStart, breakEnd), 0);
        return BigDecimal.valueOf(breakMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int calculateMinutesSafe(String start, String end) {
        if (start == null || end == null) return 0;
        return Math.max(TimeUtil.calculateMinutes(start, end), 0);
    }

    /** 勤怠申請ステータス取得API（AJAX用） */
    @GetMapping("/status")
    @ResponseBody
    public Map<String, Object> getKintaiStatus(@RequestParam("userId") String userId,
                                               @RequestParam("yearMonth") String yearMonth) {
        Map<String, Object> response = new HashMap<>();
        Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, "勤怠", yearMonth);

        if (app != null) {
            response.put("status", app.getStatus() == null ? "" : app.getStatus());
            response.put("comment", app.getComment());
        } else {
            response.put("status", "");
            response.put("comment", "");
        }
        return response;
    }

    // --- ModelMapper に String → BigDecimal コンバータを登録 ---
    @PostConstruct
    public void initModelMapper() {
        modelMapper.addConverter(
                context -> TimeUtil.convertTimeStringToBigDecimal(context.getSource()),
                String.class,
                BigDecimal.class
        );
    }

    // ----------------- 勤怠申請API追加 -----------------

    @PostMapping(value="/apply", produces="application/json")
    @ResponseBody
    public Map<String,Object> applyKintai(@RequestParam(name="comment", required=false) String comment) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginUserId = auth.getName();
        return applyCommon(loginUserId, "勤怠", comment);
    }

    @PostMapping(value="/approve", produces="application/json")
    @ResponseBody
    public Map<String,Object> approveKintai(@RequestParam("userId") String targetUserId,
                                            @RequestParam(name="comment", required=false) String comment) {
        // ★ 管理者ロールチェック
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            Map<String,Object> res = new HashMap<>();
            res.put("status","error");
            res.put("message","承認権限がありません（管理者のみ）");
            return res;
        }
        return approveOrRejectCommon(targetUserId, "勤怠", comment, "APPROVED");
    }


    @PostMapping(value="/reject", produces="application/json; charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> rejectKintai(@RequestParam("userId") String targetUserId,
                                                           @RequestParam("comment") String comment) {
        Map<String,Object> res = new HashMap<>();
        try {
            // ★ 管理者ロールチェック
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin) {
                res.put("status", "error");
                res.put("message", "差戻し権限がありません（管理者のみ）");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
            }

            // ★ approveOrRejectCommon が JSON を返す前提で呼び出し
            Map<String,Object> result = approveOrRejectCommon(targetUserId, "勤怠", comment, "REJECTED");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", "処理中にエラーが発生しました: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    }

    private Map<String,Object> applyCommon(String userId, String category, String comment) {
        Map<String,Object> res = new HashMap<>();
        try {
            String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, category, yearMonth);
            if (app == null) {
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

            MUser user = userService.getUserOne(userId);
            MUser admin = userService.findAdmin();
         // 申請者宛て
            mailService.send(user.getUserId(), "【"+category+"申請受付】"+yearMonth,
                    "お疲れ様です。\n"
                  + user.getUserName()+"さんの"+category+"申請を受け付けました。\n"
                  + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                  + "引き続きよろしくお願いいたします。");

            // 管理者宛て
            mailService.send(admin.getUserId(), "【"+category+"申請通知】"+yearMonth,
                    "お疲れ様です。\n"
                  + user.getUserName()+"さんから"+category+"申請が提出されました。\n"
                  + "コメント: "+(comment==null?"なし":comment)+"\n\n"
                  + "ご確認のほどよろしくお願いいたします。");

            res.put("status", app.getStatus());
            res.put("comment", app.getComment());
            res.put("createdAt", app.getCreatedAt());
        } catch(Exception e) {
            logger.error("申請処理エラー", e);
            res.put("status","error");
            res.put("message", e.getMessage());
        }
        return res;
    }

    private Map<String,Object> approveOrRejectCommon(String userId, String category, String comment, String status) {
        Map<String,Object> res = new HashMap<>();
        try {
            String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            Application app = applicationService.findByUserIdAndCategoryAndYearMonth(userId, category, yearMonth);
            if (app == null) {
                app = new Application();
                app.setUserId(userId);
                app.setCategory(category);
                app.setYearMonth(yearMonth);
                app.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
            }
            app.setStatus(status);
            app.setComment(comment);
            app.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            applicationService.saveOrUpdate(app);

            MUser user = userService.getUserOne(userId);
            MUser admin = userService.findAdmin();
            String statusText = "APPROVED".equals(status) ? "承認" : "差戻し";

         // 管理者宛て（処理実行者向け）
         mailService.send(admin.getUserId(), "【"+category+statusText+"完了】"+yearMonth,
                 "お疲れ様です。\n"
               + user.getUserName()+"さんの"+category+"を"+statusText+"しました。\n"
               + "コメント: "+(comment==null?"なし":comment)+"\n\n"
               + "以上、よろしくお願いいたします。");

         // 申請者宛て
         mailService.send(user.getUserId(), "【"+category+statusText+"結果通知】"+yearMonth,
                 "お疲れ様です。\n"
               + "あなたの"+category+"申請が"+statusText+"されました。\n"
               + "コメント: "+(comment==null?"なし":comment)+"\n\n"
               + "引き続きよろしくお願いいたします。");
            res.put("status", app.getStatus());
            res.put("comment", app.getComment());
            res.put("updatedAt", app.getUpdatedAt());
        } catch(Exception e) {
            logger.error("承認/却下処理エラー", e);
            res.put("status","error");
            res.put("message", e.getMessage());
        }
        return res;
    }
    
}
