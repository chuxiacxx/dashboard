package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.ProductMonthlySummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductMonthlySummaryRepository extends JpaRepository<ProductMonthlySummary, Long> {

    List<ProductMonthlySummary> findByYearAndMonth(Integer year, Integer month);

    @Modifying
    @Query("DELETE FROM ProductMonthlySummary p WHERE p.year = :year AND p.month = :month")
    int deleteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT p.productCode, p.productName, p.productLine, COALESCE(SUM(p.salesQuantity), 0), COALESCE(SUM(p.salesAmount), 0) FROM ProductMonthlySummary p WHERE p.year = :year AND p.month = :month GROUP BY p.productCode, p.productName, p.productLine ORDER BY SUM(p.salesAmount) DESC")
    List<Object[]> findTopProducts(@Param("year") Integer year, @Param("month") Integer month, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.salesAmount), 0) FROM ProductMonthlySummary p WHERE p.year = :year AND p.month = :month")
    BigDecimal sumSalesAmountByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT p.productLine, COALESCE(SUM(p.salesAmount), 0) FROM ProductMonthlySummary p WHERE p.year = :year AND p.month = :month GROUP BY p.productLine")
    List<Object[]> sumByProductLine(@Param("year") Integer year, @Param("month") Integer month);
}
