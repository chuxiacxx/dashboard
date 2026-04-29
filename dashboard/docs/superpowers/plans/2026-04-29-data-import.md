# 数据导入功能 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现前后端数据导入功能：前端页面支持上传 Excel/CSV 文件并显示导入结果；后端接收文件，解析 Excel/CSV 数据并保存到数据库。

**Architecture:** 后端使用 Apache POI 解析 Excel、OpenCSV 解析 CSV，通过策略模式按数据类型路由到不同的解析器，统一保存到数据库。前端复用现有页面，增强结果展示和模板下载功能。

**Tech Stack:** Spring Boot 4.0 + JPA + PostgreSQL, Apache POI (Excel), OpenCSV (CSV), Vue 3 + Element Plus + TypeScript

---

## File Structure

### 后端 (Backend)

| File | Action | Responsibility |
|------|--------|----------------|
| `pom.xml` | Modify | 添加 Apache POI + OpenCSV 依赖 |
| `controller/DataImportController.java` | Modify | 接收上传文件，调用解析服务，返回导入结果 |
| `service/DataImportService.java` | Create | 主导入服务：文件分发、结果汇总 |
| `service/parser/DataParser.java` | Create | 解析器接口 |
| `service/parser/ExcelParser.java` | Create | Excel 文件解析（Apache POI） |
| `service/parser/CsvParser.java` | Create | CSV 文件解析（OpenCSV） |
| `service/parser/SalesDataRowMapper.java` | Create | sales_data 表的数据行映射 |
| `dto/ImportResult.java` | Create | 导入结果 DTO（成功数、失败数、错误信息） |
| `dto/DataRow.java` | Create | 通用数据行封装（表头 + 数据列表） |
| `repository/SalesDataRepository.java` | Modify | 添加批量保存方法（如需要） |

### 前端 (Frontend)

| File | Action | Responsibility |
|------|--------|----------------|
| `views/system/data-import/index.vue` | Modify | 增强导入结果展示、添加模板下载 |
| `api/dashboard.ts` | Modify | 添加模板下载 API |

---

## Task 1: 添加后端依赖 (Apache POI + OpenCSV)

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: 在 pom.xml 中添加 Apache POI 和 OpenCSV 依赖**

在 `<dependencies>` 节点内，validation dependency 之后、test dependencies 之前插入：

```xml
        <!-- Apache POI for Excel parsing -->
        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
            <version>5.3.0</version>
        </dependency>

        <!-- OpenCSV for CSV parsing -->
        <dependency>
            <groupId>com.opencsv</groupId>
            <artifactId>opencsv</artifactId>
            <version>5.9</version>
        </dependency>
```

- [ ] **Step 2: 验证依赖**

Run: `mvn dependency:tree | grep -E "poi|opencsv"`
Expected: 显示 `org.apache.poi:poi-ooxml:5.3.0` 和 `com.opencsv:opencsv:5.9`

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "deps: add Apache POI and OpenCSV for data import"
```

---

## Task 2: 创建导入结果 DTO

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/dto/ImportResult.java`

- [ ] **Step 1: 创建 ImportResult DTO**

```java
package com.fusemi.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportResult {

    /** 导入总数 */
    private int total;

    /** 成功数 */
    private int success;

    /** 失败数 */
    private int failed;

    /** 错误信息列表 */
    private List<String> errors;

    /** 数据类型 */
    private String dataType;

    /** 文件名 */
    private String fileName;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/dto/ImportResult.java
git commit -m "feat: add ImportResult DTO for data import"
```

---

## Task 3: 创建通用数据行封装 DTO

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/dto/DataRow.java`

- [ ] **Step 1: 创建 DataRow DTO**

```java
package com.fusemi.dashboard.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DataRow {

    /** 表头（列名） */
    private List<String> headers;

    /** 数据行，每行是一个 列名->值 的 Map */
    private List<Map<String, String>> rows;

    /** 总行数（不含表头） */
    private int rowCount;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/dto/DataRow.java
git commit -m "feat: add DataRow DTO for parsed spreadsheet data"
```

---

## Task 4: 创建解析器接口

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/service/parser/DataParser.java`

- [ ] **Step 1: 创建 DataParser 接口**

```java
package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.dto.DataRow;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DataParser {

    /**
     * 解析上传的文件，返回结构化数据
     *
     * @param file 上传的文件
     * @return 解析后的数据行
     * @throws IOException 读取文件失败
     */
    DataRow parse(MultipartFile file) throws IOException;

    /**
     * 该解析器支持的文件扩展名
     *
     * @return 扩展名，如 ".xlsx"
     */
    String getExtension();
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/service/parser/DataParser.java
git commit -m "feat: add DataParser interface for file parsing"
```

---

## Task 5: 创建 Excel 解析器

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/service/parser/ExcelParser.java`

- [ ] **Step 1: 创建 ExcelParser**

```java
package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.dto.DataRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class ExcelParser implements DataParser {

    @Override
    public DataRow parse(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        boolean isXlsx = filename != null && filename.toLowerCase().endsWith(".xlsx");

        try (InputStream is = file.getInputStream();
             Workbook workbook = isXlsx ? new XSSFWorkbook(is) : new HSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (!rowIterator.hasNext()) {
                return DataRow.builder()
                        .headers(Collections.emptyList())
                        .rows(Collections.emptyList())
                        .rowCount(0)
                        .build();
            }

            // 读取表头（第一行）
            Row headerRow = rowIterator.next();
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell).trim());
            }

            // 读取数据行
            List<Map<String, String>> rows = new ArrayList<>();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                // 跳过空行
                if (isEmptyRow(row, headers.size())) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String value = cell != null ? getCellValue(cell).trim() : "";
                    rowData.put(headers.get(i), value);
                }
                rows.add(rowData);
            }

            return DataRow.builder()
                    .headers(headers)
                    .rows(rows)
                    .rowCount(rows.size())
                    .build();
        }
    }

    @Override
    public String getExtension() {
        return ".xlsx";
    }

    private String getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                double num = cell.getNumericCellValue();
                if (num == Math.floor(num)) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private boolean isEmptyRow(Row row, int expectedColumns) {
        for (int i = 0; i < expectedColumns; i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String value = getCellValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/service/parser/ExcelParser.java
git commit -m "feat: add Excel parser using Apache POI"
```

---

## Task 6: 创建 CSV 解析器

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/service/parser/CsvParser.java`

- [ ] **Step 1: 创建 CsvParser**

```java
package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.dto.DataRow;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class CsvParser implements DataParser {

    @Override
    public DataRow parse(MultipartFile file) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReaderBuilder(reader).build()) {

            List<String[]> allLines = csvReader.readAll();
            if (allLines.isEmpty()) {
                return DataRow.builder()
                        .headers(Collections.emptyList())
                        .rows(Collections.emptyList())
                        .rowCount(0)
                        .build();
            }

            // 第一行是表头
            String[] headerArray = allLines.get(0);
            List<String> headers = new ArrayList<>();
            for (String h : headerArray) {
                headers.add(h != null ? h.trim() : "");
            }

            // 后续行是数据
            List<Map<String, String>> rows = new ArrayList<>();
            for (int i = 1; i < allLines.size(); i++) {
                String[] line = allLines.get(i);
                // 跳过空行
                if (isEmptyLine(line)) {
                    continue;
                }

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    String value = (j < line.length() && line[j] != null) ? line[j].trim() : "";
                    rowData.put(headers.get(j), value);
                }
                rows.add(rowData);
            }

            return DataRow.builder()
                    .headers(headers)
                    .rows(rows)
                    .rowCount(rows.size())
                    .build();
        }
    }

    @Override
    public String getExtension() {
        return ".csv";
    }

    private boolean isEmptyLine(String[] line) {
        if (line == null || line.length == 0) {
            return true;
        }
        for (String cell : line) {
            if (cell != null && !cell.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/service/parser/CsvParser.java
git commit -m "feat: add CSV parser using OpenCSV"
```

---

## Task 7: 创建 SalesData 行映射器

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/service/parser/SalesDataRowMapper.java`

- [ ] **Step 1: 创建 SalesDataRowMapper**

将 Excel/CSV 解析后的 Map 转换为 SalesData 实体。

根据 `SalesData` 实体字段：
- `saleDate`: LocalDate
- `region`: String
- `salesperson`: String
- `productName`: String
- `amount`: BigDecimal
- `quantity`: Integer
- `unitPrice`: BigDecimal
- `customerName`: String
- `orderNo`: String
- `status`: String

```java
package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.entity.SalesData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Component
public class SalesDataRowMapper {

    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
    };

    public SalesData map(Map<String, String> row) {
        SalesData data = new SalesData();

        data.setSaleDate(parseDate(getValue(row, "saleDate", "日期", "销售日期")));
        data.setRegion(getValue(row, "region", "地区", "区域"));
        data.setSalesperson(getValue(row, "salesperson", "销售员", "销售人员"));
        data.setProductName(getValue(row, "productName", "产品名称", "产品", "商品"));
        data.setAmount(parseBigDecimal(getValue(row, "amount", "金额", "销售额")));
        data.setQuantity(parseInt(getValue(row, "quantity", "数量", "销量")));
        data.setUnitPrice(parseBigDecimal(getValue(row, "unitPrice", "单价", "单位价格")));
        data.setCustomerName(getValue(row, "customerName", "客户名称", "客户", "客户名"));
        data.setOrderNo(getValue(row, "orderNo", "订单号", "订单编号"));
        data.setStatus(getValue(row, "status", "状态"));

        return data;
    }

    /**
     * 尝试多个可能的列名获取值
     */
    private String getValue(Map<String, String> row, String... possibleKeys) {
        for (String key : possibleKeys) {
            if (row.containsKey(key)) {
                return row.get(key);
            }
        }
        return "";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/service/parser/SalesDataRowMapper.java
git commit -m "feat: add SalesData row mapper for Excel/CSV import"
```

---

## Task 8: 创建 DataImportService

**Files:**
- Create: `src/main/java/com/fusemi/dashboard/service/DataImportService.java`
- Modify: `src/main/java/com/fusemi/dashboard/repository/SalesDataRepository.java` (确认存在)

- [ ] **Step 1: 确认 SalesDataRepository 存在**

File: `src/main/java/com/fusemi/dashboard/repository/SalesDataRepository.java`

如果不存在，创建：

```java
package com.fusemi.dashboard.repository;

import com.fusemi.dashboard.entity.SalesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesDataRepository extends JpaRepository<SalesData, Long> {
}
```

- [ ] **Step 2: 创建 DataImportService**

```java
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
```

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/service/DataImportService.java
git add src/main/java/com/fusemi/dashboard/repository/SalesDataRepository.java
git commit -m "feat: add DataImportService with sales data import support"
```

---

## Task 9: 修改 DataImportController

**Files:**
- Modify: `src/main/java/com/fusemi/dashboard/controller/DataImportController.java`

- [ ] **Step 1: 重写 DataImportController，接入 DataImportService**

```java
package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.ImportResult;
import com.fusemi.dashboard.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataImportController {

    private final DataImportService dataImportService;

    /**
     * 上传数据文件（Excel/CSV）并导入数据库
     * 仅允许 R_SUPER 和 R_ADMIN 访问
     */
    @PostMapping("/data/import")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<ImportResult> importData(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String dataType) {

        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return Result.fail("文件名不能为空");
        }

        // 检查文件类型
        String lowerName = filename.toLowerCase();
        if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls") && !lowerName.endsWith(".csv")) {
            return Result.fail("仅支持 .xlsx, .xls, .csv 格式的文件");
        }

        // 检查文件大小（最大 10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.fail("文件大小不能超过 10MB");
        }

        // 执行导入
        ImportResult result = dataImportService.importData(file, dataType);

        if (result.getSuccess() > 0 && result.getErrors().isEmpty()) {
            return Result.ok("导入成功，共导入 " + result.getSuccess() + " 条数据", result);
        } else if (result.getSuccess() > 0) {
            return Result.ok("部分导入成功，成功 " + result.getSuccess() + " 条，失败 " + result.getFailed() + " 条", result);
        } else {
            return Result.fail("导入失败: " + String.join("; ", result.getErrors()));
        }
    }

    /**
     * 获取支持导入的数据类型列表
     * 仅允许 R_SUPER 和 R_ADMIN 访问
     */
    @GetMapping("/data/import/types")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public Result<List<Map<String, String>>> getImportTypes() {
        List<Map<String, String>> types = new ArrayList<>();

        types.add(createType("sales", "销售数据", ".xlsx, .xls, .csv"));
        types.add(createType("order", "订单数据", ".xlsx, .xls, .csv"));
        types.add(createType("shipment", "发货数据", ".xlsx, .xls, .csv"));
        types.add(createType("invoice", "发票数据", ".xlsx, .xls, .csv"));
        types.add(createType("deal", "成交数据", ".xlsx, .xls, .csv"));
        types.add(createType("customer", "客户数据", ".xlsx, .xls, .csv"));

        return Result.ok(types);
    }

    private Map<String, String> createType(String type, String name, String extensions) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("name", name);
        map.put("extensions", extensions);
        return map;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/controller/DataImportController.java
git commit -m "feat: wire DataImportController to DataImportService"
```

---

## Task 10: 添加模板下载 API（后端）

**Files:**
- Modify: `src/main/java/com/fusemi/dashboard/controller/DataImportController.java`

- [ ] **Step 1: 在 DataImportController 中添加模板下载端点**

在 `DataImportController` 中添加：

```java
    /**
     * 下载导入模板
     * 仅允许 R_SUPER 和 R_ADMIN 访问
     */
    @GetMapping("/data/import/template/{type}")
    @PreAuthorize("hasAnyRole('R_SUPER', 'R_ADMIN')")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String type) {
        byte[] content = generateTemplate(type);
        String filename = type + "_template.csv";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", filename);
        // 添加 BOM 以支持 Excel 正确识别 UTF-8
        byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] fullContent = new byte[bom.length + content.length];
        System.arraycopy(bom, 0, fullContent, 0, bom.length);
        System.arraycopy(content, 0, fullContent, bom.length, content.length);

        return new ResponseEntity<>(fullContent, headers, HttpStatus.OK);
    }

    private byte[] generateTemplate(String type) {
        String template = switch (type) {
            case "sales" -> "saleDate,region,salesperson,productName,amount,quantity,unitPrice,customerName,orderNo,status\n" +
                    "2024-01-15,华东,张三,产品A,10000,10,1000,客户A,ORD001,已完成\n";
            case "order" -> "orderDate,customerName,productName,quantity,amount,status\n" +
                    "2024-01-15,客户A,产品A,10,10000,待处理\n";
            case "shipment" -> "shipDate,orderNo,customerName,productName,quantity,trackingNo,status\n" +
                    "2024-01-16,ORD001,客户A,产品A,10,SF123456,已发货\n";
            case "invoice" -> "invoiceDate,orderNo,customerName,amount,taxAmount,totalAmount,status\n" +
                    "2024-01-17,ORD001,客户A,10000,1300,11300,已开票\n";
            case "deal" -> "dealDate,customerName,productName,amount,salesperson,status\n" +
                    "2024-01-15,客户A,产品A,10000,张三,已成交\n";
            case "customer" -> "name,phone,email,channel,isNew,firstPurchaseDate,repeatPurchaseCount,level\n" +
                    "客户A,13800138000,test@example.com,线上,1,2024-01-01,1,普通\n";
            default -> "column1,column2,column3\n";
        };
        return template.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
```

同时需要在文件顶部添加 import：

```java
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/fusemi/dashboard/controller/DataImportController.java
git commit -m "feat: add CSV template download endpoint"
```

---

## Task 11: 前端添加模板下载 API

**Files:**
- Modify: `dashboard-web/src/api/dashboard.ts`

- [ ] **Step 1: 在 dashboard.ts 中添加模板下载函数**

在 `fetchImportData` 函数之后添加：

```typescript
/**
 * 下载导入模板
 * @param dataType 数据类型
 */
export function fetchImportTemplate(dataType: string) {
  return api.request({
    url: `/api/data/import/template/${dataType}`,
    method: 'GET',
    responseType: 'blob',
    raw: true
  })
}
```

- [ ] **Step 2: Commit**

```bash
git add dashboard-web/src/api/dashboard.ts
git commit -m "feat: add template download API"
```

---

## Task 12: 前端增强数据导入页面

**Files:**
- Modify: `dashboard-web/src/views/system/data-import/index.vue`

- [ ] **Step 1: 修改 script setup 部分**

将 `import { fetchImportTypes, fetchImportData } from '@/api/dashboard'` 改为：

```typescript
import { fetchImportTypes, fetchImportData, fetchImportTemplate } from '@/api/dashboard'
```

将 `downloadTemplate` 函数替换为：

```typescript
// 下载模板
const downloadTemplate = async (type: string) => {
  try {
    const res: any = await fetchImportTemplate(type)
    const blob = new Blob([res], { type: 'text/csv;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${type}_template.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败')
  }
}
```

将 `importResult` 的类型和展示增强。修改 `importResult` ref 定义：

```typescript
const importResult = ref<{
  code: number
  message: string
  data?: {
    total: number
    success: number
    failed: number
    errors: string[]
  }
} | null>(null)
```

修改 `handleImport` 中结果处理：

```typescript
// 开始导入
const handleImport = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  uploading.value = true
  importResult.value = null

  try {
    const res: any = await fetchImportData(selectedFile.value, selectedType.value)
    importResult.value = {
      code: res.code,
      message: res.message || (res.code === 200 ? '导入成功' : '导入失败'),
      data: res.data
    }

    if (res.code === 200) {
      ElMessage.success(res.message || '导入成功')
    } else {
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error: any) {
    importResult.value = {
      code: 500,
      message: error?.message || '导入失败'
    }
    ElMessage.error('导入失败')
  } finally {
    uploading.value = false
  }
}
```

- [ ] **Step 2: 修改 template 中导入结果展示**

将导入结果 `<div v-if="importResult" class="mt-6">` 区域替换为：

```vue
        <!-- 导入结果 -->
        <div v-if="importResult" class="mt-6">
          <ElAlert
            :type="importResult.code === 200 ? 'success' : 'error'"
            :title="importResult.code === 200 ? '导入完成' : '导入失败'"
            :description="importResult.message"
            show-icon
          />
          <div v-if="importResult.data" class="mt-3 p-4 bg-gray-50 rounded-lg">
            <div class="flex gap-6 text-sm">
              <span>总记录: <strong>{{ importResult.data.total }}</strong></span>
              <span class="text-green-600">成功: <strong>{{ importResult.data.success }}</strong></span>
              <span class="text-red-600">失败: <strong>{{ importResult.data.failed }}</strong></span>
            </div>
            <div v-if="importResult.data.errors && importResult.data.errors.length > 0" class="mt-3">
              <p class="text-sm font-medium text-red-600 mb-1">错误详情:</p>
              <ul class="text-sm text-red-500 space-y-1 max-h-40 overflow-y-auto">
                <li v-for="(err, idx) in importResult.data.errors" :key="idx">{{ err }}</li>
              </ul>
            </div>
          </div>
        </div>
```

- [ ] **Step 3: Commit**

```bash
git add dashboard-web/src/views/system/data-import/index.vue
git add dashboard-web/src/api/dashboard.ts
git commit -m "feat: enhance data import page with result details and template download"
```

---

## Task 13: 编译并测试

- [ ] **Step 1: 后端编译**

Run: `mvn clean compile -DskipTests`
Expected: BUILD SUCCESS

- [ ] **Step 2: 启动后端**

Run: `mvn spring-boot:run` 或在 IDE 中启动
Expected: 应用正常启动，无 Schema-validation 错误

- [ ] **Step 3: 测试导入 API**

准备一个 test_sales.csv 文件：

```csv
saleDate,region,salesperson,productName,amount,quantity,unitPrice,customerName,orderNo,status
2024-01-15,华东,张三,产品A,10000,10,1000,客户A,ORD001,已完成
2024-01-16,华北,李四,产品B,20000,20,1000,客户B,ORD002,已完成
```

使用 curl 或 Postman 测试：

```bash
curl -X POST http://localhost:8080/api/data/import \
  -H "Authorization: Bearer <your_jwt_token>" \
  -F "file=@test_sales.csv" \
  -F "type=sales"
```

Expected: 返回 code=200，data.success=2

- [ ] **Step 4: 测试模板下载**

```bash
curl -O -J http://localhost:8080/api/data/import/template/sales \
  -H "Authorization: Bearer <your_jwt_token>"
```

Expected: 下载 sales_template.csv 文件

- [ ] **Step 5: 前端测试**

启动前端 dev server，登录后访问数据导入页面，测试：
1. 选择"销售数据"
2. 上传 test_sales.csv
3. 点击"开始导入"
4. 查看导入结果（成功数、失败数）
5. 点击"下载销售数据模板"

- [ ] **Step 6: Commit**

```bash
git commit -m "feat: complete data import feature with Excel/CSV parsing"
```

---

## Self-Review

**1. Spec coverage:**
- ✅ 前端页面已有数据导入页面，增强结果展示和模板下载
- ✅ 后端接收文件并解析 Excel/CSV
- ✅ 解析后的数据保存到数据库（sales 类型已实现，其他类型预留扩展点）
- ✅ 导入结果返回（成功数、失败数、错误信息）
- ✅ 模板下载功能

**2. Placeholder scan:**
- ✅ 无 TBD/TODO
- ✅ 所有代码完整
- ✅ 测试步骤明确

**3. Type consistency:**
- ✅ `ImportResult` 在 Service 和 Controller 中一致使用
- ✅ `DataRow` 在 Parser 和 Service 中一致使用
- ✅ 前端 `importResult` 类型与后端返回结构匹配

---

## 后续扩展（不在本计划范围内）

1. **其他数据类型**：为 `order`, `shipment`, `invoice`, `deal`, `customer` 添加对应的 `RowMapper` 和 `processXxxData` 方法
2. **批量插入优化**：使用 JDBC batch insert 替代 JPA `saveAll`
3. **异步导入**：大文件使用 Spring `@Async` 异步处理
4. **导入历史记录**：创建 `ImportRecord` 实体记录每次导入
5. **数据去重**：导入前检查重复数据
