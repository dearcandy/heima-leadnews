package com.heima.wemedia;

import com.heima.common.tencent.cloud.util.TencentContentSecurity;
import com.heima.file.service.FileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Base64;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TencentContentSecurityTest {
    @Resource
    TencentContentSecurity tencentContentSecurity;
    @Resource
    FileStorageService fileStorageService;

    @Test
    public void textModerationTest(){
        Map<String, String> resultMap = tencentContentSecurity.textModeration("做爱加微信");
        System.out.println(resultMap);

    }

    @Test
    public void imageModerationTest(){
        byte[] bytes = fileStorageService.downLoadFile("http://121.4.65.89:9000/leadnews/2022/11/30/40ef58af67fb4fdd955e652cde093c12.jpg");
        // 图片字节数组加密
        String fileContent = Base64.getEncoder().encodeToString(bytes);
        Map<String, String> resultMap = tencentContentSecurity.imageModeration(fileContent);
        System.out.println(resultMap);
    }
}
