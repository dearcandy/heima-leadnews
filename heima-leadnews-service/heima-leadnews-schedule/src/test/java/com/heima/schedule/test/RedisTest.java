package com.heima.schedule.test;

import com.heima.common.redis.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
public class RedisTest {

    @Resource
    CacheService cacheService;

    @Test
    public void listTest(){
       // cacheService.lLeftPush("list_001", "hello-redis");

        String list_001 = cacheService.lRightPop("list_001");
        System.out.println(list_001);
    }


    @Test
    public void zSetTest(){
//        cacheService.zAdd("zSet-001", "hello-zset_002", 2000);
//        cacheService.zAdd("zSet-001", "hello-zset_003", 3000);
//        cacheService.zAdd("zSet-001", "hello-zset_004", 4000);

        Set<String> strings = cacheService.zRangeByScore("zSet-001", 0, 3000);
        System.out.println(strings);
    }
}
