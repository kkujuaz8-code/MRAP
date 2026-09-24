package com.marketrisk.repository.portfolio;

import com.marketrisk.entity.portfolio.Portfolio;
import com.marketrisk.entity.user.User; // 💡 User 객체 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // 💡 List 임포트

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    List<Portfolio> findByUser(User user);
}