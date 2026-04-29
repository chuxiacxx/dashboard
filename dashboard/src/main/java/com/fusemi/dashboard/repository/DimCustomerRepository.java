package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.DimCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DimCustomerRepository extends JpaRepository<DimCustomer, Long> {
    Optional<DimCustomer> findByCustomerCode(String customerCode);
}
