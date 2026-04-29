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

    /** 新建记录数 */
    private int created;

    /** 更新记录数 */
    private int updated;
}
