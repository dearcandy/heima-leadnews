package com.heima.search.service;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;

public interface ArticleSearchService {

    /**
     * 文章飞分页搜索
     * @param dto 搜索条件
     * @return 搜索结果
     */
    ResponseResult search(@RequestBody UserSearchDto dto) throws IOException;
}
