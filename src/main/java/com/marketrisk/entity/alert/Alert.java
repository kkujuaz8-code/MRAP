package com.marketrisk.entity.alert;

import com.marketrisk.common.entity.BaseEntity;
import com.marketrisk.entity.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alert")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private AlertType type; // 알림 타입

    @Column(nullable = false, length = 100)
    private String title; // 알림 제목 (예: "[위험] 애플 리스크 초과")

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 알림 상세 내용

    @Column(nullable = false)
    private boolean isRead = false; // 읽음 여부 (기본값 false)

    public enum AlertType {
        HIGH_RISK, API_ERROR, SYSTEM
    }

    @Builder
    public Alert(User user, AlertType type, String title, String content) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.content = content;
        this.isRead = false;
    }
    
    // 알림 읽음 처리 비즈니스 메서드
    public void markAsRead() {
        this.isRead = true;
    }
}