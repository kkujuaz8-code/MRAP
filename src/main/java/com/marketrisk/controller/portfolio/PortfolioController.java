package com.marketrisk.controller.portfolio;

import com.marketrisk.dto.portfolio.PortfolioItemRequest;
import com.marketrisk.dto.portfolio.PortfolioSummaryResponse; 
import com.marketrisk.service.portfolio.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio/items") // 💡 기본 주소 설정
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // [1. 포트폴리오 주식 등록 (POST)]
    @PostMapping
    public ResponseEntity<String> addPortfolioItem(Authentication authentication, @RequestBody PortfolioItemRequest request) {
        String loginId = authentication.getName();
        String result = portfolioService.addPortfolioItem(loginId, request);
        return ResponseEntity.ok(result);
    }

    // [2. 포트폴리오 전체 조회 (GET)]
    @GetMapping
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioItems(Authentication authentication) {
        String loginId = authentication.getName();
        PortfolioSummaryResponse response = portfolioService.getPortfolioItems(loginId);
        return ResponseEntity.ok(response);
    }

    // [3. 포트폴리오 주식 삭제 (DELETE)]
    @DeleteMapping("/{itemId}")
    public ResponseEntity<String> deletePortfolioItem(Authentication authentication, @PathVariable("itemId") Long itemId) {
        String loginId = authentication.getName();
        String result = portfolioService.deletePortfolioItem(loginId, itemId);
        return ResponseEntity.ok(result);
    }

    // 💡 [4. 매도 API 엔드포인트 추가] -> 최종 주소: /api/portfolio/items/sell
    @PostMapping("/sell")
    public ResponseEntity<String> sellItem(@RequestBody java.util.Map<String, Object> requestData) {
        String loginId = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        
        String ticker = requestData.get("ticker").toString();
        int quantity = Integer.parseInt(requestData.get("quantity").toString());

        portfolioService.sellStock(loginId, ticker, quantity);
        return ResponseEntity.ok("매도 성공");
    }
}