package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    List<SalesOrder> findByOrderDateBetween(LocalDate start, LocalDate end);

    List<SalesOrder> findBySalesperson(String salesperson);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM sales_order WHERE order_date BETWEEN :start AND :end", nativeQuery = true)
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query(value = "SELECT COUNT(*) FROM sales_order WHERE order_date BETWEEN :start AND :end", nativeQuery = true)
    Long countByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
