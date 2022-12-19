package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/v1/article")
public class ArticleHomeController {

    @Resource
    private ApArticleService apArticleService;

    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleHomeDto dto) {
        // return apArticleService.load(ArticleConstants.LOAD_TYPE_LOAD_MORE, dto);
        return apArticleService.load2(dto, ArticleConstants.LOAD_TYPE_LOAD_MORE, true);
    }

    @PostMapping("/loadMore")
    public ResponseResult loadMore(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(ArticleConstants.LOAD_TYPE_LOAD_MORE, dto);
    }

    @PostMapping("/loadNew")
    public ResponseResult loadNew(@RequestBody ArticleHomeDto dto) {
        return apArticleService.load(ArticleConstants.LOAD_TYPE_LOAD_NEW, dto);
    }
}
