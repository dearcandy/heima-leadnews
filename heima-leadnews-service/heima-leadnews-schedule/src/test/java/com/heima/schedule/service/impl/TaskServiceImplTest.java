package com.heima.schedule.service.impl;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.Date;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TaskServiceImplTest {

    @Resource
    private TaskService taskService;

    @Test
    public void addTask() {

        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(50);
        task.setParameters("task test".getBytes());
        task.setExecuteTime(new Date().getTime());

        long id = taskService.addTask(task);

        System.out.println(id);
    }

    @Test
    public void cancelTask(){
        boolean b = taskService.cancelTask(1600392069962117122L);
    }

    @Test
    public void pullTaskTest(){
        Task task = taskService.pull(100 , 50);
        System.out.println(task);
    }
}