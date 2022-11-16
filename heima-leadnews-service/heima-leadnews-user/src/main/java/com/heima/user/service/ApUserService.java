package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;

/**
 * ApUserService
 * @author liuhangfei
 */
public interface ApUserService extends IService<ApUser> {

    /**
     * 用户登录
     * @param dto 登录参数
     * @return ResponseResult
     */
    ResponseResult login(LoginDto dto);
}
