package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 销售目标 Repository
 */
@Repository
public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long> {

    /**
     * 根据年月查询目标
     */
    List<SalesTarget> findByYearAndMonth(Integer year, Integer month);

    /**
     * 根据年月和销售员查询
     */
    Optional<SalesTarget> findByYearAndMonthAndSalesperson(Integer year, Integer month, String salesperson);

    /**
     * 查询指定年月的总目标（所有销售员汇总）
     */
    @Query("SELECT COALESCE(SUM(t.targetAmount), 0) FROM SalesTarget t WHERE t.year = :year AND t.month = :month")
    BigDecimal sumTargetAmountByYearMonth(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 查询指定年月和销售员的目标
     */
    @Query("SELECT COALESCE(SUM(t.targetAmount), 0) FROM SalesTarget t WHERE t.year = :year AND t.month = :month AND (t.salesperson = :salesperson OR :salesperson IS NULL)")
    BigDecimal sumTargetAmountByYearMonthAndSalesperson(@Param("year") Integer year, @Param("month") Integer month, @Param("salesperson") String salesperson);

    /**
     * 按年份查询所有目标
     */
    List<SalesTarget> findByYearOrderByMonthAsc(Integer year);

    /**
     * 按销售员查询目标
     */
    List<SalesTarget> findBySalespersonOrderByYearAscMonthAsc(String salesperson);

    /**
     * 查询是否存在
     */
    boolean existsByYearAndMonthAndSalespersonAndRegion(Integer year, Integer month, String salesperson, String region);
}
