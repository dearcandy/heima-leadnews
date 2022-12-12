package com.heima.article.listener;

import com.alibaba.fastjson.JSON;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WmNewsMessageConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class ArticleIsDownListener {

    @Resource
    ApArticleConfigService apArticleConfigService;

    @KafkaListener(topics = WmNewsMessageConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void onMessage(String message){
        log.info("消费文章上下架通知 message : {}", message);
        if (StringUtils.isNotBlank(message)){
            Map map = JSON.parseObject(message, Map.class);
            // 修改文章配置
            apArticleConfigService.updateByMap(map);
        }
    }
}
