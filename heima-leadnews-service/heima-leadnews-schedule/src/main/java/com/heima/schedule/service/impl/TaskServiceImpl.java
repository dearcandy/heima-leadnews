package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.constants.ScheduleConstants;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.TaskInfo;
import com.heima.model.schedule.pojos.TaskInfoLogs;
import com.heima.schedule.mapper.TaskInfoLogsMapper;
import com.heima.schedule.mapper.TaskInfoMapper;
import com.heima.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
     * 取消任务
     * @param taskId 任务ID
     * @return 取消结果
     */
    @Override
    public boolean cancelTask(long taskId) {
        boolean flag = false;
        // 删除任务 更新任务日志
        Task task = updateDb(taskId, ScheduleConstants.CANCELLED);

        // 删除缓存中任务信息
        if (task.getTaskType() != null){
            removeTaskFromCache(task);
            flag = true;
        }

        return flag;
    }

    /**
     * 拉取任务
     * @param type 任务类型
     * @param priority 任务优先级
     * @return 任务
     */
    @Override
    public Task pull(int type, int priority) {
        Task task = null;
        try {
            String key = type + "_" + priority;
            // 从Redis中拉取数据
            String taskJson = cacheService.lRightPop(ScheduleConstants.TOPIC + key);
            if (StringUtils.isNotBlank(taskJson)){
                task = JSON.parseObject(taskJson, Task.class);

                // 修改数据库信息
                updateDb(task.getTaskId(), ScheduleConstants.EXECUTED);
            }
        }catch (Exception exception){
            log.error("pull task has occurred exception : ", exception);
        }
        return task;
    }

    /**
     * 移除缓存中任务
     * @param task 任务实体
     */
    private void removeTaskFromCache(Task task) {
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()){
            cacheService.lRemove(ScheduleConstants.TOPIC + key, 0, JSON.toJSONString(task));
        }else{
            cacheService.zRemove(ScheduleConstants.FUTURE + key,  JSON.toJSONString(task));
        }
    }

    /**
     * 删除任务 更新任务日志
     * @param taskId 任务ID
     * @param status 任务状态
     * @return 任务信息
     */
    private Task updateDb(long taskId, int status) {
        Task task = new Task();
      try{
          // 删除任务
          taskInfoMapper.deleteById(taskId);
          // 更新任务日志
          TaskInfoLogs taskInfoLogs = taskInfoLogsMapper.selectById(taskId);
          taskInfoLogs.setStatus(status);
          taskInfoLogsMapper.updateById(taskInfoLogs);

          BeanUtils.copyProperties(taskInfoLogs, task);
          task.setExecuteTime(taskInfoLogs.getExecuteTime().getTime());
      }catch (Exception exception){
          log.error("task cancel has occurred exception : ", exception);
      }
      return task;
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

    @Scheduled(cron = "0 */1 * * * ?")
    public void refresh(){
        // 获取分布式锁
        String token = cacheService.tryLock("FUTURE_TASK_SYNC", 1000 * 30);

        if (StringUtils.isNotBlank(token)){
            log.info("未来数据定时刷新");

            // 获取所有未来集合key
            Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");
            for (String futureKey: futureKeys) {
                // 构造当前数据的topic_key
                String topicKey = ScheduleConstants.TOPIC + futureKey.split(ScheduleConstants.FUTURE)[1];

                // 按照key和分值查询符合条件的数据
                Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());

                // 同步数据
                if (!tasks.isEmpty()) {
                    cacheService.refreshWithPipeline(futureKey, topicKey, tasks);
                    log.info("成功的将" + futureKey + "刷新到了" + topicKey);
                }
            }
        }

    }

    /**
     * 数据库任务定时全量同步Redis 每五分钟
     */
    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void reloadData(){
        // 清理缓存任务数据 list zSet
        clearCache();
        // 查询符合条件的任务
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);

        List<TaskInfo> taskInfos = taskInfoMapper.selectList(Wrappers.<TaskInfo>lambdaQuery().lt(TaskInfo::getExecuteTime, calendar.getTime()));
        // 任务同步

        if (taskInfos != null && taskInfos.size() > 0){
            for (TaskInfo taskInfo : taskInfos){
                Task task = new Task();
                BeanUtils.copyProperties(taskInfo, task);
                task.setExecuteTime(taskInfo.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }

        log.info("数据库任务定时全量同步Redis");
    }

    /**
     * 清理缓存中任务
     */
    public void  clearCache(){
        Set<String> topicKeys = cacheService.scan(ScheduleConstants.TOPIC + "*");
        Set<String> futureKeys = cacheService.scan(ScheduleConstants.FUTURE + "*");

        cacheService.delete(topicKeys);
        cacheService.delete(futureKeys);
    }
}
