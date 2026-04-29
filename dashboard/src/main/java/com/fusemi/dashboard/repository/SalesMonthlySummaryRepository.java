package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesMonthlySummaryRepository extends JpaRepository<SalesMonthlySummary, Long> {

    Optional<SalesMonthlySummary> findByYearAndMonth(Integer year, Integer month);

    List<SalesMonthlySummary> findByYearOrderByMonth(Integer year);

    @Query("SELECT COALESCE(SUM(s.salesAmount), 0) FROM SalesMonthlySummary s WHERE s.year = :year AND s.month BETWEEN :startMonth AND :endMonth")
    BigDecimal sumSalesAmountByYearAndMonthRange(@Param("year") Integer year, @Param("startMonth") Integer startMonth, @Param("endMonth") Integer endMonth);

    @Query("SELECT DISTINCT s.year FROM SalesMonthlySummary s ORDER BY s.year")
    List<Integer> findAllYears();
}
