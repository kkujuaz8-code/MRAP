package com.marketrisk.controller.portfolio;

import com.marketrisk.entity.portfolio.TradeHistory;
import com.marketrisk.service.portfolio.TradeHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class TradeHistoryController {

    private final TradeHistoryService tradeHistoryService;

    // 1. 거래 영수증 조회 API
    @GetMapping
    public ResponseEntity<List<TradeHistory>> getMyHistory(Principal principal) {
        // principal.getName()으로 로그인한 유저 ID를 가져옵니다.
        String userId = principal.getName(); 
        List<TradeHistory> historyList = tradeHistoryService.getHistory(userId);
        return ResponseEntity.ok(historyList);
    }

    // 2. 개별 기록 삭제 API
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecord(Authentication authentication, @PathVariable("id") Long id) {
        tradeHistoryService.deleteHistory(id);
        return ResponseEntity.ok().build();
    }

    // 3. 전체 기록 초기화 API
    @DeleteMapping("/all")
    public ResponseEntity<Void> clearAllRecords(Principal principal) {
        String userId = principal.getName();
        tradeHistoryService.clearAllHistory(userId);
        return ResponseEntity.ok().build();
    }
}