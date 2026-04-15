package com.fusemi.dashboard.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "用户名不能为空")
    private String userName;

    private String nickName;

    @Email(message = "邮箱格式不正确")
    private String userEmail;

    private String userPhone;

    private String userGender;

    private String avatar;

    /** 0=正常, 1=禁用 */
    private String status;

    private Set<Long> roleIds;

    private String password;
}
