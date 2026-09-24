package com.marketrisk.entity.portfolio;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 영수증 고유 번호

    // 어떤 사용자의 거래 내역인지 연결 (기존 User 엔티티가 있다면 관계 매핑, 여기서는 심플하게 ID로 저장)
    @Column(nullable = false)
    private String userId; 

    @Column(nullable = false)
    private String ticker; // 종목명 (예: AAPL)

    @Column(nullable = false)
    private String tradeType; // 거래 종류 (BUY 또는 SELL)

    @Column(nullable = false)
    private int quantity; // 체결 수량

    @Column(nullable = false)
    private double price; // 체결 단가

    @Column(nullable = false)
    private LocalDateTime tradeDate; // 거래 일시

    // 저장되기 직전에 자동으로 현재 시간을 입력해주는 마법의 어노테이션
    @PrePersist
    protected void onCreate() {
        this.tradeDate = LocalDateTime.now();
    }
}