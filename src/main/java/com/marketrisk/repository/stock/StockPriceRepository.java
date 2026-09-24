package com.marketrisk.repository.stock;

import com.marketrisk.entity.stock.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
}