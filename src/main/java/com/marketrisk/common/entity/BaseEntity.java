package com.marketrisk.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 실제 테이블로 매핑되지 않고, 자식 엔티티에게 컬럼 정보만 제공합니다.
@EntityListeners(AuditingEntityListener.class) // 시간에 대한 자동 감시 및 기록 수행
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false, nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
}