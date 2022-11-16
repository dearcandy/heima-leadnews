package com.heima.model.user.dtos;

import lombok.Data;

/**
 * @author liuhangfei
 * @create 2022-11-14 00:13
 * @description 登录参数
 */
@Data
public class LoginDto {

    /**
     * 手机号
     */
    private String phone;
    /**
     * 密码
     */
    private String password;

}
