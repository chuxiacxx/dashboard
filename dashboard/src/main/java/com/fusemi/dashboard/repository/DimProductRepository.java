package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.DimProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DimProductRepository extends JpaRepository<DimProduct, Long> {
    Optional<DimProduct> findByProductCode(String productCode);
}
