package com.marketrisk.repository.portfolio;

import com.marketrisk.entity.portfolio.Portfolio;
import com.marketrisk.entity.stock.Stock;
import com.marketrisk.entity.portfolio.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PortfolioItemRepository extends JpaRepository<PortfolioItem, Long> {
    
    // 💡 1. 특정 포트폴리오 상자 안에 든 모든 주식 아이템들을 싹 꺼내오는 메서드 (조회용)
    List<PortfolioItem> findByPortfolio(Portfolio portfolio);

    // 💡 2. 내 포트폴리오 안에서 "특정 주식(ID)"만 쏙 찾아내는 메서드 (삭제 검증용)
    Optional<PortfolioItem> findByPortfolioAndId(Portfolio portfolio, Long id);
    Optional<PortfolioItem> findByPortfolioAndStock(Portfolio portfolio, Stock stock);
}