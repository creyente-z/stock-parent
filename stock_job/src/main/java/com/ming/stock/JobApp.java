package com.ming.stock;

import com.ming.stock.pojo.vo.TaskThreadPoolInfo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @Author: Ming
 * @Description TODO
 */
@SpringBootApplication
@MapperScan("com.ming.stock.mapper")
@EnableConfigurationProperties(TaskThreadPoolInfo.class)
public class JobApp {
    public static void main(String[] args) {
        SpringApplication.run(JobApp.class,args);
    }
}
