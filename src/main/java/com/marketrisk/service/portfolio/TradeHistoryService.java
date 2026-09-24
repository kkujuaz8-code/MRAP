package com.marketrisk.service.portfolio; // 본인 패키지명으로 수정!

import com.marketrisk.entity.portfolio.TradeHistory;
import com.marketrisk.repository.portfolio.TradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeHistoryService {

    private final TradeHistoryRepository tradeHistoryRepository;

    // 1. 거래 내역 한 줄 기록하기 (매수/매도 성공 시 호출할 메서드)
    @Transactional
    public void recordTrade(String userId, String ticker, String tradeType, int quantity, double price) {
        TradeHistory history = new TradeHistory();
        history.setUserId(userId);
        history.setTicker(ticker);
        history.setTradeType(tradeType); // "BUY" 또는 "SELL"
        history.setQuantity(quantity);
        history.setPrice(price);
        tradeHistoryRepository.save(history);
    }

    // 2. 내 거래 내역 전체 조회 (최신순)
    @Transactional(readOnly = true)
    public List<TradeHistory> getHistory(String userId) {
        return tradeHistoryRepository.findByUserIdOrderByTradeDateDesc(userId);
    }

    // 3. 테스트 기록 한 개만 삭제 🗑️
    @Transactional
    public void deleteHistory(Long id) {
        tradeHistoryRepository.deleteById(id);
    }

    // 4. 테스트 기록 전체 초기화 💣
    @Transactional
    public void clearAllHistory(String userId) {
        tradeHistoryRepository.deleteByUserId(userId);
    }
}