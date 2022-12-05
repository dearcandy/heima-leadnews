package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.constants.ArticleConstants;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.article.service.ApArticleService;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Resource
    private ApArticleMapper apArticleMapper;
    @Resource
    private ApArticleConfigMapper apArticleConfigMapper;
    @Resource
    private ApArticleContentMapper apArticleContentMapper;

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

    /**
     * 保存app端相关文章
     * @param dto ArticleDto
     * @return ResponseResult
     */
    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        // 检查参数
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto,apArticle);

        // 判断是否存在ID
        if (dto.getId() == null){
            // 不存在 保存文章 文章配置和内容
            save(apArticle);

            ApArticleConfig apArticleConfig = new ApArticleConfig(apArticle.getId());
            apArticleConfigMapper.insert(apArticleConfig);

            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }else{
            updateById(apArticle);

            // 修改文章内容
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                    .eq(ApArticleContent::getArticleId, dto.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);

        }
        return ResponseResult.okResult(apArticle.getId());
    }
}
