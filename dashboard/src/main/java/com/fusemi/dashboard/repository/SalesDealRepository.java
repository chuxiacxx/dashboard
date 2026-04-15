package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesDeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesDealRepository extends JpaRepository<SalesDeal, Long> {

    List<SalesDeal> findByDealDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM SalesDeal d WHERE d.dealDate BETWEEN :start AND :end")
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
