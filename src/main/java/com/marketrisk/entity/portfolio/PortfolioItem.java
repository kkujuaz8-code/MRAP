package com.marketrisk.entity.portfolio;

import com.marketrisk.common.entity.BaseEntity;
import com.marketrisk.entity.stock.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_item", uniqueConstraints = {
    @UniqueConstraint(name = "uk_portfolio_stock", columnNames = {"portfolio_id", "stock_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Integer quantity; // 보유 수량

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePrice; // 매수 평단가

    @Builder
    public PortfolioItem(Portfolio portfolio, Stock stock, Integer quantity, BigDecimal averagePrice) {
        this.portfolio = portfolio;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }

    // 💡 [새로 추가된 부분] 물타기(추가 매수) 시 수량과 평단가를 덮어쓰는 메서드
    public void updateItem(Integer addQuantity, BigDecimal newAveragePrice) {
        this.quantity += addQuantity; // 기존 수량에 새 수량을 더함
        this.averagePrice = newAveragePrice; // 새로 계산된 평단가로 덮어씀
    }
}