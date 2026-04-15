package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoVO {

    private List<String> buttons;
    private List<String> roles;
    private Long userId;
    private String userName;
    private String email;
    private String avatar;
}
