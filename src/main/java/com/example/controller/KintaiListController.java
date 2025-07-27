package com.example.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.domain.kintai.model.Kintai;
import com.example.domain.kintai.model.MUser;
import com.example.domain.kintai.service.KintaiService;
import com.example.domain.kintai.service.UserService;
import com.example.dto.KintaiDto;
import com.example.form.KeihiForm;

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
    private ModelMapper modelMapper;


    @GetMapping("/kintaiList/{userId:.+}")
    public String showKintaiList(@ModelAttribute("KeihiForm") KeihiForm keihiForm,
                                 @PathVariable("userId") String userId,
                                 Model model,
                                 HttpSession session) {

        // DBから勤怠データ取得
        List<Kintai> kintaiList = kintaiService.getListByUserId(userId);

        // ユーザー情報を取得
        MUser targetUser = userService.getUserOne(userId);
        String userName = (targetUser != null) ? targetUser.getUserName() : "不明なユーザー";

        // Entity → DTO に変換
        List<KintaiDto> kintaiDtoList = kintaiList.stream()
            .map(this::convertToDto)
            .peek(dto -> dto.setUserName(userName))
            .collect(Collectors.toList());

        model.addAttribute("kintaiList", kintaiDtoList);

        // ログインユーザーの情報を取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        MUser loginUser = userService.getUserOne(currentUserName);

        model.addAttribute("sessionUserName", loginUser.getUserName());
        model.addAttribute("sessionUserId", loginUser.getUserId());

        // ★ セッションにログインユーザーのIDをセット（未設定の場合のみ）
        if (session.getAttribute("userId") == null) {
            session.setAttribute("userId", loginUser.getUserId());
        }

        // 表示対象のユーザー名も渡す
        model.addAttribute("targetUserName", userName);
        model.addAttribute("targetUserId", userId);

        return "kintai/kintaiList";
    }

    /**
     * Entity → DTO の変換ロジック
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private KintaiDto convertToDto(Kintai entity) {
        KintaiDto dto = new KintaiDto();
        dto.setUserId(entity.getUserId());
        dto.setUpdatedBy(entity.getUpdatedBy());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setWorkDate(entity.getWorkDate());
        
        dto.setPlannedWorkStartTime(entity.getPlannedWorkStartTime());
        dto.setPlannedWorkEndTime(entity.getPlannedWorkEndTime());

        // ここに休憩時間のセットを追加
        dto.setPlannedBreakStartTime(entity.getPlannedBreakStartTime());
        dto.setPlannedBreakEndTime(entity.getPlannedBreakEndTime());

        dto.setActualWorkStartTime(entity.getActualWorkStartTime());
        dto.setActualWorkEndTime(entity.getActualWorkEndTime());

        // 実休憩時間も追加
        dto.setActualBreakStartTime(entity.getActualBreakStartTime());
        dto.setActualBreakEndTime(entity.getActualBreakEndTime());

        dto.setScheduledWorkHours(entity.getScheduledWorkHours());
        dto.setActualWorkHours(entity.getActualWorkHours());
        dto.setOvertimeHours(entity.getOvertimeHours());
        dto.setDeductionTime(entity.getDeductionTime());
        dto.setKintaiStatus(entity.getKintaiStatus());
        dto.setKintaiComment(entity.getKintaiComment());
        return dto;
    }
    
    @PostMapping("/api/save-list/{userId}")
    @ResponseBody
    public Map<String, Object> saveOrUpdateKintaiListAjax(@RequestBody List<KintaiDto> dtoList
    		,@PathVariable("userId") String userId
    		,Model model) {

        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        try {
            for (KintaiDto dto : dtoList) {
                Kintai kintai = modelMapper.map(dto, Kintai.class);
                kintai.setUserId(userId);
                LocalDateTime now = LocalDateTime.now();      
                Timestamp timestamp = Timestamp.valueOf(now);
                kintai.setUpdatedAt(timestamp);
                

                // 既存の勤怠データがあるかを確認して、更新 or 挿入
                //currentUserNameが問題　ユーザー１のユーザーID取得したい
                Kintai existing = kintaiService.selectOneByUserIdAndDate(dto.getUserId(), kintai.getWorkDate());
                if (existing != null) {
                    kintaiService.update(kintai);
                } else {
                    kintaiService.insert(kintai);
                }
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


    
//    @GetMapping("/entry")
//    public String entry(@PathVariable("userId") String userId, Model model, HttpSession session) {
//    	List<Kintai> kintaiList = kintaiService.getListByUserId(userId);
//        model.addAttribute("kintaiList", kintaiList);
//        return "entry"; // entry.html
//    }
}