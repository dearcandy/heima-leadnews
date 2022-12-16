package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {

    /**
     * 保存用户搜索记录
     * @param keyword 搜索关键词
     * @param userId 当前用户ID
     */
    void insert(String keyword, Integer userId);

    /**
     * 查询用户搜索历史
     * @return 搜索历史
     */
    ResponseResult findUserSearch();

    /**
     * 删除用户搜索历史
     * @param dto
     * @return
     */
    ResponseResult delUserSearch(HistorySearchDto dto);
}
