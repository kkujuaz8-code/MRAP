package com.marketrisk.repository.portfolio;

import com.marketrisk.entity.portfolio.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    
    // 특정 사용자의 거래 내역을 최신순(시간 역순)으로 가져오는 메서드
    List<TradeHistory> findByUserIdOrderByTradeDateDesc(String userId);
    
    // 💡 [전체 초기화 버튼용] 특정 사용자의 모든 거래 내역 싹 지우기
    void deleteByUserId(String userId);
}