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
