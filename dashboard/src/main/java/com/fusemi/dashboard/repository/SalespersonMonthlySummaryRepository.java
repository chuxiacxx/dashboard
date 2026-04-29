package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalespersonMonthlySummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalespersonMonthlySummaryRepository extends JpaRepository<SalespersonMonthlySummary, Long> {

    Optional<SalespersonMonthlySummary> findByYearAndMonthAndSalespersonCode(Integer year, Integer month, String salespersonCode);

    List<SalespersonMonthlySummary> findByYearAndMonthOrderBySalesAmountDesc(Integer year, Integer month);

    @Modifying
    @Query("DELETE FROM SalespersonMonthlySummary s WHERE s.year = :year AND s.month = :month")
    int deleteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(s.salesAmount), 0) FROM SalespersonMonthlySummary s WHERE s.year = :year AND s.month = :month")
    BigDecimal sumSalesAmountByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT s.salesRegionDesc, COALESCE(SUM(s.salesAmount), 0) FROM SalespersonMonthlySummary s WHERE s.year = :year AND s.month = :month AND s.salesRegionDesc IS NOT NULL AND s.salesRegionDesc != '' GROUP BY s.salesRegionDesc")
    List<Object[]> sumByRegion(@Param("year") Integer year, @Param("month") Integer month);
}
