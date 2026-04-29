package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoVO {

    private List<String> buttons;
    private List<String> roles;
    private Long userId;
    private String userName;
    private String nickName;
    private String email;
    private String phone;
    private String gender;
    private String avatar;
}
