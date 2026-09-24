package com.marketrisk.repository.user;

import com.marketrisk.entity.user.UserRiskSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRiskSettingRepository extends JpaRepository<UserRiskSetting, Long> {
}