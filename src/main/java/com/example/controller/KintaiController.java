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
import com.example.domain.kintai.service.CalendarService;
import com.example.domain.kintai.service.KintaiService;
import com.example.domain.kintai.service.UserService;
import com.example.form.KintaiForm;

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
    @GetMapping
    public String getKintai(@ModelAttribute KintaiForm form, Model model, HttpSession session) {

    	//ログインユーザーネーム表示処理追加
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser user = userService.getUserOne(currentUserName);
      
 
        
     // userNameを画面に渡す
        model.addAttribute("sessionUserId", user.getUserId());


        

        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();
        List<DayInfo> calendarDays = calendarService.getCalendarDays(year, month);

        model.addAttribute("user", user);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("kintaiForm", form);
        session.setAttribute("userName", currentUserName);
       // model.addAttribute("sessionUserName", currentUserName);
     // userNameを画面に渡す
        model.addAttribute("sessionUserName", user.getUserName());
        model.addAttribute("sessionUserId", user.getUserId());


        return "kintai/kintai";
    }

    /** 勤怠情報送信処理 */
    @PostMapping
    public String postKintai(@ModelAttribute KintaiForm form, Model model, HttpSession session) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        System.out.println("勤怠フォーム入力内容: " + form);

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
            throw e; // 必要に応じて例外処理を変更
        }

        MUser user = userService.getUserOne(currentUserName);
        LocalDate today = LocalDate.now();
        List<DayInfo> calendarDays = calendarService.getCalendarDays(today.getYear(), today.getMonthValue());

        model.addAttribute("user", user);
        model.addAttribute("calendarDays", calendarDays);
        model.addAttribute("kintaiForm", new KintaiForm());
        session.setAttribute("userName", currentUserName);
        model.addAttribute("sessionUserName", currentUserName);
        model.addAttribute("sessionDisplayName", user.getUserName());
        
        return "kintai/kintai";
    }

    /** カレンダーイベント取得 */
    @GetMapping("/events")
    @ResponseBody
    public List<Map<String, Object>> getCalendarEvents() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        List<DayInfo> calendarDays = calendarService.getCalendarDays(year, month);

        List<Map<String, Object>> events = new ArrayList<>();
        for (DayInfo day : calendarDays) {
            Map<String, Object> event = new HashMap<>();
            event.put("title", day.getDayOfWeek());
            event.put("start", day.getDate().toString());
            events.add(event);
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
    public Map<String, Object> saveOrUpdateKintaiListAjax(@RequestBody KintaiForm form) {

        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        try {
          //  for (KintaiForm form : formList) {
                Kintai kintai = modelMapper.map(form, Kintai.class);
                kintai.setUserId(currentUserName);
                kintai.setUpdatedBy(currentUserName);
                
                LocalDateTime now = LocalDateTime.now();      
                Timestamp timestamp = Timestamp.valueOf(now);
              //  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
             //   String formatted = now.format(formatter);
                kintai.setUpdatedAt(timestamp);
                
                // 既存の勤怠データがあるかを確認して、更新 or 挿入
                Kintai existing = kintaiService.selectOneByUserIdAndDate(currentUserName, kintai.getWorkDate());
                if (existing != null) {
                    kintaiService.update(kintai);
                } else {
                    kintaiService.insert(kintai);
              //  }
            }

            response.put("status", "success");
            response.put("message", "勤怠情報を保存しました");

        } catch (Exception e) {
            logger.error("勤怠の一括保存処理でエラーが発生しました", e);
            response.put("status", "error");
            response.put("message", "保存に失敗しました: " + e.getMessage());
        }

        return response;
        
        
        
    }

    
    
//    @PostMapping("/api/save")
//    @ResponseBody
//    public Map<String, Object> saveOrUpdateKintaiAjax(@RequestBody KintaiForm form) {
//
//        Map<String, Object> response = new HashMap<>();
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String currentUserName = authentication.getName();
//
//        // form → entity に変換し、ログイン中のユーザーIDを設定
//        Kintai kintai = modelMapper.map(form, Kintai.class);
//        kintai.setUserId(currentUserName);
//
//        try {
//            // 既存データがあるか確認
//            Kintai existing = kintaiService.selectOneByUserIdAndDate(currentUserName, kintai.getWorkDate());
//
//            if (existing != null) {
//                // 存在すれば更新
//                kintaiService.update(kintai);
//                response.put("status", "success");
//                response.put("message", "勤怠情報を更新しました");
//            } else {
//                // なければ新規登録
//                kintaiService.insert(kintai);
//                response.put("status", "success");
//                response.put("message", "勤怠情報を新規登録しました");
//            }
//
//        } catch (Exception e) {
//            logger.error("勤怠の保存処理でエラーが発生しました", e);
//            response.put("status", "error");
//            response.put("message", "保存に失敗しました: " + e.getMessage());
//        }
//
//        return response;
//    }
}
