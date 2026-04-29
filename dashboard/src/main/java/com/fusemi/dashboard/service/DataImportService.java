package com.fusemi.dashboard.service;

import com.fusemi.dashboard.dto.DataRow;
import com.fusemi.dashboard.dto.ImportResult;
import com.fusemi.dashboard.entity.SalesData;
import com.fusemi.dashboard.repository.SalesDataRepository;
import com.fusemi.dashboard.service.parser.CsvParser;
import com.fusemi.dashboard.service.parser.DataParser;
import com.fusemi.dashboard.service.parser.ExcelParser;
import com.fusemi.dashboard.service.parser.SalesDataRowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataImportService {

    private final ExcelParser excelParser;
    private final CsvParser csvParser;
    private final SalesDataRowMapper salesDataRowMapper;
    private final SalesDataRepository salesDataRepository;

    @Transactional
    public ImportResult importData(MultipartFile file, String dataType) {
        String filename = file.getOriginalFilename();
        log.info("开始导入数据: type={}, file={}", dataType, filename);

        // 选择解析器
        DataParser parser = selectParser(filename);
        if (parser == null) {
            return ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("不支持的文件格式，仅支持 .xlsx, .xls, .csv"))
                    .build();
        }

        // 解析文件
        DataRow dataRow;
        try {
            dataRow = parser.parse(file);
        } catch (IOException e) {
            log.error("文件解析失败", e);
            return ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("文件解析失败: " + e.getMessage()))
                    .build();
        }

        // 按数据类型处理
        return switch (dataType) {
            case "sales" -> processSalesData(dataRow, filename);
            default -> ImportResult.builder()
                    .dataType(dataType)
                    .fileName(filename)
                    .total(0)
                    .success(0)
                    .failed(0)
                    .errors(List.of("暂不支持的数据类型: " + dataType))
                    .build();
        };
    }

    private DataParser selectParser(String filename) {
        if (filename == null) {
            return null;
        }
        String lower = filename.toLowerCase();
        if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            return excelParser;
        }
        if (lower.endsWith(".csv")) {
            return csvParser;
        }
        return null;
    }

    private ImportResult processSalesData(DataRow dataRow, String filename) {
        List<String> errors = new ArrayList<>();
        List<SalesData> toSave = new ArrayList<>();

        int rowNum = 1; // 从1开始计数（不含表头）
        for (Map<String, String> row : dataRow.getRows()) {
            try {
                SalesData data = salesDataRowMapper.map(row);
                // 基本校验
                if (data.getSaleDate() == null && data.getAmount() == null && data.getProductName() == null) {
                    errors.add("第 " + rowNum + " 行: 数据为空或格式不正确");
                    rowNum++;
                    continue;
                }
                toSave.add(data);
            } catch (Exception e) {
                errors.add("第 " + rowNum + " 行: " + e.getMessage());
            }
            rowNum++;
        }

        // 批量保存
        if (!toSave.isEmpty()) {
            salesDataRepository.saveAll(toSave);
        }

        return ImportResult.builder()
                .dataType("sales")
                .fileName(filename)
                .total(dataRow.getRowCount())
                .success(toSave.size())
                .failed(dataRow.getRowCount() - toSave.size())
                .errors(errors)
                .build();
    }
}
