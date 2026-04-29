package com.fusemi.dashboard.service.parser;

import com.fusemi.dashboard.dto.DataRow;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
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

            List<String[]> allLines;
            try {
                allLines = csvReader.readAll();
            } catch (CsvException e) {
                throw new IOException("CSV 解析失败: " + e.getMessage(), e);
            }
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
                    String value = (j < line.length && line[j] != null) ? line[j].trim() : "";
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
