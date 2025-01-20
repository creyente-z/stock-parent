package com.ming.stock.config;

import com.ming.stock.pojo.vo.StockInfoConfig;
import com.ming.stock.utils.IdWorker;
import com.ming.stock.utils.ParserStockInfoUtil;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Ming
 * @Description: 定义公共配置类
 **/
@Configuration
@EnableConfigurationProperties({StockInfoConfig.class})//开启对相关配置对象的加载
public class CommonConfig {
    /**
     * 配置基于雪花算法生成全局唯一id
     *   参与元算的参数： 时间戳 + 机房id + 机器id + 序列号
     *   保证id唯一
     * @return
     */
    @Bean
    public IdWorker idWorker(){
        //参数1:机器id  参数2:机房id 一般有运维人员定唯一性
        //基于运维人员对机房和机器的编号规划自行约定
        return new IdWorker(1l,2l);
    }

    /**
     * 定义解析股票 大盘 外盘  个股  板块相关的工具类bean
     * @return
     */
    @Bean
    public ParserStockInfoUtil parserStockInfoUtil(){
        return new ParserStockInfoUtil(idWorker());
    }
}
