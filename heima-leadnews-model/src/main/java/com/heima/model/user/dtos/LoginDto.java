package com.heima.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value="手机号",required = true)
    private String phone;
    /**
     * 密码
     */
    @ApiModelProperty(value="密码",required = true)
    private String password;

}
