package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.Set;

@Data
public class UserVO {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private String avatar;
    private Integer status;
    private Set<String> roles;
    private Set<String> permissions;
}
