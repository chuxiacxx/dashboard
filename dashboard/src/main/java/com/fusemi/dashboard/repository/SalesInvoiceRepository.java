package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {

    List<SalesInvoice> findByInvoiceDateBetween(LocalDate start, LocalDate end);

    @Query(value = "SELECT COALESCE(SUM(i.amount), 0) FROM sales_invoice i WHERE i.invoice_date BETWEEN :start AND :end", nativeQuery = true)
    BigDecimal sumAmountByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
