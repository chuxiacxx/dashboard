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

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM SalesOrder o WHERE o.orderDate BETWEEN :start AND :end")
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE o.orderDate BETWEEN :start AND :end")
    Long countByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
