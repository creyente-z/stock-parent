package com.ming.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: Ming
 * @Description 定义公共的配置类
 */
@Configuration
public class CommonConfig {
    /**
     *  密码加密器
     *  BCryptPasswordEncoder()方法采用SHA-256对称密码进行加密
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
