package com.heima.search.service.impl;

import com.heima.search.pojos.ApUserSearch;
import com.heima.search.service.ApUserSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class ApUserSearchServiceImpl implements ApUserSearchService {

    @Resource
    private MongoTemplate mongoTemplate;

    /**
     * 保存用户搜索记录
     *
     * @param keyword 搜索关键词
     * @param userId  当前用户ID
     */
    @Override
    @Async
    public void insert(String keyword, Integer userId) {
        log.info("保存用户搜索记录 keyword : {}, userId : {}", keyword, userId);
        // 查询当前用户的搜索关键词
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch apUserSearch = mongoTemplate.findOne(query, ApUserSearch.class);

        // 存在 更新创建时间
        if (apUserSearch != null){
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
            return;
        }

        // 不存在 判断当前用户搜索记录条数是否超过10
        apUserSearch = new ApUserSearch();
        apUserSearch.setUserId(userId);
        apUserSearch.setKeyword(keyword);
        apUserSearch.setCreatedTime(new Date());
        Query query1 = Query.query(Criteria.where("userId").is(userId));
        query1.with(Sort.by(Sort.Direction.DESC, "createTime"));
        List<ApUserSearch> apUserSearches = mongoTemplate.find(query1, ApUserSearch.class);

        // 小于10条 添加记录
        if (apUserSearches.size() < 10){
            mongoTemplate.save(apUserSearch);
        }else {
            // 大于等于十条 替换最后一条记录
            ApUserSearch lastUserSearch = apUserSearches.get(apUserSearches.size() - 1);
            mongoTemplate.findAndReplace(Query.query(Criteria.where("id").is(lastUserSearch.getId())), apUserSearch);
        }

    }
}
