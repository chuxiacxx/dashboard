package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesDataRepository extends JpaRepository<SalesData, Long> {

    List<SalesData> findBySaleDateBetween(LocalDate start, LocalDate end);

    List<SalesData> findByRegion(String region);

    @Query(value = "SELECT COALESCE(SUM(s.amount), 0) FROM sales_data s WHERE s.sale_date BETWEEN :start AND :end", nativeQuery = true)
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = "SELECT COUNT(DISTINCT s.customer_name) FROM sales_data s WHERE s.sale_date BETWEEN :start AND :end", nativeQuery = true)
    Long countDistinctCustomerByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
