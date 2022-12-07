package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.model.article.pojos.ApArticle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ApArticleServiceImplTest {

    @Resource
    ApArticleMapper apArticleMapper;

    @Test
    public void saveArticle() {
        ApArticle apArticle = apArticleMapper.selectOne(Wrappers.<ApArticle>lambdaQuery().eq(ApArticle::getId, 1600442043891585025L));
        System.out.println(apArticle);

    }
}