package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.thread.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Resource
    private FileStorageService fileStorageService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        String fileId = null;
        // 检查参数
        if (multipartFile == null || multipartFile.getSize() == 0){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 获取图片名称
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 上传图片到minIO
        String fileName = UUID.randomUUID().toString().replace("-", "");
        try {
            fileId = fileStorageService.uploadImgFile("",fileName + suffix, multipartFile.getInputStream());
            log.info("上传图片到MinIO中, fileId : {}", fileId);
        } catch (IOException e) {
            log.error("WmMaterialServiceImpl 上传图片失败, exception : ", e);
        }
        // 保存url到数据库
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUserId(WmThreadLocalUtils.getUser().getId());
        wmMaterial.setUrl(fileId);
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());

        save(wmMaterial);

        return ResponseResult.okResult(wmMaterial);
    }
}
