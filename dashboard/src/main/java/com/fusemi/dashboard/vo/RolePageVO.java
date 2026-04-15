package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class RolePageVO {
    private List<RoleItemVO> records;
    private Long current;
    private Long size;
    private Long total;
}
