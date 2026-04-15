package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface SalesCustomerRepository extends JpaRepository<SalesCustomer, Long> {

    List<SalesCustomer> findByIsNew(Integer isNew);

    @Query(value = "SELECT COUNT(*) FROM sales_customer WHERE is_new = :isNew AND first_purchase_date BETWEEN :start AND :end", nativeQuery = true)
    Long countByIsNewAndDateRange(@Param("isNew") Integer isNew,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    List<SalesCustomer> findByChannel(String channel);
}
