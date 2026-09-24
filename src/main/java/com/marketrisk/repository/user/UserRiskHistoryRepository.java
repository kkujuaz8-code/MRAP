package com.marketrisk.repository.user;

import com.marketrisk.entity.user.UserRiskHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRiskHistoryRepository extends JpaRepository<UserRiskHistory, Long> {
}