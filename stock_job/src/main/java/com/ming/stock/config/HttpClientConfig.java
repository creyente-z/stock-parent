package com.ming.stock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Author: Ming
 * @Description 定义访问http服务的配置类
 */
@Configuration
public class HttpClientConfig {
    /*
    * 定义restTemplate bean
    * */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
