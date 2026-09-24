package com.marketrisk.dto.portfolio;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PortfolioSummaryResponse {
    // 1. 포트폴리오 전체 요약 데이터
    private Double totalInvestedAmount; // 총 투자 원금 (내가 산 돈의 합)
    private Double totalPortfolioValue; // 총 평가 금액 (현재가치 합)
    private Double totalProfitRate;     // 전체 총 수익률 (%)
    
    // 2. 개별 주식들의 상세 리스트
    private List<PortfolioItemResponse> items; 
}