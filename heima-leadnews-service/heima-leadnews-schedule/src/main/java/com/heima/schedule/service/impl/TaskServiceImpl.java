package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.TaskInfo;
import com.heima.model.schedule.pojos.TaskInfoLogs;
import com.heima.schedule.mapper.TaskInfoLogsMapper;
import com.heima.schedule.mapper.TaskInfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    @Resource
    private TaskInfoMapper taskInfoMapper;
    @Resource
    private TaskInfoLogsMapper taskInfoLogsMapper;
    @Resource
    private CacheService cacheService;


    /**
     * 添加任务
     * @param task   任务对象
     * @return 任务id
     */
    @Override
    public long addTask(Task task) {

        // 添加任务到数据库
        boolean success = addTaskToDb(task);

        if(success){
            // 添加任务到redis
            addTaskToCache(task);
        }

        return task.getTaskId();
    }

    /**
     * 添加任务到redis
     * @param task 任务信息
     */
    private void addTaskToCache(Task task) {
        // 获取五分钟后的时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long nextScheduleTime = calendar.getTimeInMillis();

        String key = task.getTaskType() + "_" + task.getPriority();
        // 任务执行时间小于等于当前时间 存入list
        if (task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lLeftPush(ScheduleConstants.TOPIC + key, JSON.toJSONString(task));
        }else if (task.getExecuteTime() < nextScheduleTime){
            // 任务执行时间大于当前时间 小于预设时间(5分钟内) 存入zSet
            cacheService.zAdd(ScheduleConstants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }

    }

    /**
     * 添加任务到数据库
     * @param task 任务实体
     * @return 是否成功添加
     */
    private boolean addTaskToDb(Task task) {
        // 保存任务表
        TaskInfo taskInfo = new TaskInfo();
        BeanUtils.copyProperties(task, taskInfo);
        taskInfo.setExecuteTime(new Date(task.getExecuteTime()));
        int insert = taskInfoMapper.insert(taskInfo);

        // 设置taskId
        task.setTaskId(taskInfo.getTaskId());
        log.info("添加任务成功条数 : {}", insert);
        // 保存任务日志表
        TaskInfoLogs taskInfoLogs = new TaskInfoLogs();
        BeanUtils.copyProperties(taskInfo, taskInfoLogs);
        taskInfoLogs.setStatus(ScheduleConstants.SCHEDULED);
        taskInfoLogs.setVersion(1);
        int insert1 = taskInfoLogsMapper.insert(taskInfoLogs);
        log.info("添加任务日志成功条数 : {}", insert1);

        return insert == 1 && insert1 == 1;
    }
}
