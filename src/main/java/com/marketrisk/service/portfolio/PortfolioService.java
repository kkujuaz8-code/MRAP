package com.marketrisk.service.portfolio;

import com.marketrisk.dto.portfolio.PortfolioItemRequest;
import com.marketrisk.dto.portfolio.PortfolioItemResponse;
import com.marketrisk.dto.portfolio.PortfolioSummaryResponse;
import com.marketrisk.entity.portfolio.Portfolio;
import com.marketrisk.entity.portfolio.PortfolioItem;
import com.marketrisk.entity.stock.Stock;
import com.marketrisk.entity.user.User;
import com.marketrisk.repository.portfolio.PortfolioItemRepository;
import com.marketrisk.repository.portfolio.PortfolioRepository;
import com.marketrisk.repository.stock.StockRepository;
import com.marketrisk.repository.user.UserRepository;
import com.marketrisk.service.stock.StockService; 
import com.marketrisk.service.portfolio.TradeHistoryService; 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j 
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioItemRepository portfolioItemRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    
    // 💡 1. 영수증 발급기(Service) 주입
    private final TradeHistoryService tradeHistoryService; 
    
    @Transactional
    public String addPortfolioItem(String loginId, PortfolioItemRequest request) {
        
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("주식 수량은 1주 이상이어야 합니다.");
        }
        if (request.getAveragePrice().doubleValue() < 0) {
            throw new IllegalArgumentException("평단가는 0보다 작을 수 없습니다.");
        }

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Portfolio portfolio = portfolioRepository.findByUser(user).stream().findFirst()
                .orElseGet(() -> portfolioRepository.save(
                        Portfolio.builder().user(user).name("나의 포트폴리오").build()
                ));

        Stock stock = stockRepository.findByTicker(request.getTicker())
                .orElseGet(() -> stockRepository.save(
                        Stock.builder().ticker(request.getTicker()).companyName(request.getTicker() + " Inc.").build()
                ));

        var existingItemOpt = portfolioItemRepository.findByPortfolioAndStock(portfolio, stock);

        if (existingItemOpt.isPresent()) {
            PortfolioItem existingItem = existingItemOpt.get();
            
            double oldQuantity = existingItem.getQuantity();
            double oldAvgPrice = existingItem.getAveragePrice().doubleValue();
            
            double addQuantity = request.getQuantity();
            double addAvgPrice = request.getAveragePrice().doubleValue();

            double totalOldValue = oldQuantity * oldAvgPrice;
            double totalAddValue = addQuantity * addAvgPrice;
            double newAvgPrice = (totalOldValue + totalAddValue) / (oldQuantity + addQuantity);

            newAvgPrice = Math.round(newAvgPrice * 10000.0) / 10000.0;

            existingItem.updateItem(request.getQuantity(), BigDecimal.valueOf(newAvgPrice));
            
            // 💡 2. [추가 매수] 성공 직후 영수증 발급
            tradeHistoryService.recordTrade(loginId, request.getTicker(), "BUY", request.getQuantity(), addAvgPrice);
            
            return request.getTicker() + " 종목이 추가 매수되어 평단가가 재계산되었습니다!";
            
        } else {
            PortfolioItem newItem = PortfolioItem.builder()
                    .portfolio(portfolio)
                    .stock(stock)
                    .quantity(request.getQuantity())
                    .averagePrice(request.getAveragePrice())
                    .build();

            portfolioItemRepository.save(newItem);
            
            // 💡 3. [신규 매수] 성공 직후 영수증 발급
            tradeHistoryService.recordTrade(loginId, request.getTicker(), "BUY", request.getQuantity(), request.getAveragePrice().doubleValue());
            
            return request.getTicker() + " 종목이 " + request.getQuantity() + "주 담겼습니다!";
        }
    }

    @Transactional
    public void sellStock(String loginId, String ticker, int sellQty) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Portfolio portfolio = portfolioRepository.findByUser(user).stream().findFirst()
                .orElseThrow(() -> new RuntimeException("등록된 포트폴리오가 없습니다."));

        Stock stock = stockRepository.findByTicker(ticker)
                .orElseThrow(() -> new RuntimeException("해당 티커의 주식 정보가 없습니다."));

        PortfolioItem item = portfolioItemRepository.findByPortfolioAndStock(portfolio, stock)
                .orElseThrow(() -> new RuntimeException("보유하지 않은 종목입니다."));

        if (item.getQuantity() < sellQty) {
            throw new RuntimeException("보유 수량보다 많이 매도할 수 없습니다.");
        } else if (item.getQuantity() == sellQty) {
            portfolioItemRepository.delete(item); 
        } else {
            item.updateItem(-sellQty, item.getAveragePrice());
        }

        // 💡 4. [매도 단가 구하기] 외부 API로 현재가 조회 (실패 시 기존 평단가로 대체)
        double sellPrice;
        try {
            sellPrice = stockService.getCurrentPriceValue(ticker);
        } catch (Exception e) {
            log.warn("{} 종목의 실시간 가격을 가져오는데 실패하여 평단가로 매도 기록을 남깁니다.", ticker);
            sellPrice = item.getAveragePrice().doubleValue();
        }

        // 💡 5. [매도] 성공 직후 영수증 발급
        tradeHistoryService.recordTrade(loginId, ticker, "SELL", sellQty, sellPrice);
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioItems(String loginId) {
        // ... (이하 기존 코드 완벽히 동일하여 생략, 기존 코드 그대로 유지) ...
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Portfolio portfolio = portfolioRepository.findByUser(user).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("등록된 포트폴리오가 없습니다."));

        List<PortfolioItem> items = portfolioItemRepository.findByPortfolio(portfolio);

        double totalInvestedAmount = 0.0;
        double totalPortfolioValue = 0.0;

        List<PortfolioItemResponse> itemResponses = items.stream()
                .map(item -> {
                    String ticker = item.getStock().getTicker();
                    Double currentPrice;
                    double avgPrice = item.getAveragePrice().doubleValue();
                    
                    try {
                        currentPrice = stockService.getCurrentPriceValue(ticker);
                    } catch (Exception e) {
                        log.warn("{} 종목의 실시간 가격을 가져오는데 실패했습니다. 임시로 평단가를 현재가로 대체합니다.", ticker);
                        currentPrice = avgPrice; 
                    }
                    
                    int quantity = item.getQuantity();
                    
                    Double itemTotalValue = currentPrice * quantity;
                    Double profitRate = 0.0;
                    if (avgPrice > 0) {
                        profitRate = ((currentPrice - avgPrice) / avgPrice) * 100.0;
                        profitRate = Math.round(profitRate * 100.0) / 100.0;
                    }
                    
                    return PortfolioItemResponse.builder()
                            .id(item.getId())
                            .ticker(ticker)
                            .companyName(item.getStock().getCompanyName())
                            .quantity(quantity)
                            .averagePrice(item.getAveragePrice())
                            .currentPrice(currentPrice)
                            .totalValue(itemTotalValue)
                            .profitRate(profitRate)
                            .build();
                })
                .collect(Collectors.toList());

        for (PortfolioItemResponse res : itemResponses) {
            totalInvestedAmount += res.getAveragePrice().doubleValue() * res.getQuantity();
            totalPortfolioValue += res.getTotalValue();
        }

        double totalProfitRate = 0.0;
        if (totalInvestedAmount > 0) {
            totalProfitRate = ((totalPortfolioValue - totalInvestedAmount) / totalInvestedAmount) * 100.0;
            totalProfitRate = Math.round(totalProfitRate * 100.0) / 100.0; 
        }

        return PortfolioSummaryResponse.builder()
                .totalInvestedAmount(totalInvestedAmount)
                .totalPortfolioValue(totalPortfolioValue)
                .totalProfitRate(totalProfitRate)
                .items(itemResponses) 
                .build();
    }

    @Transactional
    public String deletePortfolioItem(String loginId, Long itemId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Portfolio portfolio = portfolioRepository.findByUser(user).stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("등록된 포트폴리오가 없습니다."));

        PortfolioItem item = portfolioItemRepository.findByPortfolioAndId(portfolio, itemId)
                .orElseThrow(() -> new IllegalArgumentException("내 포트폴리오에 존재하지 않는 아이템입니다."));

        String ticker = item.getStock().getTicker();
        portfolioItemRepository.delete(item);

        return ticker + " 종목이 포트폴리오에서 완전히 삭제되었습니다.";
    }
}