package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.CustomerMonthlySummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CustomerMonthlySummaryRepository extends JpaRepository<CustomerMonthlySummary, Long> {

    List<CustomerMonthlySummary> findByYearAndMonth(Integer year, Integer month);

    @Modifying
    @Query("DELETE FROM CustomerMonthlySummary c WHERE c.year = :year AND c.month = :month")
    int deleteByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT c.customerCode, c.customerName, COALESCE(SUM(c.salesAmount), 0), COALESCE(SUM(c.orderCount), 0), c.region FROM CustomerMonthlySummary c WHERE c.year = :year AND c.month = :month GROUP BY c.customerCode, c.customerName, c.region ORDER BY SUM(c.salesAmount) DESC")
    List<Object[]> findTopCustomers(@Param("year") Integer year, @Param("month") Integer month, Pageable pageable);

    @Query("SELECT COALESCE(SUM(c.salesAmount), 0) FROM CustomerMonthlySummary c WHERE c.year = :year AND c.month = :month")
    BigDecimal sumSalesAmountByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);

    @Query("SELECT COUNT(DISTINCT c.customerCode) FROM CustomerMonthlySummary c WHERE c.year = :year AND c.month = :month")
    Long countCustomersByYearAndMonth(@Param("year") Integer year, @Param("month") Integer month);
}
