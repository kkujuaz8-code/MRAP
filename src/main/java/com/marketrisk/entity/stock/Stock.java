package com.marketrisk.entity.stock;

import com.marketrisk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker; // 예: AAPL, TSLA

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(length = 50)
    private String sector; // 섹터 (예: Technology)

    @Builder
    public Stock(String ticker, String companyName, String sector) {
        this.ticker = ticker;
        this.companyName = companyName;
        this.sector = sector;
    }
}