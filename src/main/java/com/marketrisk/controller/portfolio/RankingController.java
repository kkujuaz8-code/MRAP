package com.marketrisk.controller.portfolio;

import com.marketrisk.service.portfolio.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveRanking() {
        return ResponseEntity.ok(rankingService.getRealActiveRanking());
    }

    @GetMapping("/rise")
    public ResponseEntity<List<Map<String, Object>>> getRiseRanking() {
        return ResponseEntity.ok(rankingService.getRealRiseRanking());
    }

    @GetMapping("/fall")
    public ResponseEntity<List<Map<String, Object>>> getFallRanking() {
        return ResponseEntity.ok(rankingService.getRealFallRanking());
    }
}
