package com.heima.article;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ArticleFreemarkerTest {

    @Resource
    private Configuration configuration;
    @Resource
    private FileStorageService fileStorageService;
    @Resource
    private ApArticleMapper apArticleMapper;
    @Resource
    private ApArticleContentMapper apArticleContentMapper;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {
        // 获取文章内容
        LambdaQueryWrapper<ApArticleContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApArticleContent::getArticleId, 1302862387124125698L);
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(wrapper);

        if (apArticleContent != null && StringUtils.isNotBlank(apArticleContent.getContent())){

            StringWriter out = new StringWriter();
            // 文章内容通过freemarker生成html文件
            Template template = configuration.getTemplate("article.ftl");

            // 构造数据
            Map<String, Object> params = new HashMap<>();
            params.put("content", JSONArray.parseArray(apArticleContent.getContent()));

            // 生成文件流
            template.process(params, out);
            InputStream is = new ByteArrayInputStream(out.toString().getBytes());

            // 上传html文件到minio
            String path = fileStorageService.uploadHtmlFile("", apArticleContent.getArticleId() + ".html", is);

            // 修改ap_article表 保存static_url字段
            ApArticle apArticle = new ApArticle();
            apArticle.setId(apArticleContent.getArticleId());
            apArticle.setStaticUrl(path);
            int i = apArticleMapper.updateById(apArticle);

            System.out.println("更新成功条数 = " +  i);

        }

    }
}
