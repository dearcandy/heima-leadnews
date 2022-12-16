package com.heima.common.constants;

/**
 * 文章相关常量
 */
public class ArticleConstants {
    /**
     * 加载文章列表类型-加载更多
     */
    public static final Short LOAD_TYPE_LOAD_MORE = 1;
    /**
     * 加载文章列表类型-加载最新
     */
    public static final Short LOAD_TYPE_LOAD_NEW = 2;
    /**
     * 加载文章标签-默认推荐(全量)
     */
    public static final String DEFAULT_TAG = "__all__";

    /**
     * 文章创建ES索引topic
     */
    public static final String ARTICLE_ES_SYNC_TOPIC = "article.es.sync.topic";


    /**
     * 热点文章点赞权重
     */
    public static final Integer HOT_ARTICLE_LIKE_WEIGHT = 3;
    /**
     * 热点文章评论权重
     */
    public static final Integer HOT_ARTICLE_COMMENT_WEIGHT = 5;
    /**
     * 热点文章收藏权重
     */
    public static final Integer HOT_ARTICLE_COLLECTION_WEIGHT = 8;

    public static final String HOT_ARTICLE_FIRST_PAGE = "hot_article_first_page_";
}