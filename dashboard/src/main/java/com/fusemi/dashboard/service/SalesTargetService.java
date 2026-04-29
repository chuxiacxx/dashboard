package com.fusemi.dashboard.service;

import com.fusemi.dashboard.common.PageResult;
import com.fusemi.dashboard.entity.SalesTarget;
import com.fusemi.dashboard.repository.SalesOrderDetailRepository;
import com.fusemi.dashboard.repository.SalesOrderMasterRepository;
import com.fusemi.dashboard.repository.SalesTargetRepository;
import com.fusemi.dashboard.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 销售目标配置服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesTargetService {

    private final SalesTargetRepository salesTargetRepository;
    private final SalesOrderDetailRepository salesOrderDetailRepository;
    private final SalesOrderMasterRepository salesOrderMasterRepository;

    /**
     * 获取所有目标配置
     */
    public List<SalesTarget> getAllTargets(Integer year, Integer month) {
        if (year != null && month != null) {
            return salesTargetRepository.findByYearAndMonth(year, month);
        }
        if (year != null) {
            return salesTargetRepository.findByYearOrderByMonthAsc(year);
        }
        return salesTargetRepository.findAll(Sort.by(Sort.Direction.DESC, "year", "month"));
    }

    /**
     * 创建目标
     */
    public SalesTarget createTarget(SalesTarget target) {
        // 检查是否已存在
        boolean exists = salesTargetRepository.existsByYearAndMonthAndSalespersonAndRegion(
                target.getYear(), target.getMonth(), target.getSalesperson(), target.getRegion());
        if (exists) {
            throw new RuntimeException("该年月的目标配置已存在");
        }
        return salesTargetRepository.save(target);
    }

    /**
     * 更新目标
     */
    public SalesTarget updateTarget(Long id, SalesTarget target) {
        Optional<SalesTarget> existing = salesTargetRepository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("目标配置不存在");
        }
        SalesTarget old = existing.get();
        old.setTargetAmount(target.getTargetAmount());
        old.setCollectionTargetAmount(target.getCollectionTargetAmount());
        old.setOrderCountTarget(target.getOrderCountTarget());
        old.setRemark(target.getRemark());
        return salesTargetRepository.save(old);
    }

    /**
     * 删除目标
     */
    public void deleteTarget(Long id) {
        salesTargetRepository.deleteById(id);
    }

    /**
     * 批量创建或更新目标
     */
    public List<SalesTarget> batchCreateTargets(List<SalesTarget> targets) {
        List<SalesTarget> result = new ArrayList<>();
        for (SalesTarget target : targets) {
            Optional<SalesTarget> existing = salesTargetRepository
                    .findByYearAndMonthAndSalesperson(target.getYear(), target.getMonth(), target.getSalesperson());
            if (existing.isPresent()) {
                SalesTarget old = existing.get();
                old.setTargetAmount(target.getTargetAmount());
                old.setCollectionTargetAmount(target.getCollectionTargetAmount());
                old.setOrderCountTarget(target.getOrderCountTarget());
                old.setRemark(target.getRemark());
                result.add(salesTargetRepository.save(old));
            } else {
                result.add(salesTargetRepository.save(target));
            }
        }
        return result;
    }

    /**
     * 获取销售员业绩排行（含目标对比）
     */
    public List<SalesPersonVO> getSalespersonRanking(Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 查询实际业绩
        List<Object[]> actualData = salesOrderMasterRepository.sumBySalesperson(startDate, endDate);

        // 查询目标
        List<SalesTarget> targets = salesTargetRepository.findByYearAndMonth(year, month);

        List<SalesPersonVO> result = new ArrayList<>();
        int rank = 1;

        for (Object[] row : actualData) {
            String name = (String) row[0];
            BigDecimal actualAmount = (BigDecimal) row[1];
            BigDecimal shippedAmount = (BigDecimal) row[2];

            // 查找对应目标
            BigDecimal targetAmount = targets.stream()
                    .filter(t -> name.equals(t.getSalesperson()))
                    .findFirst()
                    .map(SalesTarget::getTargetAmount)
                    .orElse(BigDecimal.ZERO);

            SalesPersonVO vo = new SalesPersonVO();
            vo.setName(name);
            vo.setActualAmount(actualAmount);
            vo.setTargetAmount(targetAmount);
            vo.setShippedAmount(shippedAmount);

            // 计算完成率
            if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                vo.setCompletionRate(actualAmount
                        .divide(targetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP));
            } else {
                vo.setCompletionRate(BigDecimal.ZERO);
            }
            vo.setRank(rank++);

            result.add(vo);
        }

        return result;
    }

    /**
     * 获取月度目标达成情况
     */
    public List<SalesMonthlyVO> getMonthlyAchievement(Integer year) {
        List<SalesMonthlyVO> result = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            LocalDate startDate = LocalDate.of(year, month, 1);
            LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

            SalesMonthlyVO vo = new SalesMonthlyVO();
            vo.setMonth(String.format("%d-%02d", year, month));

            // 实际销售额
            BigDecimal actualAmount = salesOrderDetailRepository.sumOrderAmountByDateRange(startDate, endDate);
            vo.setSalesAmount(actualAmount != null ? actualAmount : BigDecimal.ZERO);

            // 订单数
            Long orderCount = salesOrderDetailRepository.countDistinctOrdersByDateRange(startDate, endDate);
            vo.setOrderCount(orderCount != null ? orderCount : 0L);

            // 发货金额
            BigDecimal shipmentAmount = salesOrderDetailRepository.sumShippedAmountByDateRange(startDate, endDate);
            vo.setShipmentAmount(shipmentAmount != null ? shipmentAmount : BigDecimal.ZERO);

            result.add(vo);
        }

        return result;
    }
}
