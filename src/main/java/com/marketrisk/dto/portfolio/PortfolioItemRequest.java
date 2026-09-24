package com.marketrisk.dto.portfolio;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class PortfolioItemRequest {
    private String ticker;             // 종목 코드 (예: AAPL)
    private Integer quantity;          // 수량 (예: 10)
    private BigDecimal averagePrice;   // 평단가 (예: 150.50)
}