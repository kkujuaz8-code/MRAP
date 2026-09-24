package com.marketrisk.repository.stock;

import com.marketrisk.entity.stock.RiskMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskMetricRepository extends JpaRepository<RiskMetric, Long> {
}