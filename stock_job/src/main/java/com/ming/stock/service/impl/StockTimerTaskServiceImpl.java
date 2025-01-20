package com.ming.stock.service.impl;

import com.google.common.collect.Lists;
import com.ming.stock.face.StockCacheFace;
import com.ming.stock.mapper.StockBlockRtInfoMapper;
import com.ming.stock.mapper.StockBusinessMapper;
import com.ming.stock.mapper.StockMarketIndexInfoMapper;
import com.ming.stock.mapper.StockRtInfoMapper;
import com.ming.stock.pojo.entity.StockBlockRtInfo;
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

/**
 * @Author: Ming
 * @Description TODO
 */
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
    @Autowired
    private StockBlockRtInfoMapper stockBlockRtInfoMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //必须包装该对象为无状态
    private HttpEntity<Object> httpEntity;
    @Autowired
    private StockCacheFace stockCacheFace;


    @Override
    public void getInnerMarketInfo() {
        //1.阶段1:采集原始数据
        //1.1组装url地址
        //String url = "http://hq.sinajs.cn/list=sh000001,sz399001";
        String url = stockInfoConfig.getMarketUrl() + String.join(",", stockInfoConfig.getInner());
        //1.2维护请求头 添加防盗链和用户标识
//        HttpHeaders headers = new HttpHeaders();
//        //防盗链
//        headers.add("Referer","https://finance.sina.com.cn/stock/");
//        //用户客户端标识
//        headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
//        //维护http请求实体对象
//        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        //发送请求
        ResponseEntity<String> responseEntity =
                restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
        //状态码
        int statusCodeValue = responseEntity.getStatusCodeValue();
        if (responseEntity.getStatusCodeValue() != 200) {
            //当前请求失败
            log.error("当前时间点:{},采集数据失败,http状态码:{}",
                    DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), statusCodeValue);
            //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
            return;
        }
        //获取js格式数据
        String jsData = responseEntity.getBody();
        log.info("当前时间点:{},采集原始数据的内容:{}",
                DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), jsData);
        //2.阶段2:java正则解析原始数据
        //2.1定义正则表达式
        //String reg = var hq_str_sh000001="上证指数,3209.7832,3211.4299,3207.8797,3219.4877,3185.4631,0,0,426403551,439580087328,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2025-01-06,14:59:23,00,";
        String reg = "var hq_str_(.+)=\"(.+)\";";
        //2.2表达式编译
        Pattern pattern = Pattern.compile(reg);
        //2.3匹配字符串
        Matcher matcher = pattern.matcher(jsData);
        ArrayList<StockMarketIndexInfo> entities = new ArrayList<>();
        while (matcher.find()) {
            //1.获取大盘的编码
            String marketCode = matcher.group(1);
            //获取其他信息
            String otherInfo = matcher.group(2);
            //讲other字符串以逗号进行分割 获取大盘的详细信息
            String[] splitArr = otherInfo.split(",");
            //大盘名称
            String marketName = splitArr[0];
            //获取当前大盘的开盘点数
            BigDecimal openPoint = new BigDecimal(splitArr[1]);
            //前收盘点
            BigDecimal preClosePoint = new BigDecimal(splitArr[2]);
            //获取大盘的当前点数
            BigDecimal curPoint = new BigDecimal(splitArr[3]);
            //获取大盘最高点
            BigDecimal maxPoint = new BigDecimal(splitArr[4]);
            //获取大盘的最低点
            BigDecimal minPoint = new BigDecimal(splitArr[5]);
            //获取成交量
            Long tradeAmt = Long.valueOf(splitArr[8]);
            //获取成交金额
            BigDecimal tradeVol = new BigDecimal(splitArr[9]);
            //时间
            Date curTime = DateTimeUtil.getDateTimeWithoutSecond(splitArr[30] + " " + splitArr[31]).toDate();
            //3.阶段3:解析数据封装到entity
            StockMarketIndexInfo entity = StockMarketIndexInfo.builder()
                    .id(idWorker.nextId())
                    .marketCode(marketCode)
                    .marketName(marketName)
                    .curPoint(curPoint)
                    .openPoint(openPoint)
                    .preClosePoint(preClosePoint)
                    .maxPoint(maxPoint)
                    .minPoint(minPoint)
                    .tradeVolume(tradeVol)
                    .tradeAmount(tradeAmt)
                    .curTime(curTime)
                    .build();
            entities.add(entity);
        }
        log.info("解析数据完毕");
        //4.阶段4:调用mybatis批量入库
        int count = stockMarketIndexInfoMapper.insertBatch(entities);
        if (count > 0) {
            //大盘采集数据完毕之后 通知backend工程刷新缓存
            //发送
            rabbitTemplate.convertAndSend("stockExchange", "inner.market", new Date());

            log.info("当前时间:{},插入大盘数据:{}成功", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), entities);
        } else {
            log.error("当前时间:{},插入大盘数据:{}失败", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), entities);
        }
    }

    /**
     * 定义获取分钟级股票数据
     */
    @Override
    public void getStockRtIndex() {
        //1.获取所有个股集合
        List<String> stockIds = stockCacheFace.getAllStockCodeWithPredix();
//        List<String> stockIds = stockBusinessMapper.getStockIds();
//        //添加大盘业务前缀 sh sz
//        stockIds = stockIds.stream()
//                .map(code -> code.startsWith("6") ? "sh" + code : "sz" + code)
//                .collect(Collectors.toList());
        //一次性将所有的集合拼接到url地址中 所以导致地址过长参数过多
        //String url = stockInfoConfig.getMarketUrl()+String.join(",",stockIds);
        long startTime = System.currentTimeMillis();
        //核心思想:将大的集合切分成若干个小集合 分批次拉取数据
        //将所有个股编码组成大的的集合拆分成若干个小集合 40---->15  15  10
        Lists.partition(stockIds, 15).forEach(codes -> {
            //原始方案
            //分批次采集
//            String url = stockInfoConfig.getMarketUrl()+String.join(",",codes);
//            //1.2维护请求头 添加防盗链和用户标识
////            HttpHeaders headers = new HttpHeaders();
////            //防盗链
////            headers.add("Referer","https://finance.sina.com.cn/stock/");
////            //用户客户端标识
////            headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
////            //维护http请求实体对象
////            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
//            //发送请求
//            ResponseEntity<String> responseEntity =
//                    restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
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
//            //调用工具解析获取个股数据
//            List<StockRtInfo> list = parserStockInfoUtil.parser4StockOrMarketInfo(jsData, ParseType.ASHARE);
//            log.info("采集个股数据:{}",list);
//            //批量保存采集个股数据
//            int count = stockRtInfoMapper.insertBatch(list);
//            if (count>0){
//                log.info("当前时间:{},插入个股数据:{}成功",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list);
//            }else {
//                log.error("当前时间:{},插入个股数据:{}失败",DateTime.now().toString("yyyy-MM-dd HH:mm:ss"),list);
//            }
            //方案1:原始方案采集个股数据时候将集合分片 然后分批次采集数据 效率不高 踩在较高的采集延迟:引入多线程
            //代码问题:1.每次执行采集任务就创建一个线程 复用性差
            // 2.如果多线程使用不当造成cpu的竞争 导致频繁的上下文切换 导致程序性能降低
//            new Thread(() -> {
//                //分批次采集
//                String url = stockInfoConfig.getMarketUrl() + String.join(",", codes);
//                //1.2维护请求头 添加防盗链和用户标识
////            HttpHeaders headers = new HttpHeaders();
////            //防盗链
////            headers.add("Referer","https://finance.sina.com.cn/stock/");
////            //用户客户端标识
////            headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
////            //维护http请求实体对象
////            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
//                //发送请求
//                ResponseEntity<String> responseEntity =
//                        restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
//                //状态码
//                int statusCodeValue = responseEntity.getStatusCodeValue();
//                if (statusCodeValue != 200) {
//                    //当前请求失败
//                    log.error("当前时间点:{},采集数据失败,http状态码:{}",
//                            DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), statusCodeValue);
//                    //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
//                    return;
//                }
//                //获取js格式数据
//                String jsData = responseEntity.getBody();
//                //调用工具解析获取个股数据
//                List<StockRtInfo> list = parserStockInfoUtil.parser4StockOrMarketInfo(jsData, ParseType.ASHARE);
//                log.info("采集个股数据:{}", list);
//                //批量保存采集个股数据
//                int count = stockRtInfoMapper.insertBatch(list);
//                if (count > 0) {
//                    log.info("当前时间:{},插入个股数据:{}成功", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), list);
//                } else {
//                    log.error("当前时间:{},插入个股数据:{}失败", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), list);
//                }
//            }).start();
            //方案2:引入线程池
            threadPoolTaskExecutor.execute(()->{
                //分批次采集
                String url = stockInfoConfig.getMarketUrl() + String.join(",", codes);
                //1.2维护请求头 添加防盗链和用户标识
//            HttpHeaders headers = new HttpHeaders();
//            //防盗链
//            headers.add("Referer","https://finance.sina.com.cn/stock/");
//            //用户客户端标识
//            headers.add("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
//            //维护http请求实体对象
//            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
                //发送请求
                ResponseEntity<String> responseEntity =
                        restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
                //状态码
                int statusCodeValue = responseEntity.getStatusCodeValue();
                if (statusCodeValue != 200) {
                    //当前请求失败
                    log.error("当前时间点:{},采集数据失败,http状态码:{}",
                            DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), statusCodeValue);
                    //其他:发送邮件 企业微信 钉钉  短信给相关人员提醒
                    return;
                }
                //获取js格式数据
                String jsData = responseEntity.getBody();
                //调用工具解析获取个股数据
                List<StockRtInfo> list = parserStockInfoUtil.parser4StockOrMarketInfo(jsData, ParseType.ASHARE);
                log.info("采集个股数据:{}", list);
                //批量保存采集个股数据
                int count = stockRtInfoMapper.insertBatch(list);
                if (count > 0) {
                    log.info("当前时间:{},插入个股数据:{}成功", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), list);
                } else {
                    log.error("当前时间:{},插入个股数据:{}失败", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"), list);
                }
            });

        });
        long endTime = System.currentTimeMillis();
        long takeTime = endTime - startTime;
        log.info("本次采集划分时间:{}ms",takeTime);
    }

    /**
     * 获取板块实时数据
     * http://vip.stock.finance.sina.com.cn/q/view/newSinaHy.php
     */
    @Override
    public void getStockSectorRtIndex() {
        //发送板块数据请求
        String result = restTemplate.getForObject(stockInfoConfig.getBlockUrl(), String.class);
        //响应结果转板块集合数据
        List<StockBlockRtInfo> infos = parserStockInfoUtil.parse4StockBlock(result);
        log.info("板块数据量：{}", infos.size());
        //数据分片保存到数据库下 行业板块类目大概50个，可每小时查询一次即可
        Lists.partition(infos, 20).forEach(list -> {
            //20个一组，批量插入
            stockBlockRtInfoMapper.insertBatch(list);
        });
    }

    /*
     * bean生命周期的初始化的回调方法
     * */
    @PostConstruct
    public void initData() {
        //1.2维护请求头 添加防盗链和用户标识
        HttpHeaders headers = new HttpHeaders();
        //防盗链
        headers.add("Referer", "https://finance.sina.com.cn/stock/");
        //用户客户端标识
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        //维护http请求实体对象
        httpEntity = new HttpEntity<>(headers);
    }
}
