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
@Table(name = "stock_price", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_target_date", columnNames = {"stock_id", "targetDate"})
}) // 동일한 날짜에 같은 종목의 가격이 중복 저장되는 것을 방지
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_price_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private LocalDate targetDate; // 주가 일자

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal closePrice; // 종가 (정확한 금융 계산을 위해 BigDecimal 사용)

    @Column(nullable = false)
    private Long volume; // 거래량

    @Builder
    public StockPrice(Stock stock, LocalDate targetDate, BigDecimal closePrice, Long volume) {
        this.stock = stock;
        this.targetDate = targetDate;
        this.closePrice = closePrice;
        this.volume = volume;
    }
}