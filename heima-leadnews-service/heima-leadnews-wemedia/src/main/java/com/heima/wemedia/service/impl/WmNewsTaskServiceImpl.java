package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.apis.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.TaskTypeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.ProtostuffUtil;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class WmNewsTaskServiceImpl implements WmNewsTaskService {

    @Resource
    private IScheduleClient scheduleClient;
    @Resource
    WmNewsAutoScanService wmNewsAutoScanService;

    /**
     * 添加任务到延迟队列
     * @param id 文章ID
     * @param publishTime 文章发布时间, 作为任务执行时间
     */
    @Override
    @Async
    public void addNewsToTask(Integer id, Date publishTime) {

        log.info("添加任务到延迟任务服务 start");
        Task task = new Task();
        task.setExecuteTime(publishTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        WmNews wmNews = new WmNews();
        wmNews.setId(id);
        task.setParameters(ProtostuffUtil.serialize(wmNews));

        scheduleClient.addTask(task);

        log.info("添加任务到延迟任务服务 end");
    }

    /**
     * 消费任务 审核文章
     */
    @Scheduled(fixedRate = 1000)
    @Override
    public void scanNewsByTask() {
        log.info("消费任务 审核文章 ");
        ResponseResult responseResult = scheduleClient.pull(TaskTypeEnum.NEWS_SCAN_TIME.getTaskType(), TaskTypeEnum.NEWS_SCAN_TIME.getPriority());
        if (responseResult.getCode().equals(200) && responseResult.getData() != null){
            // 消费任务 审核文章
            Task task = JSON.parseObject(JSON.toJSONString(responseResult.getData()), Task.class);
            WmNews wmNews = ProtostuffUtil.deserialize(task.getParameters(), WmNews.class);
            wmNewsAutoScanService.autoScanWmNews(wmNews.getId());
        }
    }


}
