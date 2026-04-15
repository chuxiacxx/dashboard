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

    @Query("SELECT COALESCE(SUM(s.amount), 0) FROM SalesShipment s WHERE s.shipmentDate BETWEEN :start AND :end")
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
