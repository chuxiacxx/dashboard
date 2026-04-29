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
