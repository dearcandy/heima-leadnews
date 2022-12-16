package com.heima.search.service;

public interface ApUserSearchService {

    /**
     * 保存用户搜索记录
     * @param keyword 搜索关键词
     * @param userId 当前用户ID
     */
    void insert(String keyword, Integer userId);
}
