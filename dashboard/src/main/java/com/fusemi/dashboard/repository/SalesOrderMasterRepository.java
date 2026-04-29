package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesOrderMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalesOrderMasterRepository extends JpaRepository<SalesOrderMaster, Long> {

    Optional<SalesOrderMaster> findByOrderNo(String orderNo);

    @Query("SELECT COALESCE(SUM(l.orderAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '')")
    BigDecimal sumOrderAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(l.shippedAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate")
    BigDecimal sumShippedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(l.invoicedAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate")
    BigDecimal sumInvoicedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT o.orderNo) FROM SalesOrderMaster o WHERE o.orderCreateDate BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) as month, COALESCE(SUM(l.orderAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') GROUP BY FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) ORDER BY month")
    List<Object[]> sumAmountByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) as month, COUNT(DISTINCT o.orderNo) FROM SalesOrderMaster o WHERE o.orderCreateDate BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) ORDER BY month")
    List<Object[]> countOrdersByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) as month, COALESCE(SUM(l.shippedAmountTax), 0), COUNT(DISTINCT o.orderNo) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE_TRUNC', 'month', o.orderCreateDate) ORDER BY month")
    List<Object[]> sumShipmentByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT l.productFullName, COALESCE(SUM(l.orderQuantity), 0), COALESCE(SUM(l.orderAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') GROUP BY l.productFullName ORDER BY COALESCE(SUM(l.orderAmountTax), 0) DESC")
    List<Object[]> sumByProduct(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT o.salespersonName, COALESCE(SUM(l.orderAmountTax), 0), COALESCE(SUM(l.shippedAmountTax), 0) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') GROUP BY o.salespersonName ORDER BY COALESCE(SUM(l.orderAmountTax), 0) DESC")
    List<Object[]> sumBySalesperson(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT o.salesRegionDesc, COALESCE(SUM(l.orderAmountTax), 0), COUNT(DISTINCT o.orderNo) FROM SalesOrderMaster o JOIN SalesOrderLine l ON o.orderNo = l.orderNo WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') AND o.salesRegionDesc IS NOT NULL AND o.salesRegionDesc != '' GROUP BY o.salesRegionDesc ORDER BY COALESCE(SUM(l.orderAmountTax), 0) DESC")
    List<Object[]> sumByRegion(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT o.salespersonName FROM SalesOrderMaster o WHERE o.salespersonName IS NOT NULL AND o.salespersonName != '' ORDER BY o.salespersonName")
    List<String> findAllSalespersons();

    @Query("SELECT DISTINCT o.salesRegionDesc FROM SalesOrderMaster o WHERE o.salesRegionDesc IS NOT NULL AND o.salesRegionDesc != '' ORDER BY o.salesRegionDesc")
    List<String> findAllRegions();

    @Query(value = "SELECT DISTINCT EXTRACT(YEAR FROM o.order_create_date)::int FROM sales_order_master o WHERE o.order_create_date IS NOT NULL ORDER BY 1", nativeQuery = true)
    List<Integer> findAllYears();

    @Query("SELECT o FROM SalesOrderMaster o WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') ORDER BY o.orderCreateDate DESC")
    List<SalesOrderMaster> findRecentOrders(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT o FROM SalesOrderMaster o WHERE o.orderCreateDate BETWEEN :startDate AND :endDate AND (o.rejectReasonCode IS NULL OR o.rejectReasonCode = '') AND o.estimatedDeliveryDate IS NOT NULL AND o.estimatedDeliveryDate < CURRENT_DATE ORDER BY o.estimatedDeliveryDate ASC")
    List<SalesOrderMaster> findOverdueOrders(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
