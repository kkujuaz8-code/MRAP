package com.marketrisk.repository.batch;

import com.marketrisk.entity.batch.BatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchHistoryRepository extends JpaRepository<BatchHistory, Long> {
}