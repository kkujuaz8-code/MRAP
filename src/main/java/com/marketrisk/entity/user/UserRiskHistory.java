package com.marketrisk.entity.user;

import com.marketrisk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_risk_history", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_target_date", columnNames = {"user_id", "targetDate"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRiskHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate targetDate; // 점수 산출 일자

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal totalRiskScore; // 종합 리스크 점수 (0~100)

    @Column(precision = 5, scale = 2)
    private BigDecimal volatilityScore; // 변동성 기여 점수

    @Column(precision = 5, scale = 2)
    private BigDecimal varScore; // VaR 기여 점수

    @Column(precision = 5, scale = 2)
    private BigDecimal mddScore; // MDD 기여 점수

    @Builder
    public UserRiskHistory(User user, LocalDate targetDate, BigDecimal totalRiskScore, BigDecimal volatilityScore, BigDecimal varScore, BigDecimal mddScore) {
        this.user = user;
        this.targetDate = targetDate;
        this.totalRiskScore = totalRiskScore;
        this.volatilityScore = volatilityScore;
        this.varScore = varScore;
        this.mddScore = mddScore;
    }
}