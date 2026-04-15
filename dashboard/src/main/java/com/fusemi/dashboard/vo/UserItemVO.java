package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserItemVO {
    private Long id;
    private String avatar;
    private String status;
    private String userName;
    private String userGender;
    private String nickName;
    private String userPhone;
    private String userEmail;
    private List<String> userRoles;
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
}
