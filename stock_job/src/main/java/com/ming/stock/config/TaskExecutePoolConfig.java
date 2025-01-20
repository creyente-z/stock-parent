package com.ming.stock.config;

import com.ming.stock.pojo.vo.TaskThreadPoolInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: Ming
 * @Description TODO
 */
@Configuration
public class TaskExecutePoolConfig {
    @Autowired
    private TaskThreadPoolInfo info;
    /**
     * 构建线程池bean对象
     *
     */
    @Bean(name = "threadPoolTaskExecutor",destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor ThreadPollTaskExecute(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数(线程池创建的时候初始化的线程数)
        executor.setCorePoolSize(info.getCorePoolSize());
        //最大线程数:只有缓冲队列满了之后才会申请超过核心线程程序数的线程
        executor.setMaxPoolSize(info.getMaxPoolSize());
        //允许线程的空闲时间:当超过了核心线程数之外的线程在空闲时间到达之后被销毁
        executor.setKeepAliveSeconds(info.getKeepAliveSeconds());
        //设置任务队列的长度 缓冲队列 :用来缓冲执行任务队列
        executor.setQueueCapacity(info.getQueueCapacity());
        //设置拒绝策列
        //executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        //将设置参数初始化
        executor.initialize();
        return executor;

    }

}
