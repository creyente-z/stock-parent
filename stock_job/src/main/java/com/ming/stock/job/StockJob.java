package com.ming.stock.job;

import com.ming.stock.service.StockTimerTaskService;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: Ming
 * @Description TODO
 */
@Component
public class StockJob {
    @Autowired
    private StockTimerTaskService stockTimerTaskService;

    /**
     * 1、简单任务示例（Bean模式）
     */
    @XxlJob("myJobHandler")
    public void demoJobHandler() throws Exception {
        System.out.println("当前时间点为:"+ DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 定义定时任务 采集国内大盘数据
     */
//    @XxlJob("getInnerMarketInfo")
//    public void getStockInnerMarketInfos(){
//        stockTimerTaskService.getInnerMarketInfo();
//    }

    /**
     * 定时采集A股数据
     */
//    @XxlJob("getStockInfos")
//    public void getStockInfos(){
//        stockTimerTaskService.getStockRtIndex();;
//    }
}
