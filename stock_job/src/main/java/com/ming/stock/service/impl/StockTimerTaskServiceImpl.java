package com.ming.stock.service.impl;

import com.google.common.collect.Lists;

import com.ming.stock.mapper.StockBusinessMapper;
import com.ming.stock.mapper.StockMarketIndexInfoMapper;

import com.ming.stock.mapper.StockRtInfoMapper;
import com.ming.stock.pojo.entity.StockMarketIndexInfo;

import com.ming.stock.pojo.entity.StockRtInfo;
import com.ming.stock.pojo.vo.StockInfoConfig;
import com.ming.stock.service.StockTimerTaskService;
import com.ming.stock.utils.DateTimeUtil;
import com.ming.stock.utils.IdWorker;
import com.ming.stock.utils.ParseType;
import com.ming.stock.utils.ParserStockInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockTimerTaskServiceImpl implements StockTimerTaskService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StockInfoConfig stockInfoConfig;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private StockMarketIndexInfoMapper stockMarketIndexInfoMapper;

    @Autowired
    private StockBusinessMapper stockBusinessMapper;

    @Autowired
    private ParserStockInfoUtil parserStockInfoUtil;

    @Autowired
    private StockRtInfoMapper stockRtInfoMapper;
    //必须保证该对象是无状态值
    private HttpEntity<Object> httpEntity;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Override
    public void getInnerMarketInfo() {
        //1.阶段1:采集原始数据
        //1.1组装url地址
        //String url = "http://hq.sinajs.cn/list=sh000001,sz399001";
        String url = stockInfoConfig.getMarketUrl()+String.join(",",stockInfoConfig.getInner());
        //1.2维护请求头 添加防盗链和用户标识
//        HttpHeaders headers = new HttpHeaders();
//        //防盗链
//        headers.add("Referer","https://finance.sina.com.cn/stock/");
//        //用户客户端标识
//        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");        //维护http请求实体对象
//        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);

        //发送请求
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        //状态码
        int statusCodeValue = responseEntity.getStatusCodeValue();
        if (responseEntity.getStatusCodeValue()!=200){
            //当前请求失败
            log.error("当前时间点:{},采集数据失败,http状态码:{}",
                    DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),statusCodeValue);
            //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
            return;
        }
        //获取js格式数据
        String jsData = responseEntity.getBody();
        log.info("当前时间点:{},采集原始数据的内容:{}",
                DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),jsData);
        //2.阶段2:java正则解析原始数据
        String reg = "var hq_str_(.+)=\"(.+)\";";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(jsData);
        ArrayList<StockMarketIndexInfo> list = new ArrayList<>();
        while(matcher.find()){

            String marketCode = matcher.group(1);

            String otherInfo = matcher.group(2);

            //分割 获取大盘的详细信息
            String[] splitArr =  otherInfo.split(",");

            //大盘名称
            String marketName=splitArr[0];
            //获取当前大盘的开盘点数
            BigDecimal openPoint=new BigDecimal(splitArr[1]);
            //前收盘点
            BigDecimal preClosePoint=new BigDecimal(splitArr[2]);
            //获取大盘的当前点数
            BigDecimal curPoint=new BigDecimal(splitArr[3]);
            //获取大盘最高点
            BigDecimal maxPoint=new BigDecimal(splitArr[4]);
            //获取大盘的最低点
            BigDecimal minPoint=new BigDecimal(splitArr[5]);
            //获取成交量
            Long tradeAmt=Long.valueOf(splitArr[8]);
            //获取成交金额
            BigDecimal tradeVol=new BigDecimal(splitArr[9]);
            //时间
            Date curTime = DateTimeUtil.getDateTimeWithoutSecond(splitArr[30] + " " + splitArr[31]).toDate();

            StockMarketIndexInfo info = StockMarketIndexInfo.builder()
                    .id(idWorker.nextId())
                    .marketCode(marketCode)
                    .marketName(marketName)
                    .curPoint(curPoint)
                    .maxPoint(maxPoint)
                    .minPoint(minPoint)
                    .curTime(curTime)
                    .openPoint(openPoint)
                    .tradeAmount(tradeAmt)
                    .tradeVolume(tradeVol)
                    .preClosePoint(preClosePoint)
                    .build();
            list.add(info);
        }
        //3.阶段3:解析数据封装到entity



        //4.阶段4:调用mybatis批量入库

        int count =  stockMarketIndexInfoMapper.insertBatch(list);
        if(count>0){

            //大盘采集数据完毕之后 通知Backend工程刷新缓存
            //发送日期对象
            rabbitTemplate.convertAndSend("stockExchange","inner.market",new Date());

            log.info("当前时间:{}，插入数据:{}成功",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),count);

        }else {

            log.info("当前时间:{}，插入数据:{}失败",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),count);

        }
    }

    @Override
    public void getStockRtIndex() {

        List<String> list = stockBusinessMapper.getStockIds();


        //添加大盘业务前缀 sh sz

        list = list.stream().map(code->code.startsWith("6")?"sh"+code:"sz"+code).collect(Collectors.toList());

        long startTime = System.currentTimeMillis();


        //一次性将集合拼接到url中会导致地址过长，参数过多
//        String url = stockInfoConfig.getMarketUrl()+String.join(",",list);
        //核心思想是将大的集合切分成若干个小集合 分批次拉取数据
        //将所有的个股编码组成庞大的集合拆分成若干个小集合 40---->15 15 10
        //guawa 进行拆分
        Lists.partition(list,15).forEach(codes->{
//            //原始方案
//            //分批次采集
//                //方案1：原始方案采集个股数据时将集合分片 然后分批次采集数据 效率不高 存在较高的采集延迟 所以引入多线程 提高采集速度
//                //代码问题：1。每次执行采任务就创建一个线程 复用性差 2.如果多线程使用不当 导致多线程互相竞争
//                new Thread(()->{
//                    String url =  stockInfoConfig.getMarketUrl()+String.join(",",codes);
//
////                    HttpHeaders headers = new HttpHeaders();
//                    //防盗链
// //                   headers.add("Referer","https://finance.sina.com.cn/stock/");
//                    //用户客户端标识
////                    headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");        //维护http请求实体对象
////                    HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
//            //发送请求
//            ResponseEntity<String> responseEntity =
//                    restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
//
//            //状态码
//            int statusCodeValue = responseEntity.getStatusCodeValue();
//            if (responseEntity.getStatusCodeValue()!=200){
//                //当前请求失败
//                log.error("当前时间点:{},采集数据失败,http状态码:{}",
//                        DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),statusCodeValue);
//                //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
//                return;
//            }
//            //获取js格式数据
//            String jsData = responseEntity.getBody();
//            //调用工具解析个股数据
//            List<stock_rt_info> list1 = parserStockInfoUtil.parser4StockOrMarketInfo(jsData, ParseType.ASHARE);
//
//            log.info("采集数据:{}",list1);
//
//            int count = stockRtInfoMapper.insertBatch(list1);
//
//            if(count>0){
//
//                log.info("当前时间:{}，个股插入数据:{}成功",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list1);
//
//            }else {
//
//                log.info("当前时间:{}，个股插入数据:{}失败",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list1);
//
//            }
//
//                }).start();

            threadPoolTaskExecutor.execute(()->{

                String url =  stockInfoConfig.getMarketUrl()+String.join(",",codes);

//                    HttpHeaders headers = new HttpHeaders();
                //防盗链
                //                   headers.add("Referer","https://finance.sina.com.cn/stock/");
                //用户客户端标识
//                    headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");        //维护http请求实体对象
//                    HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
                //发送请求
                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);

                //状态码
                int statusCodeValue = responseEntity.getStatusCodeValue();
                if (responseEntity.getStatusCodeValue()!=200){
                    //当前请求失败
                    log.error("当前时间点:{},采集数据失败,http状态码:{}",
                            DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),statusCodeValue);
                    //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
                    return;
                }
                //获取js格式数据
                String jsData = responseEntity.getBody();
                //调用工具解析个股数据
                List<StockRtInfo> list1 = parserStockInfoUtil.parser4StockOrMarketInfo(jsData, ParseType.ASHARE);

                log.info("采集数据:{}",list1);

                int count = stockRtInfoMapper.insertBatch(list1);

                if(count>0){

                    log.info("当前时间:{}，个股插入数据:{}成功",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list1);

                }else {

                    log.info("当前时间:{}，个股插入数据:{}失败",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list1);

                }

            });

        });

        long endTime = System.currentTimeMillis();
        System.out.println(endTime-startTime);
    }

    /**
     * bean的生命周期的初始化会回调那方法
     */
    @PostConstruct
    public void initData(){

        HttpHeaders headers = new HttpHeaders();
        //防盗链
        headers.add("Referer","https://finance.sina.com.cn/stock/");
        //用户客户端标识
        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");        //维护

        httpEntity = new HttpEntity<>(headers);
    }

}