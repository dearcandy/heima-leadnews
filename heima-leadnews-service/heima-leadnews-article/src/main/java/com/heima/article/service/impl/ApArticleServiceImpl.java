package com.heima.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Resource
    private ApArticleMapper apArticleMapper;

    /**
     * 默认最大分页参数
     */
    private static final Integer MAX_PAGE_SIZE = 50;

    /**
     * 根据参数加载文章列表
     * @param loadType 1为加载更多  2为加载最新
     * @param dto 加載參數
     * @return 文章列表
     */
    @Override
    public ResponseResult load(Short loadType, ArticleHomeDto dto) {
        // 分頁條數校驗
        Integer size =  dto.getSize();
        if (size == null || size == 0){
            size = 10;
        }
        size = Math.min(size, MAX_PAGE_SIZE);
        // 加载类型校验
        if (!loadType.equals(ArticleConstants.LOAD_TYPE_LOAD_MORE) && !loadType.equals(ArticleConstants.LOAD_TYPE_LOAD_NEW)){
            loadType = ArticleConstants.LOAD_TYPE_LOAD_MORE;
        }
        // 文章频道校验
        if (StringUtils.isBlank(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        // 时间校验
        if (dto.getMaxBeHotTime() == null){
            dto.setMaxBeHotTime(new Date());
        }
        if (dto.getMinBeHotTime() == null){
            dto.setMinBeHotTime(new Date());
        }

        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, loadType);
        return ResponseResult.okResult(apArticles);
    }
}
