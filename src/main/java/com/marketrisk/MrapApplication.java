package com.marketrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling; // 💡 스케줄러 임포트 추가!

@EnableScheduling // 💡 [추가됨] 당직자(스케줄러) 알람 시계 켜기!
@EnableJpaAuditing // JPA Auditing 기능 활성화 (필수)
@SpringBootApplication
public class MrapApplication {

    public static void main(String[] args) {
        SpringApplication.run(MrapApplication.class, args);
    }
}