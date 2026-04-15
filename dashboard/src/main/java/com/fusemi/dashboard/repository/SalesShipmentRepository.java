package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesShipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesShipmentRepository extends JpaRepository<SalesShipment, Long> {

    List<SalesShipment> findByShipmentDateBetween(LocalDate start, LocalDate end);

    @Query(value = "SELECT COALESCE(SUM(s.amount), 0) FROM sales_shipment s WHERE s.shipment_date BETWEEN :start AND :end", nativeQuery = true)
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
