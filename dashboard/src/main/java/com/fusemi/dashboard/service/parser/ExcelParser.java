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
