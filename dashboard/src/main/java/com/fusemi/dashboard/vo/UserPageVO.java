package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserPageVO {
    private List<UserItemVO> records;
    private Long current;
    private Long size;
    private Long total;
}
