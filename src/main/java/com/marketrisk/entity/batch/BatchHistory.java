package com.marketrisk.entity.batch;

import com.marketrisk.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "batch_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BatchHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "batch_history_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String jobName; // 예: DailyRiskAnalysisJob

    @Column(length = 100)
    private String stepName; // 예: Step1_CalculateMetric

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BatchStatus status; // 성공, 실패 여부

    @Column(nullable = false)
    private LocalDateTime startTime; // 시작 시간

    private LocalDateTime endTime; // 종료 시간

    @Column(columnDefinition = "TEXT")
    private String errorMessage; // 실패 시 에러 로그

    public enum BatchStatus {
        RUNNING, SUCCESS, FAIL
    }

    @Builder
    public BatchHistory(String jobName, String stepName, BatchStatus status, LocalDateTime startTime, LocalDateTime endTime, String errorMessage) {
        this.jobName = jobName;
        this.stepName = stepName;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.errorMessage = errorMessage;
    }
    
    // 배치 완료 시 상태 업데이트 메서드
    public void completeBatch(BatchStatus status, LocalDateTime endTime, String errorMessage) {
        this.status = status;
        this.endTime = endTime;
        this.errorMessage = errorMessage;
    }
}