package com.marketrisk.repository.alert;

import com.marketrisk.entity.alert.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}