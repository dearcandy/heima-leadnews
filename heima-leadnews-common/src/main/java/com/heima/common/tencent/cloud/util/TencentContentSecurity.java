package com.heima.common.tencent.cloud.util;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ims.v20200713.ImsClient;
import com.tencentcloudapi.ims.v20200713.models.ImageModerationRequest;
import com.tencentcloudapi.ims.v20200713.models.ImageModerationResponse;
import com.tencentcloudapi.tms.v20200713.TmsClient;
import com.tencentcloudapi.tms.v20200713.models.TextModerationRequest;
import com.tencentcloudapi.tms.v20200713.models.TextModerationResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.*;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "tencent-cloud")
public class TencentContentSecurity {
    private String secretId;
    private String secretKey;

    /**
     * 文本审核
     * @param content 文本内容
     * @return 审核结果
     */
    public Map<String, String> textModeration(String content) {
        Map<String, String> resultMap = new HashMap<>();
        try{
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("tms.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            TmsClient client = new TmsClient(cred, "ap-guangzhou", clientProfile);

            // 实例化一个请求对象,每个接口都会对应一个request对象
            TextModerationRequest req = new TextModerationRequest();
            String encodedContent  = Base64.getEncoder().encodeToString(content.getBytes());
            req.setContent(encodedContent);
            // 返回的resp是一个TextModerationResponse的实例，与请求对象对应
            TextModerationResponse resp = client.TextModeration(req);
            if (StringUtils.isNotBlank(resp.getSuggestion()) && StringUtils.isNotBlank(resp.getLabel())){
                resultMap.put("suggestion", resp.getSuggestion());
                resultMap.put("label", resp.getLabel());
            }
        }catch (TencentCloudSDKException exception){
            log.error("文本审核调用异常, exception : ", exception);
            throw new RuntimeException("文本审核调用异常");
        }
        return resultMap;
    }

    /**
     * 图片审核
     * @param fileContent 图片 内容
     * @return 审核结果
     */
    public Map<String, String>  imageModeration(String fileContent){
        Map<String, String> resultMap = new HashMap<>();
        try{
            Credential cred = new Credential(secretId, secretKey);
            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ims.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            ImsClient client = new ImsClient(cred, "ap-guangzhou", clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            ImageModerationRequest req = new ImageModerationRequest();
            req.setFileContent(fileContent);
            // 返回的resp是一个ImageModerationResponse的实例，与请求对象对应
            ImageModerationResponse resp = client.ImageModeration(req);
            // 输出json格式的字符串回包
            if (StringUtils.isNotBlank(resp.getSuggestion()) && StringUtils.isNotBlank(resp.getLabel())){
                resultMap.put("suggestion", resp.getSuggestion());
                resultMap.put("label", resp.getLabel());
            }
        } catch (TencentCloudSDKException exception) {
            log.error("图片审核调用异常, exception :", exception);
            throw new RuntimeException("图片审核调用异常");
        }
        return resultMap;
    }

}