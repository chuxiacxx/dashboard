package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.DimSalesperson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimSalespersonRepository extends JpaRepository<DimSalesperson, Long> {
    Optional<DimSalesperson> findBySalespersonCode(String salespersonCode);

    @Query("SELECT DISTINCT d.salespersonName FROM DimSalesperson d WHERE d.salespersonName IS NOT NULL AND d.salespersonName != '' ORDER BY d.salespersonName")
    List<String> findAllSalespersonNames();

    @Query("SELECT DISTINCT d.salesRegionDesc FROM DimSalesperson d WHERE d.salesRegionDesc IS NOT NULL AND d.salesRegionDesc != '' ORDER BY d.salesRegionDesc")
    List<String> findAllRegions();
}
