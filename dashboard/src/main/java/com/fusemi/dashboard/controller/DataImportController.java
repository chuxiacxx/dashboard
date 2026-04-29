package com.fusemi.dashboard.controller;

import com.fusemi.dashboard.common.Result;
import com.fusemi.dashboard.dto.ImportResult;
import com.fusemi.dashboard.service.DataImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
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
        return template.getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, String> createType(String type, String name, String extensions) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("type", type);
        map.put("name", name);
        map.put("extensions", extensions);
        return map;
    }
}
