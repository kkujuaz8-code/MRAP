package com.marketrisk.entity.stock;

import com.marketrisk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "risk_metric", uniqueConstraints = {
    @UniqueConstraint(name = "uk_metric_stock_date", columnNames = {"stock_id", "baseDate"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiskMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "risk_metric_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate baseDate; // 지표 산출 기준일

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal volatility; // 변동성 (표준편차)

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal valueAtRisk; // VaR (예: 95% 신뢰수준)

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal maxDrawdown; // MDD (최대 낙폭)

    @Column(nullable = false)
    private Integer windowDays; // 계산에 사용된 과거 데이터 일수 (예: 30)

    @Builder
    public RiskMetric(Stock stock, LocalDate baseDate, BigDecimal volatility, BigDecimal valueAtRisk, BigDecimal maxDrawdown, Integer windowDays) {
        this.stock = stock;
        this.baseDate = baseDate;
        this.volatility = volatility;
        this.valueAtRisk = valueAtRisk;
        this.maxDrawdown = maxDrawdown;
        this.windowDays = windowDays;
    }
}