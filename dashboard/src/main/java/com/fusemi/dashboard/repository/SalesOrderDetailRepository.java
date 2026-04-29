package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 销售订单明细 Repository
 */
@Repository
public interface SalesOrderDetailRepository extends JpaRepository<SalesOrderDetail, Long> {

    /**
     * 根据订单编号和行号查询
     */
    Optional<SalesOrderDetail> findByOrderNoAndOrderLineNo(String orderNo, String orderLineNo);

    /**
     * 根据订单编号查询所有行
     */
    List<SalesOrderDetail> findByOrderNo(String orderNo);

    /**
     * 查询指定日期范围内的订单金额总和（含税，排除有拒绝原因的）
     */
    @Query("SELECT COALESCE(SUM(s.orderAmountTax), 0) FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '')")
    BigDecimal sumOrderAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内的已发货金额总和
     */
    @Query("SELECT COALESCE(SUM(s.shippedAmountTax), 0) FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate")
    BigDecimal sumShippedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内的已开票金额总和
     */
    @Query("SELECT COALESCE(SUM(s.invoicedAmountTax), 0) FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate")
    BigDecimal sumInvoicedAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内的订单数量（按订单编号去重）
     */
    @Query("SELECT COUNT(DISTINCT s.orderNo) FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate")
    Long countDistinctOrdersByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询指定日期范围内的订单行数
     */
    @Query("SELECT COUNT(s) FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate")
    Long countOrderLinesByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按月份统计销售额
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) as month, COALESCE(SUM(s.orderAmountTax), 0) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '') " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) ORDER BY month")
    List<Object[]> sumAmountByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按月份统计订单数（按订单编号去重）
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) as month, COUNT(DISTINCT s.orderNo) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) ORDER BY month")
    List<Object[]> countOrdersByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按产品统计销量（TOP N）
     */
    @Query("SELECT s.productFullName, COALESCE(SUM(s.orderQuantity), 0), COALESCE(SUM(s.orderAmountTax), 0) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '') " +
           "GROUP BY s.productFullName ORDER BY COALESCE(SUM(s.orderAmountTax), 0) DESC")
    List<Object[]> sumByProduct(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按销售员统计销售额
     */
    @Query("SELECT s.salespersonName, COALESCE(SUM(s.orderAmountTax), 0), COALESCE(SUM(s.shippedAmountTax), 0) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '') " +
           "GROUP BY s.salespersonName ORDER BY COALESCE(SUM(s.orderAmountTax), 0) DESC")
    List<Object[]> sumBySalesperson(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按地区统计销售额
     */
    @Query("SELECT s.salesRegionDesc, COALESCE(SUM(s.orderAmountTax), 0), COUNT(DISTINCT s.orderNo) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '') " +
           "GROUP BY s.salesRegionDesc ORDER BY COALESCE(SUM(s.orderAmountTax), 0) DESC")
    List<Object[]> sumByRegion(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 按月统计发货数据
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) as month, COALESCE(SUM(s.shippedAmountTax), 0), COUNT(DISTINCT s.orderNo) " +
           "FROM SalesOrderDetail s WHERE s.orderCreateDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_TRUNC', 'month', s.orderCreateDate) ORDER BY month")
    List<Object[]> sumShipmentByMonth(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计本月与上月的数据（用于环比计算）
     */
    @Query("SELECT COALESCE(SUM(s.orderAmountTax), 0) FROM SalesOrderDetail s " +
           "WHERE s.orderCreateDate >= :startDate AND s.orderCreateDate <= :endDate " +
           "AND (s.rejectReasonCode IS NULL OR s.rejectReasonCode = '')")
    BigDecimal sumAmountForComparison(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 获取所有不重复的销售员姓名
     */
    @Query("SELECT DISTINCT s.salespersonName FROM SalesOrderDetail s WHERE s.salespersonName IS NOT NULL AND s.salespersonName != '' ORDER BY s.salespersonName")
    List<String> findAllSalespersons();

    /**
     * 获取所有不重复的销售地区
     */
    @Query("SELECT DISTINCT s.salesRegionDesc FROM SalesOrderDetail s WHERE s.salesRegionDesc IS NOT NULL AND s.salesRegionDesc != '' ORDER BY s.salesRegionDesc")
    List<String> findAllRegions();

    /**
     * 获取所有不重复的年份
     */
    @Query(value = "SELECT DISTINCT EXTRACT(YEAR FROM s.order_create_date)::int FROM sales_order_detail s WHERE s.order_create_date IS NOT NULL ORDER BY 1", nativeQuery = true)
    List<Integer> findAllYears();
}
