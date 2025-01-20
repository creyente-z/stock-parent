package com.ming.stock;

import com.ming.stock.mapper.StockBlockRtInfoMapper;
import com.ming.stock.mapper.StockBusinessMapper;
import com.ming.stock.mapper.StockRtInfoMapper;
import com.ming.stock.mapper.SysUserMapper;
import com.ming.stock.pojo.domain.Stock4EvrDayDomain;
import com.ming.stock.pojo.domain.StockBlockDomain;
import com.ming.stock.pojo.domain.WeekklineDomain;
import com.ming.stock.pojo.entity.StockBusiness;
import com.ming.stock.pojo.entity.SysUser;
import com.ming.stock.utils.DateTimeUtil;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
public class TestSharding {
    @Autowired
    private SysUserMapper sysUserMapper;
    /**
     * 测试默认数据源
     */
    @Test
    public void testDefaultDs(){
        SysUser user = sysUserMapper.selectByPrimaryKey("1237365636208922624");
        System.out.println(user);
    }

    @Autowired
    private StockBusinessMapper stockBusinessMapper;
    /**
     * @Description 测试广播表
     */
    @Test
    public void testBroadCastTable(){
//        StockBusiness pojo = StockBusiness.builder().stockCode("90000")
//                .stockName("90000")
//                .blockLabel("90000")
//                .blockName("90000")
//                .business("90000")
//                .updateTime(new Date())
//                .build();
//        stockBusinessMapper.insert(pojo);
        stockBusinessMapper.deleteByPrimaryKey("90000");
    }
    @Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;

    /**
     * @Description 测试公共分库算法类
     */
    @Test
    public void testCommon4Db(){
        Date curDate= DateTime.parse("2022-01-03 09:30:00", DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate();
        List<StockBlockDomain> info = stockBlockRtInfoMapper.sectorAllLimit(curDate);
        System.out.println(info);
    }

    @Autowired
    private StockRtInfoMapper stockRtInfoMapper;

    /**
     * @Description 测试分库分表算法类
     */
    @Test
    public void testShardingDbAndTb(){
        //1.获取统计日k线的数据的时间范围
        //1.1获取截止时间(当前时间)
        DateTime endDateTime = DateTimeUtil.getLastDate4Stock(DateTime.now());
        //mock data
        endDateTime = DateTime.parse(
                "2023-01-07 15:00:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date endDate = endDateTime.toDate();
        //1.2获取开始时间
        DateTime startDateTime = endDateTime.minusDays(10);
        //mock data
        startDateTime = DateTime.parse(
                "2023-01-01 9:30:00",DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"));
        Date startDate = startDateTime.toDate();
        //2.调用mapper获取指定日期范围内的日k线数据
        List<Stock4EvrDayDomain> dkLineData =
                stockRtInfoMapper.getStock4DkLine(startDate,endDate,"000017");

        System.out.println(dkLineData);
    }
}