package com.ming.stock;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Author: Ming
 * @Description TODO
 */
@SpringBootApplication
@MapperScan("com.ming.stock.mapper") //扫描持久层mapper接口 生成代理对象 并维护到ioc容器
public class BackendApp {
    public static void main(String[] args) {
        SpringApplication.run(BackendApp.class,args);
    }
}
