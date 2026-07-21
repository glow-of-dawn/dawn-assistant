package com.dawn.plugin.task.service;

import com.dawn.plugin.entity.ccore.TabTask;
import com.dawn.plugin.util.Response;
import org.springframework.stereotype.Service;

/**
 * 定时任务
 * 创建时间：2020/11/25 10:24
 *
 * @author hforest-480s
 */
@Service
public interface HandleService extends Runnable {

    /**
     * [程序处理]
     *
     * @param tabTask [tabTask]
     **/
    void setTabTask(TabTask tabTask);

    /**
     * [程序处理]
     *
     * @return java.lang.String
     **/
    Response<Object> handle();

    /**
     * [任务处理]
     **/
    @Override
    void run();

}
