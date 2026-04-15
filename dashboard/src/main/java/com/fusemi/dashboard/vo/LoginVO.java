package com.fusemi.dashboard.vo;

import lombok.Data;
import java.util.List;

@Data
public class LoginVO {

    private String token;
    private String refreshToken;
}
