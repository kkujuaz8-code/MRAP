package com.marketrisk.dto.portfolio;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class PortfolioItemResponse {
    private Long id;               // 아이템 번호 (삭제할 때 필요함!)
    private String ticker;         // 종목 코드 (AAPL)
    private String companyName;    // 회사 이름 (Apple Inc.)
    private Integer quantity;      // 수량
    private BigDecimal averagePrice; // 평단가
    private Double currentPrice;	//실시간 현재
    private Double totalValue; // 총 평가 금액 (현재가 * 수량)
    private Double profitRate; // 수익률 (%)
}