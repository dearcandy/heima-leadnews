package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

/**
 * 首頁請求通用參數
 */
@Data
public class ArticleHomeDto {

    /**
     * 最大时间
     */
    Date maxBeHotTime;
    /**
     * 最小时间
     */
    Date minBeHotTime;
    /**
     * 分页size
     */
    Integer size;
    /**
     * 频道ID
     */
    String tag;
}
