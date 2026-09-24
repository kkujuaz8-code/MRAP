package com.marketrisk.scheduler;

import com.marketrisk.entity.user.User;
import com.marketrisk.repository.user.UserRepository;
import com.marketrisk.service.portfolio.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j // 💡 콘솔창에 예쁘게 로그를 찍기 위한 녀석
@Component // 💡 스프링에게 이 당직자를 관리하라고 등록
@RequiredArgsConstructor
public class PortfolioScheduler {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;

    // 💡 크론(Cron) 표현식: 초 분 시 일 월 요일
    // "0/10 * * * * *" -> 10초마다 무조건 실행! 
    // (나중에 실서비스 배포 시 "0 0 0 * * *" 로 바꾸면 매일 자정 1회 실행됨)
    @Scheduled(cron = "0/10 * * * * *")
    public void dailyPortfolioCheck() {
        log.info("=== 🌙 [자동화 당직자] 전체 유저 자산 결산을 시작합니다 ===");
        
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                // 아까 만들어둔 '전체 합산 계산기'를 당직자가 몰래 호출합니다!
                var summary = portfolioService.getPortfolioItems(user.getLoginId());
                
                log.info(" ➔ [{}]님의 현재 총 자산: {} USD, 총 수익률: {}%",
                        user.getLoginId(), summary.getTotalPortfolioValue(), summary.getTotalProfitRate());
                
            } catch (Exception e) {
                log.warn(" ➔ [{}]님의 포트폴리오 결산 중 문제 발생: {}", user.getLoginId(), e.getMessage());
            }
        }
        
        log.info("=== 🌙 [자동화 당직자] 결산 완료 ===");
    }
}