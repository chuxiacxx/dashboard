package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {

    Optional<SalesOrderLine> findByOrderNoAndOrderLineNo(String orderNo, String orderLineNo);

    List<SalesOrderLine> findByOrderNo(String orderNo);

    @Query("SELECT COALESCE(SUM(l.orderAmountTax), 0) FROM SalesOrderLine l WHERE l.orderNo = :orderNo")
    BigDecimal sumOrderAmountByOrderNo(@Param("orderNo") String orderNo);
}
