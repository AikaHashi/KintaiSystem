package com.example.controller;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.DayInfo;
import com.example.domain.kintai.model.Kintai;
import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.ApplicationService;
import com.example.domain.kintai.service.CalendarService;
import com.example.domain.kintai.service.KintaiService;
import com.example.domain.kintai.service.UserService;
import com.example.form.KintaiForm;
import com.example.form.ValidGroup1;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/kintai")
public class KintaiController {

    private static final Logger logger = LoggerFactory.getLogger(KintaiController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private KintaiService kintaiService;

    @Autowired
    private ModelMapper modelMapper;


    /** 勤怠一覧画面表示 */
    @Autowired
    private ApplicationService applicationService;

    @GetMapping
    public String getKintai(@ModelAttribute KintaiForm form, Model model, HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser user = userService.getUserOne(currentUserName);

        // 年月を作成
        LocalDate today = LocalDate.now();
        String ym = today.getYear() + "-" + String.format("%02d", today.getMonthValue());

        // ステータスを取得
        String kintaiStatus = applicationService.getStatus(user.getUserId(), "KINTAI", ym);
        String keihiStatus = applicationService.getStatus(user.getUserId(), "KEIHI", ym);
        String koutsuhiStatus = applicationService.getStatus(user.getUserId(), "KOUTSUHI", ym);

        model.addAttribute("kintaiStatus", kintaiStatus);
        model.addAttribute("keihiStatus", keihiStatus);
        model.addAttribute("koutsuhiStatus", koutsuhiStatus);

        // セッション情報などは今のまま
        model.addAttribute("session", Map.of("role", "ROLE_GENERAL"));
        model.addAttribute("sessionUserId", user.getUserId());
        model.addAttribute("sessionUserName", user.getUserName());

        // カレンダー処理も今のまま
        List<DayInfo> calendarDays = calendarService.getCalendarDays(today.getYear(), today.getMonthValue());
        model.addAttribute("user", user);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("kintaiForm", form);
        session.setAttribute("userName", currentUserName);

        //試し
        model.addAttribute("role", "ROLE_GENERAL");
        
        return "kintai/kintai";
    }
    /** 勤怠情報送信処理 */
    @PostMapping
    public String postKintai(
        @Validated(ValidGroup1.class) @ModelAttribute KintaiForm form,
        BindingResult bindingResult,
        Model model,
        HttpSession session
    ){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        System.out.println("勤怠フォーム入力内容: " + form);

        // ★ バリデーションエラー時はここで止める
        if (bindingResult.hasErrors()) {

            MUser user = userService.getUserOne(currentUserName);
            LocalDate today = LocalDate.now();
            List<DayInfo> calendarDays = calendarService.getCalendarDays(
                today.getYear(), 
                today.getMonthValue()
            );

            model.addAttribute("user", user);
            model.addAttribute("calendarDays", calendarDays);
            model.addAttribute("kintaiForm", form); // ←入力値保持
            session.setAttribute("userName", currentUserName);
            model.addAttribute("sessionUserName", currentUserName);
            model.addAttribute("sessionDisplayName", user.getUserName());

            return "kintai/kintai";
        }

        // ★ エラーなければ保存
        Kintai kintai = modelMapper.map(form, Kintai.class);
        kintai.setUserId(currentUserName);

        try {
            kintaiService.insert(kintai);
        } catch (MyBatisSystemException e) {
            logger.error("MyBatisSystemException発生", e);
            Throwable rootCause = e.getCause();
            if (rootCause != null) {
                logger.error("根本原因: ", rootCause);
            }
            throw e;
        }

        // ★ 正常時画面再描画
        MUser user = userService.getUserOne(currentUserName);
        LocalDate today = LocalDate.now();
        List<DayInfo> calendarDays = calendarService.getCalendarDays(
            today.getYear(), 
            today.getMonthValue()
        );

        model.addAttribute("user", user);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("kintaiForm", new KintaiForm()); // ←リセット
        session.setAttribute("userName", currentUserName);
        model.addAttribute("sessionUserName", currentUserName);
        model.addAttribute("sessionDisplayName", user.getUserName());

        return "kintai/kintai";
    }
    /** カレンダーイベント取得 */
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getCalendarEvents(
            @RequestParam String start,
            @RequestParam String end) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        LocalDate startDate = LocalDate.parse(start.substring(0, 10));
        LocalDate endDate = LocalDate.parse(end.substring(0, 10));

        List<Kintai> kintaiList = kintaiService.getListByUserId(userId);

        List<Map<String, Object>> events = new ArrayList<>();

        for (Kintai k : kintaiList) {
            LocalDate workDate = k.getWorkDate();

            if (!workDate.isBefore(startDate) && workDate.isBefore(endDate)) {
                Map<String, Object> event = new HashMap<>();
                event.put("title", "✔ 登録済み");
                event.put("start", workDate.toString());
                event.put("color", "#28a745");
                events.add(event);
            }
        }

        return events;
    }
    
    @GetMapping("/api/data")
    @ResponseBody
    public KintaiForm getKintaiData(@RequestParam("date") String dateStr) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        LocalDate date = LocalDate.parse(dateStr); // yyyy-MM-dd 形式で来る前提

        Kintai kintai = kintaiService.selectOneByUserIdAndDate(userId, date);
        if (kintai == null) {
            return null;
        }

        return modelMapper.map(kintai, KintaiForm.class);
    }
    
    @PostMapping("/api/save")
    @ResponseBody
    public Map<String, Object> saveOrUpdateKintaiListAjax(
        @Validated(ValidGroup1.class) @RequestBody KintaiForm form,
        BindingResult bindingResult
    ){

        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        // ★ バリデーションエラー（改行対応）
        if (bindingResult.hasErrors()) {

            String errorMessage = bindingResult.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct() // 同じメッセージ重複防止
                .collect(java.util.stream.Collectors.joining("\n"));

            response.put("status", "error");
            response.put("message", errorMessage);
            return response;
        }

        try {
            Kintai kintai = modelMapper.map(form, Kintai.class);
            kintai.setUserId(currentUserName);
            kintai.setUpdatedBy(currentUserName);

            LocalDateTime now = LocalDateTime.now();      
            Timestamp timestamp = Timestamp.valueOf(now);
            kintai.setUpdatedAt(timestamp);

            // 既存チェック
            Kintai existing = kintaiService
                .selectOneByUserIdAndDate(currentUserName, kintai.getWorkDate());

            if (existing != null) {
                kintaiService.update(kintai);
            } else {
                kintaiService.insert(kintai);
            }

            response.put("status", "success");
            response.put("message", "勤怠情報を保存しました");

        } catch (Exception e) {
            logger.error("勤怠の保存処理でエラーが発生しました", e);
            response.put("status", "error");
            response.put("message", "保存に失敗しました");
        }

        return response;
    }
}
