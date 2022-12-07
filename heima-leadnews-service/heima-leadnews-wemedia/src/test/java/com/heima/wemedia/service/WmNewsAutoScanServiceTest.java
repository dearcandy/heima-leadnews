package com.heima.wemedia.service;

import com.heima.apis.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class WmNewsAutoScanServiceTest {

    @Resource
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Resource
    private IArticleClient articleClient;
    @Test
    public void autoScanWmNews() {
        ArticleDto articleDto = new ArticleDto();
        ResponseResult responseResult = articleClient.saveArticle(articleDto);
        System.out.println(responseResult.getCode());
    }
}