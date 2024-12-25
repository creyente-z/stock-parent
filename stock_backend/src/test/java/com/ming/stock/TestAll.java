package com.ming.stock;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: Ming
 * @Description TODO
 */
@SpringBootTest
public class TestAll {
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 测试密码加密
     */
    @Test
    public void testPwd(){
        String pwd = "123456";
        //加密 $2a$10$q1PEEIrTt94yzXI42pkAne3MigcimjDdgcKXkh6dzz0JRhGjzxL0S
        String encode = passwordEncoder.encode(pwd);
        System.out.println(encode);

        boolean flag = passwordEncoder.matches(pwd,
                "$2a$10$q1PEEIrTt94yzXI42pkAne3MigcimjDdgcKXkh6dzz0JRhGjzxL0S");
        System.out.println(flag);
    }

}
