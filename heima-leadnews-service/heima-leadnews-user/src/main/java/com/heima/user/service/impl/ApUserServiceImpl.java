package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.user.mapper.ApUserMapper;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.service.ApUserService;
import com.heima.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuhangfei
 * @create 2022-11-14 00:19
 * @description
 */
@Slf4j
@Service
@Transactional
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {


    /**
     * 用户登录
     *
     * @param dto 登录参数
     * @return ResponseResult
     */
    @Override
    public ResponseResult login(LoginDto dto) {
        // 正常登录
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())){
            // 查询用户
            LambdaQueryWrapper<ApUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApUser::getPhone, dto.getPhone());
            ApUser dbUser = getOne(wrapper);
            if (dbUser == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "用户信息不存在");
            }
            // 比对信息
            String pwd = DigestUtils.md5DigestAsHex((dto.getPassword() + dbUser.getSalt()).getBytes());
            if (!pwd.equals(dbUser.getPassword())){
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }
            // 返回jwt
            String token = AppJwtUtil.getToken(Long.valueOf(dbUser.getId()));
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setPassword(StringUtils.EMPTY);
            dbUser.setSalt(StringUtils.EMPTY);
            map.put("user", dbUser);

            return ResponseResult.okResult(map);
        }else {
            // 游客登录
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(1L));

            return ResponseResult.okResult(map);
        }
    }
}
