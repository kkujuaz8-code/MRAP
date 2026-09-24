package com.marketrisk.entity.user;

import com.marketrisk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_risk_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRiskSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Integer volatilityWeight; // 변동성 가중치 (예: 40)

    @Column(nullable = false)
    private Integer varWeight; // VaR 가중치 (예: 30)

    @Column(nullable = false)
    private Integer mddWeight; // MDD 가중치 (예: 30)

    @Builder
    public UserRiskSetting(User user, Integer volatilityWeight, Integer varWeight, Integer mddWeight) {
        this.user = user;
        this.volatilityWeight = volatilityWeight;
        this.varWeight = varWeight;
        this.mddWeight = mddWeight;
    }
}