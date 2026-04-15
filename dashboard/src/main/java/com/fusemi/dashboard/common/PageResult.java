package com.fusemi.dashboard.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {

    private List<T> records;
    private Long current;
    private Long size;
    private Long total;

    public PageResult() {}

    public PageResult(List<T> records, Long current, Long size, Long total) {
        this.records = records;
        this.current = current;
        this.size = size;
        this.total = total;
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(records, 1L, (long) records.size(), total);
    }

    public static <T> PageResult<T> of(Long total, List<T> records, Long current, Long size) {
        return new PageResult<>(records, current, size, total);
    }
}
