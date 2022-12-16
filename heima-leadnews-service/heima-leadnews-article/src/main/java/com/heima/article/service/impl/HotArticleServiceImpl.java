package com.heima.article.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.wemedia.IWemediaClient;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.HotArticleService;
import com.heima.common.constants.ArticleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.vos.HotArticleVo;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmChannel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class HotArticleServiceImpl implements HotArticleService {

    @Resource
    private ApArticleMapper apArticleMapper;

    @Resource
    private IWemediaClient weMediaClient;

    @Resource
    private CacheService cacheService;

    /**
     * 计算热点文章
     */
    @Override
    public void computeHotArticle() {
        // 查询前五天文章数据
        Date date = DateTime.now().minusDays(5).toDate();
        List<ApArticle> apArticleList = apArticleMapper.findArticleListByLast5days(date);
        // 计算文章的分值
       List<HotArticleVo> hotArticleVoList = computeHotArticle(apArticleList);

        // 为每个频道缓存30条分值较高的文章
        cacheTagToRedis(hotArticleVoList);
    }

    /**
     * 为每个频道缓存30条分值最高的文章
     * @param hotArticleVoList 文章带热点分值列表
     */
    private void cacheTagToRedis(List<HotArticleVo> hotArticleVoList) {
        // 获取所有频道
        ResponseResult responseResult = weMediaClient.getChannels();
        if (responseResult.getCode().equals(200)){
            String channelsJson = JSON.toJSONString(responseResult.getData());
            List<WmChannel> channels = JSON.parseArray(channelsJson, WmChannel.class);
            if (!CollectionUtils.isEmpty(channels)){
                for (WmChannel channel: channels) {
                    // 获取当前频道的所有文章
                    List<HotArticleVo> articlesByChannel = hotArticleVoList.stream().filter(item -> item.getChannelId().equals(channel.getId())).collect(Collectors.toList());
                    // 对当前频道文章按照热点分进行排序 取前30条存进redis key channelId value : 文章列表
                    sortAndCache(articlesByChannel, ArticleConstants.HOT_ARTICLE_FIRST_PAGE + channel.getId());
                }
            }
        }
    }

    /**
     * 文章按照热点分进行排序 取前30条存进redis key channelId value : 文章列表
     * @param hotArticleVoList 文章列表
     * @param key 频道信息
     */
    private void sortAndCache(List<HotArticleVo> hotArticleVoList, String key) {
        List<HotArticleVo> sortedArticles = hotArticleVoList.stream().sorted(Comparator.comparing(HotArticleVo::getScore).reversed()).collect(Collectors.toList());
        if (sortedArticles.size() > 30) {
            sortedArticles = sortedArticles.subList(0, 30);
        }
        cacheService.set(key, JSON.toJSONString(sortedArticles));
    }

    /**
     * 根据文章列表计算文章分值
     * @param apArticleList 文章列表
     * @return 文章带热点分值列表
     */
    private List<HotArticleVo> computeHotArticle(List<ApArticle> apArticleList) {
        List<HotArticleVo> hotArticleVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(apArticleList)){
            for (ApArticle apArticle: apArticleList) {
                HotArticleVo hotArticleVo = new HotArticleVo();
                BeanUtils.copyProperties(apArticle, hotArticleVo);
                // 计算当前文章热点分数
                Integer score = computeScore(apArticle);
                hotArticleVo.setScore(score);
                hotArticleVoList.add(hotArticleVo);
            }
        }
        return hotArticleVoList;
    }

    /**
     * 根据文章信息计算文章热点分值
     * @param apArticle 文章实体
     * @return 文章分值
     */
    private Integer computeScore(ApArticle apArticle) {
        Integer score = 0;
        if (apArticle.getLikes() != null){
            score += apArticle.getLikes() * ArticleConstants.HOT_ARTICLE_LIKE_WEIGHT;
        }
        if (apArticle.getViews() != null){
            score += apArticle.getViews();
        }
        if (apArticle.getComment() != null){
            score += apArticle.getComment() * ArticleConstants.HOT_ARTICLE_COMMENT_WEIGHT;
        }
        if (apArticle.getCollection() != null){
            score += apArticle.getCollection() * ArticleConstants.HOT_ARTICLE_COLLECTION_WEIGHT;
        }

        return  score;
    }
}
